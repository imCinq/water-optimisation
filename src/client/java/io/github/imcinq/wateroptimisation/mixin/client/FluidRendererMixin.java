package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.Diagnostics;
import io.github.imcinq.wateroptimisation.FluidOptimizationPolicy;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
	@Unique
	private static final ThreadLocal<Boolean> wateroptimisation$waterTessellation = new ThreadLocal<>();

	/**
	 * Vanilla's addFace method emits the outward face and, when requested, its
	 * reverse face from the same call. Change only the argument so the outward
	 * face remains intact when the experimental mode is enabled.
	 */
	@ModifyVariable(method = "addFace", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private boolean wateroptimisation$disableOptionalBackFace(boolean addBackFace) {
		if (!addBackFace || !FluidOptimizationPolicy.reducedWaterBackfacesActive()) {
			return addBackFace;
		}
		return Boolean.TRUE.equals(wateroptimisation$waterTessellation.get()) ? false : addBackFace;
	}

	@Inject(method = "tesselate", at = @At("HEAD"))
	private void wateroptimisation$beforeTesselate(
			BlockAndTintGetter level,
			BlockPos pos,
			FluidRenderer.Output output,
			BlockState blockState,
			FluidState fluidState,
			CallbackInfo callback
	) {
		if (FluidOptimizationPolicy.reducedWaterBackfacesActive()) {
			wateroptimisation$waterTessellation.set(fluidState.is(FluidTags.WATER));
		}
		if (!Diagnostics.isEnabled()) {
			return;
		}

		Diagnostics.beginFluidCompile();
	}

	@Inject(
			method = "tesselate",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/block/FluidRenderer;shouldRenderFace(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)Z",
					ordinal = 0
			),
			cancellable = true,
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void wateroptimisation$skipInteriorSourceWater(
			BlockAndTintGetter level,
			BlockPos pos,
			FluidRenderer.Output output,
			BlockState blockState,
			FluidState fluidState,
			CallbackInfo callback,
			BlockState blockStateDown,
			FluidState fluidStateDown,
			BlockState blockStateUp,
			FluidState fluidStateUp,
			BlockState blockStateNorth,
			FluidState fluidStateNorth,
			BlockState blockStateSouth,
			FluidState fluidStateSouth,
			BlockState blockStateWest,
			FluidState fluidStateWest,
			BlockState blockStateEast,
			FluidState fluidStateEast,
			boolean renderUp
	) {
		if (!FluidOptimizationPolicy.flatWaterFastPathActive()
				|| !FluidOptimizationPolicy.shouldSkipInteriorSourceWater(
						blockState,
						fluidState,
						blockStateDown,
						fluidStateDown,
						blockStateUp,
						fluidStateUp,
						blockStateNorth,
						fluidStateNorth,
						blockStateSouth,
						fluidStateSouth,
						blockStateWest,
						fluidStateWest,
						blockStateEast,
						fluidStateEast
				)) {
			return;
		}

		if (Diagnostics.isEnabled()) {
			Diagnostics.recordFluidFastPathSkip();
			Diagnostics.endFluidCompile();
		}
		if (FluidOptimizationPolicy.reducedWaterBackfacesActive()) {
			wateroptimisation$waterTessellation.remove();
		}
		callback.cancel();
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
		if (FluidOptimizationPolicy.reducedWaterBackfacesActive()) {
			wateroptimisation$waterTessellation.remove();
		}
		if (!Diagnostics.isEnabled()) {
			return;
		}
		Diagnostics.endFluidCompile();
	}
}
