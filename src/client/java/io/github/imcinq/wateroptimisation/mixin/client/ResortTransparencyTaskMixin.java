package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.Diagnostics;
import net.minecraft.client.renderer.SectionBufferBuilderPack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$ResortTransparencyTask")
public abstract class ResortTransparencyTaskMixin {
	@Inject(method = "doTask", at = @At("HEAD"))
	private void wateroptimisation$beforeResort(
			SectionBufferBuilderPack buffers,
			CallbackInfoReturnable<?> callback
	) {
		Diagnostics.beginTranslucentResort();
	}

	@Inject(method = "doTask", at = @At("RETURN"))
	private void wateroptimisation$afterResort(
			SectionBufferBuilderPack buffers,
			CallbackInfoReturnable<?> callback
	) {
		Diagnostics.endTranslucentResort();
	}
}
