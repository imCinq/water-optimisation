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
	 * Skips only an ordinary source-water block whose six neighboring faces are
	 * already known to be hidden by another ordinary source-water block or a
	 * full solid-rendering block. The caller supplies the states already loaded
	 * by vanilla, so this predicate does not repeat chunk lookups. Any boundary,
	 * flowing state, waterlogged block, overlay, or unusual transparency case
	 * falls back to vanilla tessellation.
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
		if (!isOrdinarySourceWater(blockState, fluidState)) {
			return false;
		}

		// Surface water is the common case in oceans and open pools. Check the
		// upward face first so visible water fails before testing every neighbor.
		return hidesFluidFace(blockStateUp, fluidStateUp)
				&& hidesFluidFace(blockStateDown, fluidStateDown)
				&& hidesFluidFace(blockStateNorth, fluidStateNorth)
				&& hidesFluidFace(blockStateSouth, fluidStateSouth)
				&& hidesFluidFace(blockStateWest, fluidStateWest)
				&& hidesFluidFace(blockStateEast, fluidStateEast);
	}

	/**
	 * Identifies the only fluid shape eligible for the experimental reverse-face
	 * reduction. Flowing water and waterlogged states deliberately stay vanilla.
	 */
	public static boolean isOrdinarySourceWater(BlockState blockState, FluidState fluidState) {
		return blockState.is(Blocks.WATER)
				&& fluidState.getType() == Fluids.WATER
				&& fluidState.isSource();
	}

	private static boolean hidesFluidFace(BlockState blockState, FluidState fluidState) {
		return blockState.isSolidRender() || isOrdinarySourceWater(blockState, fluidState);
	}
}
