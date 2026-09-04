package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.Diagnostics;
import io.github.imcinq.wateroptimisation.FluidOptimizationPolicy;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.client.renderer.block.LiquidBlockRenderer;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Conservative 1.21.1 adapter. LiquidBlockRenderer has a different method
 * body from the 26.2 renderer and does not expose a stable local-state hook,
 * so the compatibility path performs the six-neighbor probe only after the
 * cheap ordinary-source-water and upward-neighbor checks, and stops at the
 * first non-water neighbor. Reverse-face reduction is intentionally
 * unsupported on this compatibility target.
 */
@Mixin(LiquidBlockRenderer.class)
public abstract class FluidRendererMixin {
	@Unique
	private static final ThreadLocal<BlockPos.MutableBlockPos> wateroptimisation$neighborPosition =
			ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

	@Inject(method = "tesselate", at = @At("HEAD"), cancellable = true, require = 0)
	private void wateroptimisation$beforeTesselate(
			BlockAndTintGetter level,
			BlockPos pos,
			VertexConsumer consumer,
			BlockState blockState,
			FluidState fluidState,
			CallbackInfo callback
	) {
		boolean fastPathActive = FluidOptimizationPolicy.flatWaterFastPathActive();
		boolean diagnosticsEnabled = Diagnostics.isEnabled();
		if (fastPathActive
				&& diagnosticsEnabled
				&& FluidOptimizationPolicy.flatWaterFastPathObservationActive()) {
			FluidOptimizationPolicy.markFlatWaterFastPathHookObserved();
		}
		if (diagnosticsEnabled) {
			Diagnostics.beginFluidCompile();
		}
		if (!fastPathActive
				|| !FluidOptimizationPolicy.isOrdinarySourceWater(blockState, fluidState)) {
			return;
		}

		// Open-surface water is the common case. The upward face must be hidden
		// for a block to qualify, so fail before the remaining five lookups.
		BlockPos.MutableBlockPos neighborPos = wateroptimisation$neighborPosition.get();
		BlockState blockStateUp = level.getBlockState(neighborPos.setWithOffset(pos, Direction.UP));
		if (!FluidOptimizationPolicy.isOrdinarySourceWater(blockStateUp)) {
			return;
		}

		// Probe one direction at a time and stop at the first rejection. This
		// preserves the same all-six-source-water proof without paying for the
		// remaining lookups after an open side is found. The block-only predicate
		// also avoids extracting a fluid state for non-water blocks.
		if (!FluidOptimizationPolicy.isOrdinarySourceWater(
				level.getBlockState(neighborPos.setWithOffset(pos, Direction.DOWN)))) {
			return;
		}
		if (!FluidOptimizationPolicy.isOrdinarySourceWater(
				level.getBlockState(neighborPos.setWithOffset(pos, Direction.NORTH)))) {
			return;
		}
		if (!FluidOptimizationPolicy.isOrdinarySourceWater(
				level.getBlockState(neighborPos.setWithOffset(pos, Direction.SOUTH)))) {
			return;
		}
		if (!FluidOptimizationPolicy.isOrdinarySourceWater(
				level.getBlockState(neighborPos.setWithOffset(pos, Direction.WEST)))) {
			return;
		}
		if (!FluidOptimizationPolicy.isOrdinarySourceWater(
				level.getBlockState(neighborPos.setWithOffset(pos, Direction.EAST)))) {
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
