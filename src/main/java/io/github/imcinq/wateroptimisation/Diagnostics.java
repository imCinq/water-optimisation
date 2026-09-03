package io.github.imcinq.wateroptimisation;

import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.LongAdder;

public final class Diagnostics {
	private static final int FLUID_TIMING_SAMPLE_MASK = 0x0F;
	private static final AtomicReference<Counters> ACTIVE_COUNTERS = new AtomicReference<>(new Counters());
	private static final ThreadLocal<FluidTiming> FLUID_TIMING = ThreadLocal.withInitial(FluidTiming::new);
	private static final ThreadLocal<PhaseTiming> PHASE_TIMING = ThreadLocal.withInitial(PhaseTiming::new);
	private static volatile boolean instrumentationEnabled;

	private Diagnostics() {
	}

	/**
	 * Refreshes the cached instrumentation gate after the active configuration changes.
	 * This keeps disabled diagnostics out of the fluid-rendering hot path.
	 */
	public static void updateConfig(WaterOptimisationConfig config) {
		instrumentationEnabled = config != null && config.isDiagnosticsHud();
	}

	public static boolean isEnabled() {
		return instrumentationEnabled;
	}

	/**
	 * Starts a new counter generation. Work already in progress keeps its old
	 * counter set, so it cannot appear in the next snapshot after a reset.
	 */
	public static void reset() {
		ACTIVE_COUNTERS.set(new Counters());
	}

	public static void recordFluidFastPathSkip() {
		Counters counters = activeCounters();
		if (counters != null) {
			counters.fluidFastPathSkips.increment();
		}
	}

	public static void recordReducedWaterBackface() {
		Counters counters = activeCounters();
		if (counters != null) {
			counters.reducedWaterBackfaces.increment();
		}
	}

	public static void recordFluidFace(boolean reverseFaceRequested) {
		Counters counters = activeCounters();
		if (counters == null) {
			return;
		}
		counters.fluidFaces.increment();
		if (reverseFaceRequested) {
			counters.fluidReverseFaceRequests.increment();
		}
	}

	public static void recordParticleCandidate() {
		Counters counters = activeCounters();
		if (counters != null) {
			counters.particleCandidates.increment();
		}
	}

	public static void recordParticleRejected(boolean distance) {
		Counters counters = activeCounters();
		if (counters == null) {
			return;
		}
		counters.particleRejected.increment();
		if (distance) {
			counters.particleDistanceRejected.increment();
		}
	}

	public static void recordParticleForcedPreserved() {
		Counters counters = activeCounters();
		if (counters != null) {
			counters.particleForcedPreserved.increment();
		}
	}

	public static void recordParticleBudgetRejected() {
		Counters counters = activeCounters();
		if (counters == null) {
			return;
		}
		counters.particleBudgetRejected.increment();
		counters.particleRejected.increment();
	}

	/**
	 * Records a visited fluid block and samples one in sixteen tessellations for
	 * timing. Sampling keeps the optional diagnostics HUD from adding two clock
	 * reads to every fluid block while retaining a representative average.
	 */
	public static void beginFluidCompile() {
		FluidTiming timing = FLUID_TIMING.get();
		// A diagnostics toggle can skip the previous return hook. Clear the prior
		// invocation before deciding whether this one is sampled, so an unsampled
		// call can never close a stale sample from before the toggle.
		timing.active = false;
		timing.counters = null;
		Counters counters = activeCounters();
		if (counters == null) {
			return;
		}
		counters.fluidBlocksVisited.increment();
		if ((timing.sampleIndex++ & FLUID_TIMING_SAMPLE_MASK) != 0) {
			return;
		}
		timing.startNanos = System.nanoTime();
		timing.counters = counters;
		timing.active = true;
	}

	public static void endFluidCompile() {
		FluidTiming timing = FLUID_TIMING.get();
		if (!timing.active) {
			return;
		}
		timing.active = false;
		Counters counters = timing.counters;
		timing.counters = null;
		if (counters == null) {
			return;
		}
		counters.fluidCompileCalls.increment();
		counters.fluidCompileNanos.add(Math.max(0L, System.nanoTime() - timing.startNanos));
	}

	public static void beginSectionCompile() {
		Counters counters = activeCounters();
		if (counters == null) {
			return;
		}
		PhaseTiming timing = PHASE_TIMING.get();
		timing.sectionCompileStartNanos = System.nanoTime();
		timing.sectionCompileCounters = counters;
		timing.sectionCompileActive = true;
	}

	public static void endSectionCompile() {
		PhaseTiming timing = PHASE_TIMING.get();
		if (!timing.sectionCompileActive) {
			return;
		}
		timing.sectionCompileActive = false;
		Counters counters = timing.sectionCompileCounters;
		timing.sectionCompileCounters = null;
		if (counters == null) {
			return;
		}
		counters.sectionCompileCalls.increment();
		counters.sectionCompileNanos.add(Math.max(0L, System.nanoTime() - timing.sectionCompileStartNanos));
	}

	public static void beginTranslucentResort() {
		Counters counters = activeCounters();
		if (counters == null) {
			return;
		}
		PhaseTiming timing = PHASE_TIMING.get();
		timing.translucentResortStartNanos = System.nanoTime();
		timing.translucentResortCounters = counters;
		timing.translucentResortActive = true;
	}

	public static void endTranslucentResort() {
		PhaseTiming timing = PHASE_TIMING.get();
		if (!timing.translucentResortActive) {
			return;
		}
		timing.translucentResortActive = false;
		Counters counters = timing.translucentResortCounters;
		timing.translucentResortCounters = null;
		if (counters == null) {
			return;
		}
		counters.translucentResortCalls.increment();
		counters.translucentResortNanos.add(Math.max(0L, System.nanoTime() - timing.translucentResortStartNanos));
	}

	public static Snapshot snapshot() {
		Counters counters = ACTIVE_COUNTERS.get();
		long fluidCalls = counters.fluidCompileCalls.sum();
		long fluidNanos = counters.fluidCompileNanos.sum();
		long sectionCalls = counters.sectionCompileCalls.sum();
		long sectionNanos = counters.sectionCompileNanos.sum();
		long resortCalls = counters.translucentResortCalls.sum();
		long resortNanos = counters.translucentResortNanos.sum();
		return new Snapshot(
				counters.fluidBlocksVisited.sum(),
				counters.fluidFastPathSkips.sum(),
				counters.reducedWaterBackfaces.sum(),
				counters.fluidFaces.sum(),
				counters.fluidReverseFaceRequests.sum(),
				counters.particleCandidates.sum(),
				counters.particleRejected.sum(),
				counters.particleDistanceRejected.sum(),
				counters.particleForcedPreserved.sum(),
				counters.particleBudgetRejected.sum(),
				fluidCalls,
				fluidNanos,
				averageMillis(fluidCalls, fluidNanos),
				sectionCalls,
				sectionNanos,
				averageMillis(sectionCalls, sectionNanos),
				resortCalls,
				resortNanos,
				averageMillis(resortCalls, resortNanos)
		);
	}

	private static double averageMillis(long calls, long nanos) {
		return calls == 0 ? 0.0 : nanos / 1_000_000.0 / calls;
	}

	private static Counters activeCounters() {
		return instrumentationEnabled ? ACTIVE_COUNTERS.get() : null;
	}

	private static final class Counters {
		private final LongAdder fluidBlocksVisited = new LongAdder();
		private final LongAdder fluidFastPathSkips = new LongAdder();
		private final LongAdder reducedWaterBackfaces = new LongAdder();
		private final LongAdder fluidFaces = new LongAdder();
		private final LongAdder fluidReverseFaceRequests = new LongAdder();
		private final LongAdder particleCandidates = new LongAdder();
		private final LongAdder particleRejected = new LongAdder();
		private final LongAdder particleDistanceRejected = new LongAdder();
		private final LongAdder particleForcedPreserved = new LongAdder();
		private final LongAdder particleBudgetRejected = new LongAdder();
		private final LongAdder fluidCompileCalls = new LongAdder();
		private final LongAdder fluidCompileNanos = new LongAdder();
		private final LongAdder sectionCompileCalls = new LongAdder();
		private final LongAdder sectionCompileNanos = new LongAdder();
		private final LongAdder translucentResortCalls = new LongAdder();
		private final LongAdder translucentResortNanos = new LongAdder();
	}

	private static final class FluidTiming {
		private int sampleIndex;
		private long startNanos;
		private Counters counters;
		private boolean active;
	}

	private static final class PhaseTiming {
		private long sectionCompileStartNanos;
		private Counters sectionCompileCounters;
		private boolean sectionCompileActive;
		private long translucentResortStartNanos;
		private Counters translucentResortCounters;
		private boolean translucentResortActive;
	}

	public record Snapshot(
			long fluidBlocksVisited,
			long fluidFastPathSkips,
			long reducedWaterBackfaces,
			long fluidFaces,
			long fluidReverseFaceRequests,
			long particleCandidates,
			long particleRejected,
			long particleDistanceRejected,
			long particleForcedPreserved,
			long particleBudgetRejected,
			long fluidCompileCalls,
			long fluidCompileNanos,
			double averageFluidCompileMillis,
			long sectionCompileCalls,
			long sectionCompileNanos,
			double averageSectionCompileMillis,
			long translucentResortCalls,
			long translucentResortNanos,
			double averageTranslucentResortMillis
	) {
	}
}
