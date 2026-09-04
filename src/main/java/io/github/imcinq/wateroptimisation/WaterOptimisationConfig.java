package io.github.imcinq.wateroptimisation;

public final class WaterOptimisationConfig {
	public static final int CURRENT_CONFIG_VERSION = 4;
	public static final int MIN_PARTICLE_DISTANCE = 8;
	public static final int MAX_PARTICLE_DISTANCE = 128;
	public static final int UNLIMITED_PARTICLE_BUDGET = 0;
	public static final int[] PARTICLE_BUDGETS = {UNLIMITED_PARTICLE_BUDGET, 64, 128, 256};

	public enum PerformanceProfile {
		VANILLA("wateroptimisation.profile.vanilla"),
		BALANCED("wateroptimisation.profile.balanced"),
		PERFORMANCE("wateroptimisation.profile.performance"),
		MAXIMUM("wateroptimisation.profile.maximum");

		private final String translationKey;

		PerformanceProfile(String translationKey) {
			this.translationKey = translationKey;
		}

		public String translationKey() {
			return this.translationKey;
		}

		public PerformanceProfile next() {
			PerformanceProfile[] values = values();
			return values[(this.ordinal() + 1) % values.length];
		}
	}

	public enum FluidCullingMode {
		DISABLED("wateroptimisation.culling.disabled"),
		CONSERVATIVE("wateroptimisation.culling.conservative"),
		EXPERIMENTAL("wateroptimisation.culling.experimental");

		private final String translationKey;

		FluidCullingMode(String translationKey) {
			this.translationKey = translationKey;
		}

		public String translationKey() {
			return this.translationKey;
		}

		public FluidCullingMode next() {
			FluidCullingMode[] values = values();
			return values[(this.ordinal() + 1) % values.length];
		}
	}

	private boolean enabled;
	private PerformanceProfile performanceProfile;
	private FluidCullingMode fluidCullingMode;
	private boolean flatWaterFastPath;
	private boolean waterParticles;
	private int particleDistance;
	private boolean particleFogCulling;
	private int particleBudget;
	private boolean limitForcedWaterParticles;
	private boolean diagnosticsHud;
	private int configVersion;

	public WaterOptimisationConfig() {
		this.configVersion = CURRENT_CONFIG_VERSION;
		this.enabled = false;
		this.performanceProfile = PerformanceProfile.BALANCED;
		this.fluidCullingMode = FluidCullingMode.CONSERVATIVE;
		this.flatWaterFastPath = false;
		this.waterParticles = true;
		this.particleDistance = 32;
		this.particleFogCulling = false;
		this.particleBudget = UNLIMITED_PARTICLE_BUDGET;
		this.limitForcedWaterParticles = false;
		this.diagnosticsHud = false;
	}

	public static WaterOptimisationConfig defaults() {
		return new WaterOptimisationConfig();
	}

	public WaterOptimisationConfig copy() {
		WaterOptimisationConfig copy = new WaterOptimisationConfig();
		copy.configVersion = this.configVersion;
		copy.enabled = this.enabled;
		copy.performanceProfile = this.performanceProfile;
		copy.fluidCullingMode = this.fluidCullingMode;
		copy.flatWaterFastPath = this.flatWaterFastPath;
		copy.waterParticles = this.waterParticles;
		copy.particleDistance = this.particleDistance;
		copy.particleFogCulling = this.particleFogCulling;
		copy.particleBudget = this.particleBudget;
		copy.limitForcedWaterParticles = this.limitForcedWaterParticles;
		copy.diagnosticsHud = this.diagnosticsHud;
		return copy;
	}

	/**
	 * Returns whether changing to {@code other} can change compiled fluid geometry.
	 * Particle and diagnostics settings take effect without rebuilding sections.
	 */
	boolean sameFluidRenderingConfiguration(WaterOptimisationConfig other) {
		return other != null
				&& this.enabled == other.enabled
				&& this.performanceProfile == other.performanceProfile
				&& this.fluidCullingMode == other.fluidCullingMode
				&& this.flatWaterFastPath == other.flatWaterFastPath;
	}

	public void sanitize() {
		if (this.configVersion <= 0 || this.configVersion > CURRENT_CONFIG_VERSION) {
			this.configVersion = CURRENT_CONFIG_VERSION;
		}
		if (this.performanceProfile == null) {
			this.performanceProfile = PerformanceProfile.BALANCED;
		}
		if (this.fluidCullingMode == null) {
			this.fluidCullingMode = FluidCullingMode.CONSERVATIVE;
		}
		this.particleDistance = Math.max(MIN_PARTICLE_DISTANCE, Math.min(MAX_PARTICLE_DISTANCE, this.particleDistance));
		this.particleBudget = normalizeParticleBudget(this.particleBudget);
	}

	/**
	 * Upgrades an older JSON format without changing an explicit user choice.
	 * New fields intentionally keep their safe defaults when they were absent
	 * from an older file. A future-version file is left untouched; its caller
	 * must avoid rewriting the file until a compatible mod version is installed.
	 */
	void migrateFrom(int sourceVersion, boolean enabledWasPresent) {
		if (sourceVersion > CURRENT_CONFIG_VERSION) {
			return;
		}
		if (!enabledWasPresent) {
			this.enabled = this.performanceProfile != PerformanceProfile.VANILLA;
		}
		this.configVersion = CURRENT_CONFIG_VERSION;
		this.sanitize();
	}

	private static int normalizeParticleBudget(int budget) {
		for (int allowed : PARTICLE_BUDGETS) {
			if (budget == allowed) {
				return budget;
			}
		}
		return UNLIMITED_PARTICLE_BUDGET;
	}

	public static int nextParticleBudget(int current) {
		int normalized = normalizeParticleBudget(current);
		for (int index = 0; index < PARTICLE_BUDGETS.length; index++) {
			if (PARTICLE_BUDGETS[index] == normalized) {
				return PARTICLE_BUDGETS[(index + 1) % PARTICLE_BUDGETS.length];
			}
		}
		return UNLIMITED_PARTICLE_BUDGET;
	}

	public void resetToProfile() {
		switch (this.performanceProfile) {
			case VANILLA -> {
				this.enabled = false;
				this.fluidCullingMode = FluidCullingMode.DISABLED;
				this.flatWaterFastPath = false;
				this.waterParticles = true;
				this.particleDistance = 32;
				this.particleFogCulling = false;
				this.particleBudget = UNLIMITED_PARTICLE_BUDGET;
				this.limitForcedWaterParticles = false;
				this.diagnosticsHud = false;
			}
			case BALANCED -> {
				this.enabled = true;
				this.fluidCullingMode = FluidCullingMode.CONSERVATIVE;
				this.flatWaterFastPath = false;
				this.waterParticles = true;
				this.particleDistance = 32;
				this.particleFogCulling = false;
				this.particleBudget = UNLIMITED_PARTICLE_BUDGET;
				this.limitForcedWaterParticles = false;
				this.diagnosticsHud = false;
			}
			case PERFORMANCE -> {
				this.enabled = true;
				this.fluidCullingMode = FluidCullingMode.CONSERVATIVE;
				this.flatWaterFastPath = true;
				this.waterParticles = false;
				this.particleDistance = 16;
				this.particleFogCulling = true;
				this.particleBudget = 128;
				this.limitForcedWaterParticles = false;
				this.diagnosticsHud = false;
			}
			case MAXIMUM -> {
				this.enabled = true;
				this.fluidCullingMode = FluidCullingMode.EXPERIMENTAL;
				this.flatWaterFastPath = true;
				this.waterParticles = false;
				this.particleDistance = 16;
				this.particleFogCulling = true;
				this.particleBudget = 64;
				this.limitForcedWaterParticles = true;
				this.diagnosticsHud = false;
			}
		}
		this.sanitize();
	}

	public boolean isEnabled() {
		return this.enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public PerformanceProfile getPerformanceProfile() {
		return this.performanceProfile;
	}

	public void setPerformanceProfile(PerformanceProfile performanceProfile) {
		this.performanceProfile = performanceProfile == null ? PerformanceProfile.BALANCED : performanceProfile;
	}

	public void selectProfile(PerformanceProfile performanceProfile) {
		this.setPerformanceProfile(performanceProfile);
		this.resetToProfile();
	}

	public FluidCullingMode getFluidCullingMode() {
		return this.fluidCullingMode;
	}

	public void setFluidCullingMode(FluidCullingMode fluidCullingMode) {
		this.fluidCullingMode = fluidCullingMode == null ? FluidCullingMode.CONSERVATIVE : fluidCullingMode;
	}

	public boolean isFlatWaterFastPath() {
		return this.flatWaterFastPath;
	}

	public void setFlatWaterFastPath(boolean flatWaterFastPath) {
		this.flatWaterFastPath = flatWaterFastPath;
	}

	public boolean isWaterParticles() {
		return this.waterParticles;
	}

	public void setWaterParticles(boolean waterParticles) {
		this.waterParticles = waterParticles;
	}

	public int getParticleDistance() {
		return this.particleDistance;
	}

	public void setParticleDistance(int particleDistance) {
		this.particleDistance = particleDistance;
		this.sanitize();
	}

	public boolean isParticleFogCulling() {
		return this.particleFogCulling;
	}

	public void setParticleFogCulling(boolean particleFogCulling) {
		this.particleFogCulling = particleFogCulling;
	}

	public int getParticleBudget() {
		return this.particleBudget;
	}

	public void setParticleBudget(int particleBudget) {
		this.particleBudget = normalizeParticleBudget(particleBudget);
	}

	public boolean isLimitForcedWaterParticles() {
		return this.limitForcedWaterParticles;
	}

	public void setLimitForcedWaterParticles(boolean limitForcedWaterParticles) {
		this.limitForcedWaterParticles = limitForcedWaterParticles;
	}

	public boolean isDiagnosticsHud() {
		return this.diagnosticsHud;
	}

	public void setDiagnosticsHud(boolean diagnosticsHud) {
		this.diagnosticsHud = diagnosticsHud;
	}

	public int getConfigVersion() {
		return this.configVersion;
	}
}
