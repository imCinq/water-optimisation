package io.github.imcinq.wateroptimisation;

/**
 * Shared, renderer-independent rules used by every settings-screen adapter.
 * Keeping these decisions out of the native GUI classes prevents Fabric and
 * NeoForge from presenting different meanings for the same saved fields.
 */
public final class SettingsPresentation {
	private SettingsPresentation() {
	}

	public static boolean isCustom(WaterOptimisationConfig config) {
		if (config == null) {
			return false;
		}

		WaterOptimisationConfig profile = config.copy();
		profile.sanitize();
		profile.resetToProfile();
		return config.isWaterParticles() != profile.isWaterParticles()
				|| config.getParticleDistance() != profile.getParticleDistance()
				|| config.isParticleFogCulling() != profile.isParticleFogCulling()
				|| config.getParticleBudget() != profile.getParticleBudget()
				|| config.isLimitForcedWaterParticles() != profile.isLimitForcedWaterParticles()
				|| config.isDiagnosticsHud() != profile.isDiagnosticsHud()
				|| config.getFluidCullingMode() != profile.getFluidCullingMode()
				|| config.isFlatWaterFastPath() != profile.isFlatWaterFastPath();
	}

	public static String profileTranslationKey(WaterOptimisationConfig config) {
		if (isCustom(config)) {
			return "wateroptimisation.profile.custom";
		}
		return config == null || config.getPerformanceProfile() == null
				? WaterOptimisationConfig.PerformanceProfile.BALANCED.translationKey()
				: config.getPerformanceProfile().translationKey();
	}

	public static boolean hiddenWaterSkipping(WaterOptimisationConfig config) {
		return config != null && config.isFlatWaterFastPath();
	}

	public static boolean reducedInwardFaces(WaterOptimisationConfig config) {
		return config != null
				&& config.getFluidCullingMode() == WaterOptimisationConfig.FluidCullingMode.EXPERIMENTAL;
	}

	public static void setHiddenWaterSkipping(WaterOptimisationConfig config, boolean enabled) {
		if (config == null) {
			return;
		}
		config.setFlatWaterFastPath(enabled);
		if (enabled && config.getFluidCullingMode() == WaterOptimisationConfig.FluidCullingMode.DISABLED) {
			config.setFluidCullingMode(WaterOptimisationConfig.FluidCullingMode.CONSERVATIVE);
		}
	}

	public static void setReducedInwardFaces(WaterOptimisationConfig config, boolean enabled) {
		if (config == null) {
			return;
		}
		config.setFluidCullingMode(enabled
				? WaterOptimisationConfig.FluidCullingMode.EXPERIMENTAL
				: (config.isFlatWaterFastPath()
						? WaterOptimisationConfig.FluidCullingMode.CONSERVATIVE
						: WaterOptimisationConfig.FluidCullingMode.DISABLED));
	}
}
