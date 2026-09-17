package io.github.imcinq.wateroptimisation;

import org.junit.jupiter.api.Test;
import java.util.concurrent.Executors;
import static org.junit.jupiter.api.Assertions.*;

class TessellationContextTest {
	@Test
	void normalAndCancelledReturnsClearState() {
		TessellationContext context = new TessellationContext();
		for (boolean cancelled : new boolean[]{false, true}) {
			runInvocation(context, cancelled);
			assertFalse(context.isOrdinaryWater());
			assertNull(context.enter(false));
			context.exit(null);
		}
	}

	private static void runInvocation(TessellationContext context, boolean cancelled) {
		Boolean previous = context.enter(true);
		try {
			assertTrue(context.isOrdinaryWater());
			if (cancelled) return;
		} finally {
			context.exit(previous);
		}
	}

	@Test
	void exceptionalExitClearsState() {
		TessellationContext context = new TessellationContext();
		assertThrows(IllegalStateException.class, () -> {
			Boolean previous = context.enter(true);
			try {
				throw new IllegalStateException("test");
			} finally {
				context.exit(previous);
			}
		});
		assertFalse(context.isOrdinaryWater());
		assertNull(context.enter(false));
		context.exit(null);
	}

	@Test
	void nestedNonWaterInvocationRestoresOuterEligibility() {
		TessellationContext context = new TessellationContext();
		Boolean outer = context.enter(true);
		try {
			Boolean inner = context.enter(false);
			try {
				assertFalse(context.isOrdinaryWater());
			} finally {
				context.exit(inner);
			}
			assertTrue(context.isOrdinaryWater());
		} finally {
			context.exit(outer);
		}
		assertFalse(context.isOrdinaryWater());
	}

	@Test
	void workersCannotSeeEachOthersState() throws Exception {
		TessellationContext context = new TessellationContext();
		Boolean outer = context.enter(true);
		try (var executor = Executors.newSingleThreadExecutor()) {
			assertFalse(executor.submit(context::isOrdinaryWater).get());
			assertTrue(context.isOrdinaryWater());
		} finally {
			context.exit(outer);
		}
	}
}
