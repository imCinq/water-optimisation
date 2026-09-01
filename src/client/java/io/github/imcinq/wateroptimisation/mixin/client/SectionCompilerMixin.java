package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.Diagnostics;
import io.github.imcinq.wateroptimisation.FarWaterOwnershipProbe;
import io.github.imcinq.wateroptimisation.WaterSectionOwnership;
import io.github.imcinq.wateroptimisation.WaterSectionOwnershipResultsAccess;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
	@Inject(
			method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
			at = @At("HEAD")
	)
	private void wateroptimisation$beforeCompile(
			SectionPos sectionPos,
			RenderSectionRegion region,
			VertexSorting vertexSorting,
			SectionBufferBuilderPack builders,
			CallbackInfoReturnable<?> callback
	) {
		FarWaterOwnershipProbe.beginSection(sectionPos, region);
		if (!Diagnostics.isEnabled()) {
			return;
		}
		Diagnostics.beginSectionCompile();
	}

	@Inject(
			method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
			at = @At("RETURN")
	)
	private void wateroptimisation$afterCompile(
			SectionPos sectionPos,
			RenderSectionRegion region,
			VertexSorting vertexSorting,
			SectionBufferBuilderPack builders,
			CallbackInfoReturnable<?> callback
	) {
		WaterSectionOwnership ownership = FarWaterOwnershipProbe.endSection();
		if (callback.getReturnValue() instanceof WaterSectionOwnershipResultsAccess access) {
			access.wateroptimisation$setWaterOwnership(ownership);
		}
		if (!Diagnostics.isEnabled()) {
			return;
		}
		Diagnostics.endSectionCompile();
	}
}
