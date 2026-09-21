package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.Diagnostics;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.renderer.chunk.SectionRenderDispatcher$RenderSection$ResortTransparencyTask")
public abstract class ResortTransparencyTaskMixin {
	@Inject(method = "doTask", at = @At("HEAD"), require = 0)
	private void wateroptimisation$beforeResort(CallbackInfoReturnable<?> callback) {
		if (Diagnostics.isEnabled()) {
			Diagnostics.beginTranslucentResort();
		}
	}

	@Inject(method = "doTask", at = @At("RETURN"), require = 0)
	private void wateroptimisation$afterResort(CallbackInfoReturnable<?> callback) {
		if (Diagnostics.isEnabled()) {
			Diagnostics.endTranslucentResort();
		}
	}
}
