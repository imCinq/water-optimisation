package io.github.imcinq.wateroptimisation;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class FluidOptimizationPolicy {
	private static final ThreadLocal<BlockPos.MutableBlockPos> NEIGHBOR_POS = ThreadLocal.withInitial(BlockPos.MutableBlockPos::new);

	private FluidOptimizationPolicy() {
	}

	public static boolean isSodiumLoaded() {
		return FabricLoader.getInstance().isModLoaded("sodium");
	}

	public static boolean fluidHooksActive() {
		WaterOptimisationConfig config = ConfigManager.get();
		return config.isEnabled()
				&& config.getPerformanceProfile() != WaterOptimisationConfig.PerformanceProfile.VANILLA
				&& config.getFluidCullingMode() != WaterOptimisationConfig.FluidCullingMode.DISABLED
				&& !isSodiumLoaded();
	}

	public static boolean flatWaterFastPathActive() {
		WaterOptimisationConfig config = ConfigManager.get();
		return fluidHooksActive() && config.isFlatWaterFastPath();
	}

	public static boolean shouldSkipInteriorSourceWater(BlockAndTintGetter level, BlockPos pos, BlockState blockState, FluidState fluidState) {
		if (!flatWaterFastPathActive()
				|| fluidState.getType() != Fluids.WATER
				|| !fluidState.isSource()
				|| !blockState.is(Blocks.WATER)) {
			return false;
		}

		BlockPos.MutableBlockPos neighbor = NEIGHBOR_POS.get();
		for (Direction direction : Direction.values()) {
			if (direction == Direction.DOWN || direction == Direction.UP
					|| direction == Direction.NORTH || direction == Direction.SOUTH
					|| direction == Direction.WEST || direction == Direction.EAST) {
				neighbor.setWithOffset(pos, direction);
				BlockState neighborState = level.getBlockState(neighbor);
				FluidState neighborFluid = level.getFluidState(neighbor);
				if (!neighborState.is(Blocks.WATER)
						|| neighborFluid.getType() != Fluids.WATER
						|| !neighborFluid.isSource()) {
					return false;
				}
			}
		}
		return true;
	}

	public static Boolean overrideFaceDecision(
			FluidState fluidState,
			BlockState selfState,
			Direction direction,
			BlockState otherState
	) {
		if (!fluidHooksActive()
				|| fluidState.getType() != Fluids.WATER
				|| !fluidState.isSource()
				|| !selfState.is(Blocks.WATER)
				|| !otherState.is(Blocks.WATER)) {
			return null;
		}

		FluidState otherFluid = otherState.getFluidState();
		if (otherFluid.getType() == Fluids.WATER && otherFluid.isSource()) {
			return Boolean.FALSE;
		}
		return null;
	}
}
