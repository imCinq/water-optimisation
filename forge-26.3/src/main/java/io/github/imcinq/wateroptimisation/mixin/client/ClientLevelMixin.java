package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.WaterOptimisationClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientLevel.class, remap = false)
public abstract class ClientLevelMixin {
	@Inject(
			method = "addParticle(Lnet/minecraft/core/particles/ParticleOptions;ZZDDDDDD)V",
			at = @At(value = "HEAD", remap = false),
			cancellable = true,
			remap = false
	)
	private void wateroptimisation$filterWaterParticle(
			ParticleOptions particle,
			boolean overrideLimiter,
			boolean alwaysShow,
			double x,
			double y,
			double z,
			double velocityX,
			double velocityY,
			double velocityZ,
			CallbackInfo callback
	) {
		if (!WaterOptimisationClient.shouldKeepWaterParticle(particle, alwaysShow, x, y, z)) {
			callback.cancel();
		}
	}
}
