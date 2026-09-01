package io.github.imcinq.wateroptimisation;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public final class FluidOptimizationPolicy {
	private static volatile boolean fluidHooksActive;
	private static volatile boolean flatWaterFastPathActive;
	private static volatile boolean flatWaterSurfaceMeshingActive;

	private FluidOptimizationPolicy() {
	}

	/**
	 * 1.21.1 has a different fluid renderer and no reviewed Sodium geometry
	 * adapter. Keep this target on the conservative vanilla path and let the
	 * 26.2 source variant own the optional reverse-face experiment.
	 */
	public static void refresh() {
		WaterOptimisationConfig config = ConfigManager.get();
		EffectiveWaterPolicy policy = WaterOptimisationClient.effectivePolicy(config);
		fluidHooksActive = policy.fluidHooksActive();
		flatWaterFastPathActive = policy.flatWaterFastPathActive();
		flatWaterSurfaceMeshingActive = policy.flatWaterSurfaceMeshingActive();
	}

	public static boolean fluidHooksActive() {
		return fluidHooksActive;
	}

	public static boolean flatWaterFastPathActive() {
		return flatWaterFastPathActive;
	}

	public static boolean flatWaterSurfaceMeshingActive() {
		return flatWaterSurfaceMeshingActive;
	}

	public static boolean reducedWaterBackfacesActive() {
		return false;
	}

	public static boolean reducedWaterBackfacesRequested() {
		WaterOptimisationConfig config = ConfigManager.get();
		return config.isEnabled()
				&& config.getPerformanceProfile() != WaterOptimisationConfig.PerformanceProfile.VANILLA
				&& config.getFluidCullingMode() == WaterOptimisationConfig.FluidCullingMode.EXPERIMENTAL;
	}

	/**
	 * Identifies the only fluid shape eligible for the conservative fast path.
	 * Flowing water and waterlogged states deliberately stay vanilla.
	 */
	public static boolean isOrdinarySourceWater(BlockState blockState, FluidState fluidState) {
		return blockState.is(Blocks.WATER)
				&& fluidState.getType() == Fluids.WATER
				&& fluidState.isSource();
	}

	/**
	 * The 1.21.1 renderer does not expose the already-read neighbor locals in a
	 * stable public hook, so this compatibility adapter performs the conservative
	 * six-neighbor check only after the cheap ordinary-source-water test. Any
	 * boundary, flowing state, waterlogged block, overlay, or unusual shape stays
	 * on vanilla tessellation.
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
		return hidesFluidFace(blockStateUp, fluidStateUp)
				&& hidesFluidFace(blockStateDown, fluidStateDown)
				&& hidesFluidFace(blockStateNorth, fluidStateNorth)
				&& hidesFluidFace(blockStateSouth, fluidStateSouth)
				&& hidesFluidFace(blockStateWest, fluidStateWest)
				&& hidesFluidFace(blockStateEast, fluidStateEast);
	}

	private static boolean hidesFluidFace(BlockState blockState, FluidState fluidState) {
		// The 1.21.1 solid-render query requires a level and position. Do not
		// manufacture context here: source-water neighbors are sufficient for
		// the enclosed-water proof, while every other case stays vanilla.
		return isOrdinarySourceWater(blockState, fluidState);
	}
}
