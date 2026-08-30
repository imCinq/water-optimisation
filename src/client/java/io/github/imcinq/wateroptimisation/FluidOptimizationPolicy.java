package io.github.imcinq.wateroptimisation;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class FluidOptimizationPolicy {
	private static final Direction[] DIRECTIONS = {
			Direction.DOWN,
			Direction.UP,
			Direction.NORTH,
			Direction.SOUTH,
			Direction.WEST,
			Direction.EAST
	};
	private static final ThreadLocal<BlockPos.MutableBlockPos> NEIGHBOR_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

	private FluidOptimizationPolicy() {
	}

	public static boolean fluidHooksActive() {
		WaterOptimisationConfig config = ConfigManager.get();
		return config.isEnabled()
				&& config.getPerformanceProfile() != WaterOptimisationConfig.PerformanceProfile.VANILLA
				&& config.getFluidCullingMode() != WaterOptimisationConfig.FluidCullingMode.DISABLED
				&& !WaterOptimisationClient.isSodiumLoaded();
	}

	public static boolean flatWaterFastPathActive() {
		WaterOptimisationConfig config = ConfigManager.get();
		return fluidHooksActive() && config.isFlatWaterFastPath();
	}

	/**
	 * Skips only a complete source-water cube whose six neighboring block states
	 * are also ordinary full water blocks. Any boundary, flow, waterlogged block,
	 * overlay, or unusual transparency case falls back to vanilla tessellation.
	 */
	public static boolean shouldSkipInteriorSourceWater(BlockAndTintGetter level, BlockPos pos, BlockState blockState, FluidState fluidState) {
		if (!flatWaterFastPathActive()
				|| fluidState.getType() != Fluids.WATER
				|| !fluidState.isSource()
				|| !blockState.is(Blocks.WATER)) {
			return false;
		}

		BlockPos.MutableBlockPos neighbor = NEIGHBOR_POS.get();
		for (Direction direction : DIRECTIONS) {
			neighbor.setWithOffset(pos, direction);
			BlockState neighborState = level.getBlockState(neighbor);
			FluidState neighborFluid = level.getFluidState(neighbor);
			if (!neighborState.is(Blocks.WATER)
					|| neighborFluid.getType() != Fluids.WATER
					|| !neighborFluid.isSource()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The face override is intentionally limited to equal full source-water
	 * states, which is the information available to vanilla Fabric's face
	 * predicate. Partial levels and other fluid types remain on vanilla logic.
	 */
	public static Boolean overrideFaceDecision(
			FluidState fluidState,
			BlockState selfState,
			Direction direction,
			FluidState otherFluidState
	) {
		if (!fluidHooksActive()
				|| fluidState.getType() != Fluids.WATER
				|| !fluidState.isSource()
				|| !selfState.is(Blocks.WATER)
				|| otherFluidState.getType() != Fluids.WATER
				|| !otherFluidState.isSource()) {
			return null;
		}

		return Boolean.FALSE;
	}
}
