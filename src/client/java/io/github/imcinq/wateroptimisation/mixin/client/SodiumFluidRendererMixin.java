package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.SodiumFluidIntegration;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Optional, exact-class-name Sodium bridge. It intentionally has no Sodium
 * compile-time dependency and every injection is fail-closed so an unknown
 * Sodium build keeps its own renderer untouched.
 */
@Pseudo
@Mixin(targets = "net.caffeinemc.mods.sodium.client.render.chunk.compile.pipeline.DefaultFluidRenderer", remap = false)
public abstract class SodiumFluidRendererMixin {
	/** Sodium's render method has BlockState as its first block-state argument. */
	@ModifyVariable(
			method = "render",
			at = @At("HEAD"),
			argsOnly = true,
			ordinal = 0,
			require = 0
	)
	private BlockState wateroptimisation$markRender(BlockState blockState) {
		SodiumFluidIntegration.beginRender(blockState);
		return blockState;
	}

	@Inject(method = "render", at = @At("RETURN"), require = 0)
	private void wateroptimisation$finishRender(CallbackInfo callback) {
		SodiumFluidIntegration.endRender();
	}

	/** Sodium's final boolean is the reversed-quad flag in the tested renderer. */
	@ModifyVariable(
			method = "writeQuad",
			at = @At("HEAD"),
			argsOnly = true,
			ordinal = 0,
			require = 0
	)
	private boolean wateroptimisation$reduceReverseFace(boolean flip) {
		return SodiumFluidIntegration.reduceReverseFace(flip);
	}
}
