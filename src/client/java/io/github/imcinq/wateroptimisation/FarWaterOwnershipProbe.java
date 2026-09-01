package io.github.imcinq.wateroptimisation;

import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * Captures ordinary still-water faces while a 26.2 section is compiled.
 * Sections are preflighted before any face can be cancelled so mixed
 * translucent terrain remains entirely on vanilla's sorted buffer.
 */
public final class FarWaterOwnershipProbe {
	private static final ThreadLocal<SectionCapture> SECTION = new ThreadLocal<>();
	private static final ThreadLocal<FluidCapture> FLUID = new ThreadLocal<>();

	private FarWaterOwnershipProbe() {
	}

	public static void beginSection(SectionPos sectionPos, RenderSectionRegion region) {
		FLUID.remove();
		boolean diagnosticsEnabled = Diagnostics.isEnabled();
		boolean farPassActive = FluidOptimizationPolicy.farWaterPassActive();
		if (!diagnosticsEnabled && !farPassActive) {
			SECTION.remove();
			return;
		}
		SECTION.set(new SectionCapture(
				farPassActive && isFarPassEligible(sectionPos, region)
		));
	}

	public static WaterSectionOwnership endSection() {
		FLUID.remove();
		SectionCapture capture = SECTION.get();
		SECTION.remove();
		if (capture == null) {
			return WaterSectionOwnership.EMPTY;
		}
		WaterSectionOwnership ownership = capture.finish();
		ownership.publishDiagnostics();
		return ownership;
	}

	public static void beginFluid(BlockState blockState, FluidState fluidState) {
		SectionCapture section = SECTION.get();
		boolean captureMesh = section != null && section.farPassEligible;
		if (!Diagnostics.isEnabled() && !captureMesh) {
			FLUID.remove();
			return;
		}
		FLUID.set(new FluidCapture(
				fluidState.getType() == Fluids.WATER,
				FluidOptimizationPolicy.isOrdinarySourceWater(blockState, fluidState),
				captureMesh
		));
	}

	/** Marks that the exact 26.2 addFace hook is present in this section. */
	public static void markOwnedGeometryHookApplied() {
		SectionCapture section = SECTION.get();
		if (section != null && section.farPassEligible) {
			section.ownedGeometryHookApplied = true;
		}
	}

	/**
	 * Copies one vanilla fluid face into the owned mesh. Returning true tells
	 * the mixin to cancel the shared-buffer write; every other case stays on
	 * vanilla and therefore cannot be affected by this experimental path.
	 */
	public static boolean captureOwnedFace(
			float x0, float y0, float z0, float u0, float v0,
			float x1, float y1, float z1, float u1, float v1,
			float x2, float y2, float z2, float u2, float v2,
			float x3, float y3, float z3, float u3, float v3,
			int color, int lightCoords, boolean addBackFace
	) {
		FluidCapture fluid = FLUID.get();
		SectionCapture section = SECTION.get();
		if (fluid == null || section == null || !fluid.captureMesh || !fluid.ordinarySourceWater) {
			return false;
		}
		// The dedicated pass is drawn after translucent terrain and has no
		// per-face vanilla sort order. Own only upward water surfaces; vertical
		// sides and bottoms stay in vanilla's shared translucent buffer.
		if (!isUpwardFace(x0, y0, z0, x1, y1, z1, x2, y2, z2)) {
			return false;
		}
		if (section.meshBuilder == null) {
			section.meshBuilder = WaterOwnedMesh.builder();
		}
		section.meshBuilder.addFace(
				x0, y0, z0, u0, v0,
				x1, y1, z1, u1, v1,
				x2, y2, z2, u2, v2,
				x3, y3, z3, u3, v3,
				color, lightCoords, addBackFace
		);
		fluid.ownedFaces++;
		fluid.ownedVertices += addBackFace ? 8 : 4;
		return true;
	}

	public static void endFluid() {
		FluidCapture capture = FLUID.get();
		FLUID.remove();
		SectionCapture section = SECTION.get();
		if (capture == null || section == null) {
			return;
		}
		if (capture.ordinarySourceWater && capture.ownedFaces > 0) {
			section.candidateBlocks++;
			section.candidateFaces += capture.ownedFaces;
			section.candidateVertices += capture.ownedVertices;
		} else if (capture.water) {
			section.fallbackBlocks++;
		}
	}

	private static boolean isUpwardFace(
			float x0, float y0, float z0,
			float x1, float y1, float z1,
			float x2, float y2, float z2
	) {
		double edge1X = x1 - x0;
		double edge1Y = y1 - y0;
		double edge1Z = z1 - z0;
		double edge2X = x2 - x0;
		double edge2Y = y2 - y0;
		double edge2Z = z2 - z0;
		double normalX = edge1Y * edge2Z - edge1Z * edge2Y;
		double normalY = edge1Z * edge2X - edge1X * edge2Z;
		double normalZ = edge1X * edge2Y - edge1Y * edge2X;
		return normalY > 0.01D
				&& Math.abs(normalX) <= normalY * 0.25D
				&& Math.abs(normalZ) <= normalY * 0.25D;
	}

	private static boolean isFarPassEligible(SectionPos sectionPos, RenderSectionRegion region) {
		boolean sawOrdinaryWater = false;
		BlockPos min = sectionPos.origin();
		BlockPos max = min.offset(15, 15, 15);
		for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
			BlockState blockState = region.getBlockState(pos);
			FluidState fluidState = blockState.getFluidState();
			boolean ordinarySourceWater = FluidOptimizationPolicy.isOrdinarySourceWater(blockState, fluidState);
			if (!fluidState.isEmpty()) {
				if (!ordinarySourceWater || !hasSafeNeighbors(pos, region)) {
					return false;
				}
				sawOrdinaryWater = true;
			}

			// 26.2 no longer exposes the old block render-type lookup. Treat every
			// non-air, non-solid block as a mixed/ambiguous section instead. This is
			// intentionally stricter: glass, leaves, plants, overlays, and custom
			// translucent models remain on vanilla's shared sorted buffer.
			if (fluidState.isEmpty()
					&& !blockState.isAir()
					&& blockState.getRenderShape() == RenderShape.MODEL
					&& !blockState.isSolidRender()) {
				return false;
			}
		}
		return sawOrdinaryWater;
	}

	private static boolean hasSafeNeighbors(BlockPos pos, RenderSectionRegion region) {
		for (Direction direction : Direction.values()) {
			// The open top of a water surface is not an overlay. The other five
			// directions are kept conservative around glass, leaves, and shapes.
			if (direction == Direction.UP) {
				continue;
			}
			BlockPos neighborPos = pos.relative(direction);
			BlockState neighborState = region.getBlockState(neighborPos);
			FluidState neighborFluid = neighborState.getFluidState();
			if (!FluidOptimizationPolicy.isOrdinarySourceWater(neighborState, neighborFluid)
					&& !neighborState.isSolidRender()) {
				return false;
			}
		}
		return true;
	}

	private static final class SectionCapture {
		private final boolean farPassEligible;
		private boolean ownedGeometryHookApplied;
		private WaterOwnedMesh.Builder meshBuilder;
		private long candidateBlocks;
		private long candidateFaces;
		private long candidateVertices;
		private long fallbackBlocks;

		private SectionCapture(boolean farPassEligible) {
			this.farPassEligible = farPassEligible;
		}

		private WaterSectionOwnership finish() {
			WaterOwnedMesh mesh = null;
			if (this.farPassEligible && this.ownedGeometryHookApplied && this.meshBuilder != null && this.candidateFaces > 0L) {
				mesh = this.meshBuilder.build();
				this.meshBuilder = null;
			} else if (this.meshBuilder != null) {
				this.meshBuilder.close();
				this.meshBuilder = null;
			}
			return new WaterSectionOwnership(
					this.candidateBlocks,
					this.candidateFaces,
					this.candidateVertices,
					this.fallbackBlocks,
					mesh
			);
		}
	}

	private static final class FluidCapture {
		private final boolean water;
		private final boolean ordinarySourceWater;
		private final boolean captureMesh;
		private int ownedFaces;
		private int ownedVertices;

		private FluidCapture(boolean water, boolean ordinarySourceWater, boolean captureMesh) {
			this.water = water;
			this.ordinarySourceWater = ordinarySourceWater;
			this.captureMesh = captureMesh && ordinarySourceWater;
		}
	}
}
