package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.Diagnostics;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

// NeoForge's section rebuild calls the overload that also takes the
// AddSectionGeometryEvent renderers; the four-argument overload only delegates.
@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
	@Inject(
			method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
			at = @At("HEAD")
	)
	private void wateroptimisation$beforeCompile(
				SectionPos sectionPos,
				RenderSectionRegion region,
				VertexSorting vertexSorting,
				SectionBufferBuilderPack builders,
				List<?> additionalRenderers,
				CallbackInfoReturnable<?> callback
	) {
		if (Diagnostics.isEnabled()) {
			Diagnostics.beginSectionCompile();
		}
	}

	@Inject(
			method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;Ljava/util/List;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
			at = @At("RETURN")
	)
	private void wateroptimisation$afterCompile(
			SectionPos sectionPos,
			RenderSectionRegion region,
			VertexSorting vertexSorting,
			SectionBufferBuilderPack builders,
			List<?> additionalRenderers,
			CallbackInfoReturnable<?> callback
	) {
		if (Diagnostics.isEnabled()) {
			Diagnostics.endSectionCompile();
		}
	}
}
