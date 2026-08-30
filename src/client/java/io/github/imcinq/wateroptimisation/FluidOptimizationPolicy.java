package io.github.imcinq.wateroptimisation;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class FluidOptimizationPolicy {
	/*
	 * Check the open-facing side first. Most surface water fails here, so the
	 * conservative probe returns before visiting the remaining neighbors.
	 */
	private static final Direction[] DIRECTIONS = {
			Direction.UP,
			Direction.DOWN,
			Direction.NORTH,
			Direction.SOUTH,
			Direction.WEST,
			Direction.EAST
	};
	private static final ThreadLocal<BlockPos.MutableBlockPos> NEIGHBOR_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);
	private static volatile boolean fluidHooksActive;
	private static volatile boolean flatWaterFastPathActive;

	private FluidOptimizationPolicy() {
	}

	/**
	 * Refreshes the cached active flags after configuration or renderer ownership changes.
	 * The mixin hot paths then avoid repeated configuration reads for every fluid block/face.
	 */
	public static void refresh() {
		WaterOptimisationConfig config = ConfigManager.get();
		boolean hooksActive = config.isEnabled()
				&& config.getPerformanceProfile() != WaterOptimisationConfig.PerformanceProfile.VANILLA
				&& config.getFluidCullingMode() != WaterOptimisationConfig.FluidCullingMode.DISABLED
				&& !WaterOptimisationClient.isSodiumLoaded();
		fluidHooksActive = hooksActive;
		flatWaterFastPathActive = hooksActive && config.isFlatWaterFastPath();
	}

	public static boolean fluidHooksActive() {
		return fluidHooksActive;
	}

	public static boolean flatWaterFastPathActive() {
		return flatWaterFastPathActive;
	}

	/**
	 * Skips only a complete source-water cube whose six neighboring block states
	 * are also ordinary full water blocks. Any boundary, flow, waterlogged block,
	 * overlay, or unusual transparency case falls back to vanilla tessellation.
	 */
	public static boolean shouldSkipInteriorSourceWater(BlockAndTintGetter level, BlockPos pos, BlockState blockState, FluidState fluidState) {
		if (!flatWaterFastPathActive
				|| fluidState.getType() != Fluids.WATER
				|| !fluidState.isSource()
				|| !blockState.is(Blocks.WATER)) {
			return false;
		}

		BlockPos.MutableBlockPos neighbor = NEIGHBOR_POS.get();
		for (Direction direction : DIRECTIONS) {
			neighbor.setWithOffset(pos, direction);
			BlockState neighborState = level.getBlockState(neighbor);
			if (!neighborState.is(Blocks.WATER)) {
				return false;
			}

			/*
			 * The ordinary water block state already owns the fluid state needed by
			 * this exact predicate. Reusing it avoids a second region lookup and
			 * keeps non-water neighbors on the fast failure path.
			 */
			FluidState neighborFluid = neighborState.getFluidState();
			if (neighborFluid.getType() != Fluids.WATER || !neighborFluid.isSource()) {
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
		if (!fluidHooksActive
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
