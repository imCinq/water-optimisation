package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.Diagnostics;
import io.github.imcinq.wateroptimisation.FluidOptimizationPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Conservative 1.21.1 adapter. LiquidBlockRenderer has a different method
 * body from the 26.2 renderer and does not expose a stable local-state hook,
 * so the compatibility path performs the six-neighbor probe only after the
 * cheap ordinary-source-water check. Reverse-face reduction stays disabled on
 * this target until an exact renderer hook is reviewed.
 */
@Mixin(LiquidBlockRenderer.class)
public abstract class FluidRendererMixin {
	@Inject(method = "tesselate", at = @At("HEAD"), require = 0)
	private void wateroptimisation$beforeTesselate(
			BlockAndTintGetter level,
			BlockPos pos,
			VertexConsumer consumer,
			BlockState blockState,
			FluidState fluidState,
			CallbackInfo callback
	) {
		if (Diagnostics.isEnabled()) {
			Diagnostics.beginFluidCompile();
		}
	}

	@Inject(method = "tesselate", at = @At("HEAD"), cancellable = true, require = 0)
	private void wateroptimisation$skipInteriorSourceWater(
			BlockAndTintGetter level,
			BlockPos pos,
			VertexConsumer consumer,
			BlockState blockState,
			FluidState fluidState,
			CallbackInfo callback
	) {
		if (!FluidOptimizationPolicy.flatWaterFastPathActive()
				|| !FluidOptimizationPolicy.isOrdinarySourceWater(blockState, fluidState)) {
			return;
		}

		BlockState blockStateDown = level.getBlockState(pos.below());
		FluidState fluidStateDown = level.getFluidState(pos.below());
		BlockState blockStateUp = level.getBlockState(pos.above());
		FluidState fluidStateUp = level.getFluidState(pos.above());
		BlockState blockStateNorth = level.getBlockState(pos.north());
		FluidState fluidStateNorth = level.getFluidState(pos.north());
		BlockState blockStateSouth = level.getBlockState(pos.south());
		FluidState fluidStateSouth = level.getFluidState(pos.south());
		BlockState blockStateWest = level.getBlockState(pos.west());
		FluidState fluidStateWest = level.getFluidState(pos.west());
		BlockState blockStateEast = level.getBlockState(pos.east());
		FluidState fluidStateEast = level.getFluidState(pos.east());

		if (!FluidOptimizationPolicy.shouldSkipInteriorSourceWater(
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
		callback.cancel();
	}

	@Inject(method = "tesselate", at = @At("RETURN"), require = 0)
	private void wateroptimisation$afterTesselate(
			BlockAndTintGetter level,
			BlockPos pos,
			VertexConsumer consumer,
			BlockState blockState,
			FluidState fluidState,
			CallbackInfo callback
	) {
		if (Diagnostics.isEnabled()) {
			Diagnostics.endFluidCompile();
		}
	}
}
