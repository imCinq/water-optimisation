package io.github.imcinq.wateroptimisation;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class FluidOptimizationPolicy {
	private static volatile boolean fluidHooksActive;
	private static volatile boolean flatWaterFastPathActive;
	private static volatile boolean reducedWaterBackfacesActive;

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
		reducedWaterBackfacesActive = hooksActive
				&& config.getFluidCullingMode() == WaterOptimisationConfig.FluidCullingMode.EXPERIMENTAL;
	}

	public static boolean fluidHooksActive() {
		return fluidHooksActive;
	}

	public static boolean flatWaterFastPathActive() {
		return flatWaterFastPathActive;
	}

	/**
	 * The experimental mode removes only vanilla's optional reverse face for a
	 * fluid quad. It is deliberately disabled for Sodium, which owns its fluid
	 * renderer, and is never enabled by a safe preset.
	 */
	public static boolean reducedWaterBackfacesActive() {
		return reducedWaterBackfacesActive;
	}

	/**
	 * Skips only a complete source-water cube whose six neighboring block and
	 * fluid states are also ordinary full water blocks. The caller supplies the
	 * states already loaded by vanilla, so this predicate does not repeat chunk
	 * lookups. Any boundary, flow, waterlogged block, overlay, or unusual
	 * transparency case falls back to vanilla tessellation.
	 */
	public static boolean shouldSkipInteriorSourceWater(
			BlockState blockState,
			FluidState fluidState,
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
			FluidState fluidStateEast
	) {
		if (!isOrdinarySourceWater(blockState, fluidState)
				|| !isOrdinarySourceWater(blockStateUp, fluidStateUp)) {
			return false;
		}

		return isOrdinarySourceWater(blockStateDown, fluidStateDown)
				&& isOrdinarySourceWater(blockStateNorth, fluidStateNorth)
				&& isOrdinarySourceWater(blockStateSouth, fluidStateSouth)
				&& isOrdinarySourceWater(blockStateWest, fluidStateWest)
				&& isOrdinarySourceWater(blockStateEast, fluidStateEast);
	}

	private static boolean isOrdinarySourceWater(BlockState blockState, FluidState fluidState) {
		return blockState.is(Blocks.WATER)
				&& fluidState.getType() == Fluids.WATER
				&& fluidState.isSource();
	}
}
