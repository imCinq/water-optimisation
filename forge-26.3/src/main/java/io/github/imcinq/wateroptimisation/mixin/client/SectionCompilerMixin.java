package io.github.imcinq.wateroptimisation.mixin.client;

import com.mojang.blaze3d.vertex.VertexSorting;
import io.github.imcinq.wateroptimisation.Diagnostics;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = SectionCompiler.class, remap = false)
public abstract class SectionCompilerMixin {
	@Inject(
			method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
			at = @At(value = "HEAD", remap = false),
			remap = false
	)
	private void wateroptimisation$beforeCompile(
			SectionPos sectionPos,
			RenderSectionRegion region,
			VertexSorting vertexSorting,
			SectionBufferBuilderPack builders,
			CallbackInfoReturnable<?> callback
	) {
		if (Diagnostics.isEnabled()) {
			Diagnostics.beginSectionCompile();
		}
	}

	@Inject(
			method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;",
			at = @At(value = "RETURN", remap = false),
			remap = false
	)
	private void wateroptimisation$afterCompile(
			SectionPos sectionPos,
			RenderSectionRegion region,
			VertexSorting vertexSorting,
			SectionBufferBuilderPack builders,
			CallbackInfoReturnable<?> callback
	) {
		if (Diagnostics.isEnabled()) {
			Diagnostics.endSectionCompile();
		}
	}
}
