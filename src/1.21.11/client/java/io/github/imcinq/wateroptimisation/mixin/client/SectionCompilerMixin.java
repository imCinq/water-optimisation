package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.Diagnostics;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Uses name-only injections so old parameter-type names cannot break loading. */
@Mixin(SectionCompiler.class)
public abstract class SectionCompilerMixin {
	@Inject(method = "compile", at = @At("HEAD"), require = 0)
	private void wateroptimisation$beforeCompile(CallbackInfoReturnable<?> callback) {
		if (Diagnostics.isEnabled()) {
			Diagnostics.beginSectionCompile();
		}
	}

	@Inject(method = "compile", at = @At("RETURN"), require = 0)
	private void wateroptimisation$afterCompile(CallbackInfoReturnable<?> callback) {
		if (Diagnostics.isEnabled()) {
			Diagnostics.endSectionCompile();
		}
	}
}
