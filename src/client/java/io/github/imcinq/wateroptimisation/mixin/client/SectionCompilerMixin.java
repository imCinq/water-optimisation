package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.Diagnostics;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.VertexSorting;
import net.minecraft.client.renderer.chunk.RenderSectionRegion;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.core.SectionPos;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
	/** Bracket the complete compile, including exceptions, so diagnostics stay balanced. */
	@WrapMethod(method = "compile(Lnet/minecraft/core/SectionPos;Lnet/minecraft/client/renderer/chunk/RenderSectionRegion;Lcom/mojang/blaze3d/vertex/VertexSorting;Lnet/minecraft/client/renderer/SectionBufferBuilderPack;)Lnet/minecraft/client/renderer/chunk/SectionCompiler$Results;")
	private SectionCompiler.Results wateroptimisation$aroundCompile(
				SectionPos sectionPos,
				RenderSectionRegion region,
				VertexSorting vertexSorting,
				SectionBufferBuilderPack builders,
				Operation<SectionCompiler.Results> original
	) {
		boolean diagnosticsEnabled = Diagnostics.isEnabled();
		if (diagnosticsEnabled) {
			Diagnostics.beginSectionCompile();
		}
		try {
			return original.call(sectionPos, region, vertexSorting, builders);
		} finally {
			if (diagnosticsEnabled) {
				Diagnostics.endSectionCompile();
			}
		}
	}
}
