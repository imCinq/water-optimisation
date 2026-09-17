package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.Diagnostics;
import io.github.imcinq.wateroptimisation.FluidOptimizationPolicy;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import io.github.imcinq.wateroptimisation.TessellationContext;
import net.minecraft.core.BlockPos;
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
	private static final TessellationContext wateroptimisation$waterTessellation = new TessellationContext();

	/**
	 * Vanilla's addFace method emits the outward face and, when requested, its
	 * reverse face from the same call. Change only the argument so the outward
	 * face remains intact when the experimental mode is enabled.
	 */
	@ModifyVariable(method = "addFace", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private boolean wateroptimisation$disableOptionalBackFace(boolean addBackFace) {
		boolean diagnosticsEnabled = Diagnostics.isEnabled();
		if (diagnosticsEnabled) {
			Diagnostics.recordFluidFace(addBackFace);
		}
		if (!addBackFace || !FluidOptimizationPolicy.reducedWaterBackfacesActive()
				|| !wateroptimisation$waterTessellation.isOrdinaryWater()) {
			return addBackFace;
		}
		if (diagnosticsEnabled) {
			Diagnostics.recordReducedWaterBackface();
		}
		return false;
	}

	/** Bracket the complete transformed method, including cancellation and exceptions. */
	@WrapMethod(method = "tesselate")
	private void wateroptimisation$withInvocationState(
			BlockAndTintGetter level,
			BlockPos pos,
			FluidRenderer.Output output,
			BlockState blockState,
			FluidState fluidState,
			Operation<Void> original
	) {
		boolean diagnosticsEnabled = Diagnostics.isEnabled();
		boolean reducedBackfacesActive = FluidOptimizationPolicy.reducedWaterBackfacesActive();
		Boolean previous = wateroptimisation$waterTessellation.enter(
				reducedBackfacesActive && FluidOptimizationPolicy.isOrdinarySourceWater(blockState, fluidState));
		try {
			if (diagnosticsEnabled) {
				Diagnostics.beginFluidCompile();
			}
			original.call(level, pos, output, blockState, fluidState);
		} finally {
			wateroptimisation$waterTessellation.exit(previous);
			if (diagnosticsEnabled) {
				Diagnostics.endFluidCompile();
			}
		}
	}

	@Inject(
			method = "tesselate",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/block/FluidRenderer;shouldRenderFace(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)Z",
					ordinal = 0
			),
			cancellable = true,
			require = 0,
			locals = LocalCapture.CAPTURE_FAILSOFT
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
		boolean fastPathActive = FluidOptimizationPolicy.flatWaterFastPathActive();
		if (fastPathActive
				&& Diagnostics.isEnabled()
				&& FluidOptimizationPolicy.flatWaterFastPathObservationActive()) {
			FluidOptimizationPolicy.markFlatWaterFastPathHookObserved();
		}
		if (!fastPathActive
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
		}
		// The enclosing WrapMethod finally block closes diagnostics and state.
		callback.cancel();
	}
}
