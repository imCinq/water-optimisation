package io.github.imcinq.wateroptimisation;

import java.util.concurrent.atomic.LongAdder;

public final class Diagnostics {
	private static final LongAdder fluidBlocksVisited = new LongAdder();
	private static final LongAdder fluidFacesAccepted = new LongAdder();
	private static final LongAdder fluidFacesCulled = new LongAdder();
	private static final LongAdder fluidFaceOverrides = new LongAdder();
	private static final LongAdder fluidFastPathSkips = new LongAdder();
	private static final LongAdder fluidFallbacks = new LongAdder();
	private static final LongAdder particleCandidates = new LongAdder();
	private static final LongAdder particleRejected = new LongAdder();
	private static final LongAdder particleDistanceRejected = new LongAdder();
	private static final LongAdder fluidCompileCalls = new LongAdder();
	private static final LongAdder fluidCompileNanos = new LongAdder();
	private static final ThreadLocal<FluidTiming> FLUID_TIMING = ThreadLocal.withInitial(FluidTiming::new);

	private Diagnostics() {
	}

	public static void recordFluidBlock() {
		if (enabled()) {
			fluidBlocksVisited.increment();
		}
	}

	public static void recordFluidFace(boolean accepted) {
		if (!enabled()) {
			return;
		}
		if (accepted) {
			fluidFacesAccepted.increment();
		} else {
			fluidFacesCulled.increment();
		}
	}

	public static void recordFluidFaceOverride() {
		if (enabled()) {
			fluidFaceOverrides.increment();
		}
	}

	public static void recordFluidFastPathSkip() {
		if (enabled()) {
			fluidFastPathSkips.increment();
		}
	}

	public static void recordFluidFallback(String reason) {
		if (!enabled()) {
			return;
		}
		fluidFallbacks.increment();
		if (WaterOptimisationConfig.defaults().isDebugFallbackLogging() || ConfigManager.get().isDebugFallbackLogging()) {
			WaterOptimisationClient.LOGGER.debug("Fluid optimization fallback: {}", reason);
		}
	}

	public static void recordParticleCandidate() {
		if (enabled()) {
			particleCandidates.increment();
		}
	}

	public static void recordParticleRejected(boolean distance) {
		if (!enabled()) {
			particleRejected.increment();
			if (distance) {
				particleDistanceRejected.increment();
			}
		}
	}

	public static void beginFluidCompile() {
		if (!enabled()) {
			return;
		}
		FluidTiming timing = FLUID_TIMING.get();
		timing.startNanos = System.nanoTime();
		timing.active = true;
	}

	public static void endFluidCompile() {
		FluidTiming timing = FLUID_TIMING.get();
		if (!timing.active) {
			return;
		}
		timing.active = false;
		fluidCompileCalls.increment();
		fluidCompileNanos.add(Math.max(0L, System.nanoTime() - timing.startNanos));
	}

	public static Snapshot snapshot() {
		long calls = fluidCompileCalls.sum();
		long nanos = fluidCompileNanos.sum();
		return new Snapshot(
				fluidBlocksVisited.sum(),
				fluidFacesAccepted.sum(),
				fluidFacesCulled.sum(),
				fluidFaceOverrides.sum(),
				fluidFastPathSkips.sum(),
				fluidFallbacks.sum(),
				particleCandidates.sum(),
				particleRejected.sum(),
				particleDistanceRejected.sum(),
				calls,
				nanos,
				calls == 0 ? 0.0 : nanos / 1_000_000.0 / calls
		);
	}

	private static boolean enabled() {
		WaterOptimisationConfig config = ConfigManager.get();
		return config.isDiagnosticsHud() || config.isDebugFallbackLogging();
	}

	private static final class FluidTiming {
		private long startNanos;
		private boolean active;
	}

	public record Snapshot(
			long fluidBlocksVisited,
			long fluidFacesAccepted,
			long fluidFacesCulled,
			long fluidFaceOverrides,
			long fluidFastPathSkips,
			long fluidFallbacks,
			long particleCandidates,
			long particleRejected,
			long particleDistanceRejected,
			long fluidCompileCalls,
			long fluidCompileNanos,
			double averageFluidCompileMillis
	) {
	}
}
