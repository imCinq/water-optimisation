package io.github.imcinq.wateroptimisation;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Small, fail-closed bridge for the optional Sodium fluid-renderer mixin.
 *
 * <p>Sodium already owns fluid visibility, height, lighting, and sorting. The
 * bridge only supplies the one distinct operation this mod can justify: an
 * opt-in removal of the renderer's reversed copy of an ordinary source-water
 * quad. No Sodium classes are referenced here, so the base mod remains
 * loadable without Sodium.</p>
 */
public final class SodiumFluidIntegration {
	private static final String SUPPORTED_SODIUM_VERSION_PREFIX = "0.9.";
	private static final String SUPPORTED_MINECRAFT_VERSION = "mc26.2";
	private static final ThreadLocal<Boolean> ORDINARY_SOURCE_WATER = new ThreadLocal<>();
	private static volatile boolean renderHookMatched;
	private static volatile boolean faceHookMatched;

	private SodiumFluidIntegration() {
	}

	/**
	 * Marks the exact Sodium render entry point as present and records whether
	 * this invocation is ordinary full source water. The optional mixin calls
	 * this before Sodium starts emitting quads.
	 */
	public static void beginRender(BlockState blockState) {
		renderHookMatched = true;
		ORDINARY_SOURCE_WATER.set(
				blockState != null
						&& FluidOptimizationPolicy.isOrdinarySourceWater(blockState, blockState.getFluidState())
						? Boolean.TRUE
						: Boolean.FALSE
		);
	}

	/** Clears the per-render eligibility marker. */
	public static void endRender() {
		ORDINARY_SOURCE_WATER.remove();
	}

	/** Marks the exact Sodium quad-writing entry point as present. */
	public static void markFaceHookMatched() {
		faceHookMatched = true;
	}

	/**
	 * Returns whether both optional hooks have actually matched at runtime.
	 * Missing classes, renamed methods, or incompatible Sodium builds therefore
	 * remain a particle-only fallback instead of receiving a guessed mixin.
	 */
	public static boolean geometryHooksAvailable() {
		return isSupportedVersion() && renderHookMatched && faceHookMatched;
	}

	/**
	 * The optional bridge is intentionally limited to the Sodium 0.9.x 26.2
	 * line whose renderer shape was reviewed. Other versions keep Sodium's
	 * renderer fully untouched until a matching bridge is reviewed.
	 */
	public static boolean isSupportedVersion() {
		if (!WaterOptimisationClient.isSodiumLoaded()) {
			return false;
		}
		return FabricLoader.getInstance()
				.getModContainer("sodium")
				.map(container -> {
					String version = container.getMetadata().getVersion().getFriendlyString();
					return version.startsWith(SUPPORTED_SODIUM_VERSION_PREFIX)
							&& version.contains(SUPPORTED_MINECRAFT_VERSION);
				})
				.orElse(false);
	}

	/**
	 * Suppresses only Sodium's reversed quad copy when the user selected the
	 * experimental mode. The outward call is never changed.
	 */
	public static boolean reduceReverseFace(boolean flip) {
		markFaceHookMatched();
		if (!flip
				|| !isSupportedVersion()
				|| !FluidOptimizationPolicy.reducedWaterBackfacesRequested()
				|| !Boolean.TRUE.equals(ORDINARY_SOURCE_WATER.get())) {
			return flip;
		}

		if (Diagnostics.isEnabled()) {
			Diagnostics.recordReducedWaterBackface();
		}
		return false;
	}
}
