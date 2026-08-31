package io.github.imcinq.wateroptimisation;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.PrimitiveTopology;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.FilterMode;
import com.mojang.blaze3d.vertex.VertexFormat;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.DynamicUniforms;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.chunk.ChunkSectionsToRender;
import net.minecraft.client.renderer.chunk.SectionMesh;
import net.minecraft.client.renderer.chunk.SectionRenderDispatcher;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * Draws the water-only meshes captured from qualifying sections. The pass is
 * intentionally a hard 64-block cutoff for the first GPU experiment: near
 * water keeps the vanilla terrain pipeline, while distant eligible water is
 * omitted instead of making the shared translucent buffer draw it.
 */
public final class FarWaterRenderer {
	private static final double MAX_DISTANCE = 64.0D;
	private static final double MAX_DISTANCE_SQUARED = MAX_DISTANCE * MAX_DISTANCE;

	private FarWaterRenderer() {
	}

	public static void register() {
		LevelRenderEvents.AFTER_TRANSLUCENT_TERRAIN.register(FarWaterRenderer::render);
	}

	private static void render(LevelRenderContext context) {
		EffectiveWaterPolicy policy = WaterOptimisationClient.effectivePolicy(ConfigManager.get());
		if (!policy.farWaterPassActive()) {
			return;
		}

		ChunkSectionsToRender sections = context.sectionsToRender();
		if (sections == null) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		Vec3 cameraPosition = client.gameRenderer.mainCamera().position();
		List<WaterDraw> draws = new ArrayList<>();
		List<DynamicUniforms.ChunkSectionInfo> sectionInfos = new ArrayList<>();
		int largestIndexCount = 0;
		ObjectArrayList<?> visibleSections = context.levelRenderer().visibleSections();

		for (Object sectionObject : visibleSections) {
			if (!(sectionObject instanceof SectionRenderDispatcher.RenderSection section)) {
				continue;
			}
			if (!isWithinDistance(section, cameraPosition)) {
				Diagnostics.recordFarWaterDistanceSkip();
				continue;
			}

			SectionMesh sectionMesh = section.getSectionMesh();
			if (!(sectionMesh instanceof WaterSectionOwnershipAccess ownershipAccess)) {
				continue;
			}
			WaterOwnedMesh waterMesh = ownershipAccess.wateroptimisation$getOwnedMesh();
			if (waterMesh == null || waterMesh.indexCount() <= 0) {
				continue;
			}
			try {
				if (!waterMesh.ensureUploaded()) {
					throw new IllegalStateException("water-owned mesh was unavailable");
				}
			} catch (RuntimeException exception) {
				WaterOptimisationClient.disableFarWaterPassForSession(exception);
				return;
			}

			var origin = section.getRenderOrigin();
			sectionInfos.add(new DynamicUniforms.ChunkSectionInfo(
					RenderSystem.getModelViewMatrixCopy(),
					origin.getX(),
					origin.getY(),
					origin.getZ(),
					section.getVisibility(net.minecraft.util.Util.getMillis()),
					sections.textureView().getWidth(0),
					sections.textureView().getHeight(0)
			));
			draws.add(new WaterDraw(waterMesh, sectionInfos.size() - 1));
			largestIndexCount = Math.max(largestIndexCount, waterMesh.indexCount());
		}

		if (draws.isEmpty() || largestIndexCount <= 0) {
			return;
		}

		GpuBufferSlice[] uniforms = RenderSystem.getDynamicUniforms().writeChunkSections(
				sectionInfos.toArray(new DynamicUniforms.ChunkSectionInfo[0])
		);
		RenderTarget target = client.gameRenderer.mainRenderTarget();
		try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
				() -> "Water Optimisation owned water",
				target.getColorTextureView(),
				Optional.empty(),
				target.getDepthTextureView(),
				OptionalDouble.empty()
		)) {
			RenderSystem.bindDefaultUniforms(renderPass);
			renderPass.bindTexture(
					"Sampler0",
					sections.textureView(),
					RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
			);
			renderPass.bindTexture(
					"Sampler2",
					client.gameRenderer.levelLightmap(),
					RenderSystem.getSamplerCache().getClampToEdge(FilterMode.LINEAR)
			);
			renderPass.setPipeline(RenderPipelines.TRANSLUCENT_TERRAIN);

			RenderSystem.AutoStorageIndexBuffer sequentialIndices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
			GpuBuffer indexBuffer = sequentialIndices.getBuffer(largestIndexCount);
			renderPass.setIndexBuffer(indexBuffer, sequentialIndices.type());
			for (WaterDraw draw : draws) {
				renderPass.setUniform("ChunkSection", uniforms[draw.uniformIndex()]);
				renderPass.setVertexBuffer(0, draw.mesh().vertexBuffer().slice());
				renderPass.drawIndexed(draw.mesh().indexCount(), 1, 0, 0, 0);
				Diagnostics.recordFarWaterDraw(draw.mesh().indexCount());
			}
		} catch (RuntimeException exception) {
			WaterOptimisationClient.disableFarWaterPassForSession(exception);
		}
	}

	private static boolean isWithinDistance(SectionRenderDispatcher.RenderSection section, Vec3 cameraPosition) {
		var origin = section.getRenderOrigin();
		double dx = distanceToInterval(cameraPosition.x, origin.getX(), origin.getX() + 16.0D);
		double dy = distanceToInterval(cameraPosition.y, origin.getY(), origin.getY() + 16.0D);
		double dz = distanceToInterval(cameraPosition.z, origin.getZ(), origin.getZ() + 16.0D);
		return dx * dx + dy * dy + dz * dz <= MAX_DISTANCE_SQUARED;
	}

	private static double distanceToInterval(double value, double min, double max) {
		if (value < min) {
			return min - value;
		}
		if (value > max) {
			return value - max;
		}
		return 0.0D;
	}

	private record WaterDraw(WaterOwnedMesh mesh, int uniformIndex) {
	}
}
