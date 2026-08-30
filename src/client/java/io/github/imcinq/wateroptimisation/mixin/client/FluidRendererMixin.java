package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.Diagnostics;
import io.github.imcinq.wateroptimisation.FluidOptimizationPolicy;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
	@Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
	private void wateroptimisation$beforeTesselate(
			BlockAndTintGetter level,
			BlockPos pos,
			FluidRenderer.Output output,
			BlockState blockState,
			FluidState fluidState,
			CallbackInfo callback
	) {
		boolean fastPathActive = FluidOptimizationPolicy.flatWaterFastPathActive();
		if (!fastPathActive && !Diagnostics.isEnabled()) {
			return;
		}

		Diagnostics.recordFluidBlock();
		Diagnostics.beginFluidCompile();
		if (fastPathActive && FluidOptimizationPolicy.shouldSkipInteriorSourceWater(level, pos, blockState, fluidState)) {
			Diagnostics.recordFluidFastPathSkip();
			Diagnostics.endFluidCompile();
			callback.cancel();
		}
	}

	@Inject(method = "tesselate", at = @At("RETURN"))
	private void wateroptimisation$afterTesselate(
			BlockAndTintGetter level,
			BlockPos pos,
			FluidRenderer.Output output,
			BlockState blockState,
			FluidState fluidState,
			CallbackInfo callback
	) {
		Diagnostics.endFluidCompile();
	}

	@Inject(
			method = "shouldRenderFace(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)Z",
			at = @At("HEAD"),
			cancellable = true
	)
	private static void wateroptimisation$overrideFaceDecision(
			FluidState fluidState,
			BlockState selfState,
			Direction direction,
			FluidState otherFluidState,
			CallbackInfoReturnable<Boolean> callback
	) {
		if (!FluidOptimizationPolicy.fluidHooksActive() && !Diagnostics.isEnabled()) {
			return;
		}

		Boolean decision = FluidOptimizationPolicy.overrideFaceDecision(fluidState, selfState, direction, otherFluidState);
		if (decision != null) {
			Diagnostics.recordFluidFaceOverride();
			callback.setReturnValue(decision);
		}
	}

	@Inject(
			method = "shouldRenderFace(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)Z",
			at = @At("RETURN")
	)
	private static void wateroptimisation$recordFaceDecision(
			FluidState fluidState,
			BlockState selfState,
			Direction direction,
			FluidState otherFluidState,
			CallbackInfoReturnable<Boolean> callback
	) {
		Diagnostics.recordFluidFace(callback.getReturnValueZ());
	}
}
