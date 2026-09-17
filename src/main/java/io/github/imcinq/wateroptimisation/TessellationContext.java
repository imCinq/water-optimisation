package io.github.imcinq.wateroptimisation;

/** Per-worker, nested-invocation-safe eligibility; callers must exit in finally. */
public final class TessellationContext {
	private final ThreadLocal<Boolean> ordinaryWater = new ThreadLocal<>();

	public Boolean enter(boolean eligible) {
		Boolean previous = ordinaryWater.get();
		ordinaryWater.set(eligible);
		return previous;
	}

	public boolean isOrdinaryWater() {
		return Boolean.TRUE.equals(ordinaryWater.get());
	}

	public void exit(Boolean previous) {
		if (previous == null) {
			ordinaryWater.remove();
		} else {
			ordinaryWater.set(previous);
		}
	}
}
