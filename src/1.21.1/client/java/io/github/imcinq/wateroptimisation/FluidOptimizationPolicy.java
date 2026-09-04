package io.github.imcinq.wateroptimisation;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.concurrent.atomic.AtomicLong;

public final class FluidOptimizationPolicy {
	private static volatile boolean fluidHooksActive;
	private static volatile boolean flatWaterFastPathActive;
	private static final long OBSERVATION_ARMED = 1L;
	private static final long OBSERVATION_OBSERVED = 1L << 1;
	private static final int OBSERVATION_GENERATION_SHIFT = 2;
	private static final AtomicLong flatWaterFastPathObservationState = new AtomicLong();

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
		resetFlatWaterFastPathObservation(flatWaterFastPathActive && Diagnostics.isEnabled());
	}

	public static boolean fluidHooksActive() {
		return fluidHooksActive;
	}

	public static boolean flatWaterFastPathActive() {
		return flatWaterFastPathActive;
	}

	/** Records that the optional compatibility fast-path hook actually ran. */
	public static void markFlatWaterFastPathHookObserved() {
		long currentState;
		do {
			currentState = flatWaterFastPathObservationState.get();
			if ((currentState & OBSERVATION_ARMED) == 0
					|| (currentState & OBSERVATION_OBSERVED) != 0) {
				return;
			}
		} while (!flatWaterFastPathObservationState.compareAndSet(
				currentState,
				currentState | OBSERVATION_OBSERVED
		));

		// Clear only this generation's armed bit. If refresh() installed a newer
		// generation meanwhile, the CAS fails and leaves that new state intact.
		long observedState = currentState | OBSERVATION_OBSERVED;
		flatWaterFastPathObservationState.compareAndSet(
				observedState,
				observedState & ~OBSERVATION_ARMED
		);
	}

	/** Returns whether a diagnostics-only first-hook observation is still needed. */
	public static boolean flatWaterFastPathObservationActive() {
		return (flatWaterFastPathObservationState.get() & OBSERVATION_ARMED) != 0;
	}

	public static boolean flatWaterFastPathHookObserved() {
		return (flatWaterFastPathObservationState.get() & OBSERVATION_OBSERVED) != 0;
	}

	private static void resetFlatWaterFastPathObservation(boolean armed) {
		long currentState;
		long nextState;
		do {
			currentState = flatWaterFastPathObservationState.get();
			long generation = currentState >>> OBSERVATION_GENERATION_SHIFT;
			nextState = (generation + 1) << OBSERVATION_GENERATION_SHIFT;
			if (armed) {
				nextState |= OBSERVATION_ARMED;
			}
		} while (!flatWaterFastPathObservationState.compareAndSet(currentState, nextState));
	}

	public static boolean reducedWaterBackfacesActive() {
		return false;
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

	/**
	 * Completes the already-gated 1.21.1 probe after the center and upward
	 * neighbors have passed their cheap early checks. Keeping that ordering in
	 * the renderer hook avoids five extra block reads for open-surface water.
	 */
	public static boolean areRemainingNeighborsOrdinarySourceWater(
			BlockState blockStateDown,
			FluidState fluidStateDown,
			BlockState blockStateNorth,
			FluidState fluidStateNorth,
			BlockState blockStateSouth,
			FluidState fluidStateSouth,
			BlockState blockStateWest,
			FluidState fluidStateWest,
			BlockState blockStateEast,
			FluidState fluidStateEast
	) {
		return isOrdinarySourceWater(blockStateDown, fluidStateDown)
				&& isOrdinarySourceWater(blockStateNorth, fluidStateNorth)
				&& isOrdinarySourceWater(blockStateSouth, fluidStateSouth)
				&& isOrdinarySourceWater(blockStateWest, fluidStateWest)
				&& isOrdinarySourceWater(blockStateEast, fluidStateEast);
	}

	private static boolean hidesFluidFace(BlockState blockState, FluidState fluidState) {
		// The 1.21.1 solid-render query requires a level and position. Do not
		// manufacture context here: source-water neighbors are sufficient for
		// the enclosed-water proof, while every other case stays vanilla.
		return isOrdinarySourceWater(blockState, fluidState);
	}
}
