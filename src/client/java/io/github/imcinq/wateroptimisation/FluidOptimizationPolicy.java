package io.github.imcinq.wateroptimisation;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import java.util.concurrent.atomic.AtomicLong;

public final class FluidOptimizationPolicy {
	private static volatile boolean fluidHooksActive;
	private static volatile boolean flatWaterFastPathActive;
	private static volatile boolean reducedWaterBackfacesActive;
	private static final long OBSERVATION_ARMED = 1L;
	private static final long OBSERVATION_OBSERVED = 1L << 1;
	// The low two bits are flags; the remaining bits identify the refresh generation.
	// Keeping them in one atomic word prevents a hook from mutating a newer reset.
	private static final int OBSERVATION_GENERATION_SHIFT = 2;
	private static final AtomicLong flatWaterFastPathObservationState = new AtomicLong();

	private FluidOptimizationPolicy() {
	}

	/**
	 * Refreshes the cached active flags after configuration or renderer ownership changes.
	 * The mixin hot paths then avoid repeated configuration reads for every fluid block/face.
	 */
	public static void refresh() {
		WaterOptimisationConfig config = ConfigManager.get();
		EffectiveWaterPolicy policy = WaterOptimisationClient.effectivePolicy(config);
		fluidHooksActive = policy.fluidHooksActive();
		flatWaterFastPathActive = policy.flatWaterFastPathActive();
		reducedWaterBackfacesActive = policy.reducedWaterBackfacesActive();
		resetFlatWaterFastPathObservation(flatWaterFastPathActive && Diagnostics.isEnabled());
	}

	public static boolean fluidHooksActive() {
		return fluidHooksActive;
	}

	public static boolean flatWaterFastPathActive() {
		return flatWaterFastPathActive;
	}

	/** Records that the optional local-capture fast-path hook actually ran. */
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
			// Advance the generation even when the new state is not armed so an
			// in-flight hook from the previous configuration cannot be reused.
			long generation = currentState >>> OBSERVATION_GENERATION_SHIFT;
			nextState = (generation + 1) << OBSERVATION_GENERATION_SHIFT;
			if (armed) {
				nextState |= OBSERVATION_ARMED;
			}
		} while (!flatWaterFastPathObservationState.compareAndSet(currentState, nextState));
	}

	/**
	 * The experimental mode removes only vanilla's optional reverse face for a
	 * fluid quad. Sodium owns its fluid renderer, so the vanilla hook never runs
	 * there and the experimental mode is unavailable while Sodium is present.
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

	/**
	 * Checks a fetched block state without extracting a fluid state for non-water
	 * blocks. This keeps the target-shared policy API aligned with the 1.21.1
	 * compatibility probe; the 26.2 renderer continues to use captured fluids.
	 */
	public static boolean isOrdinarySourceWater(BlockState blockState) {
		if (!blockState.is(Blocks.WATER)) {
			return false;
		}
		FluidState fluidState = blockState.getFluidState();
		return fluidState.getType() == Fluids.WATER && fluidState.isSource();
	}

	private static boolean hidesFluidFace(BlockState blockState, FluidState fluidState) {
		// Source-water neighbors are common in the only dense case this probe
		// can skip. Test that cheap identity/type path before asking a solid
		// block state for its render shape.
		return isOrdinarySourceWater(blockState, fluidState) || blockState.isSolidRender();
	}
}
