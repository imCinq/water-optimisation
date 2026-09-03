package io.github.imcinq.wateroptimisation;

/**
 * Resolves requested settings plus renderer capabilities into the work that
 * is actually allowed to run. Keeping this decision pure makes the UI and
 * diagnostics truthful before a setting is applied and keeps compatibility
 * fallbacks explicit.
 */
public record EffectiveWaterPolicy(
		GeometryPath geometryPath,
		boolean fluidHooksActive,
		boolean flatWaterFastPathActive,
		boolean reducedWaterBackfacesActive,
		boolean particleFilteringActive,
		int particleBudget,
		boolean limitForcedWaterParticles
) {
	public enum GeometryPath {
		DISABLED("wateroptimisation.path.disabled"),
		VANILLA_PARTICLES("wateroptimisation.path.vanilla_particles"),
		HIDDEN_WATER_COMPILE("wateroptimisation.path.hidden_compile"),
		REDUCED_INWARD_FACES("wateroptimisation.path.reduced_faces"),
		SODIUM_PARTICLES("wateroptimisation.path.sodium_particles");

		private final String translationKey;

		GeometryPath(String translationKey) {
			this.translationKey = translationKey;
		}

		public String translationKey() {
			return this.translationKey;
		}
	}

	public static EffectiveWaterPolicy resolve(WaterOptimisationConfig config, RendererCapabilities capabilities) {
		if (config == null) {
			return disabled();
		}

		RendererCapabilities safeCapabilities = capabilities == null ? RendererCapabilities.vanilla() : capabilities;
		boolean requested = config.isEnabled()
				&& config.getPerformanceProfile() != WaterOptimisationConfig.PerformanceProfile.VANILLA;
		if (!requested) {
			return disabled();
		}

		boolean particleFilteringActive = true;
		int particleBudget = config.getParticleBudget();
		boolean limitForced = config.isLimitForcedWaterParticles();
		boolean fluidHooksActive = config.getFluidCullingMode() != WaterOptimisationConfig.FluidCullingMode.DISABLED
				&& !safeCapabilities.sodiumLoaded();
		boolean flatWaterFastPathActive = fluidHooksActive && config.isFlatWaterFastPath();
		boolean reducedBackfacesActive = !safeCapabilities.sodiumLoaded()
				&& config.getFluidCullingMode() == WaterOptimisationConfig.FluidCullingMode.EXPERIMENTAL;

		GeometryPath path;
		if (safeCapabilities.sodiumLoaded()) {
			path = GeometryPath.SODIUM_PARTICLES;
		} else if (reducedBackfacesActive) {
			path = GeometryPath.REDUCED_INWARD_FACES;
		} else if (flatWaterFastPathActive) {
			path = GeometryPath.HIDDEN_WATER_COMPILE;
		} else {
			path = GeometryPath.VANILLA_PARTICLES;
		}

		return new EffectiveWaterPolicy(
				path,
				fluidHooksActive,
				flatWaterFastPathActive,
				reducedBackfacesActive,
				particleFilteringActive,
				particleBudget,
				limitForced
		);
	}

	private static EffectiveWaterPolicy disabled() {
		return new EffectiveWaterPolicy(
				GeometryPath.DISABLED,
				false,
				false,
				false,
				false,
				WaterOptimisationConfig.UNLIMITED_PARTICLE_BUDGET,
				false
		);
	}
}
