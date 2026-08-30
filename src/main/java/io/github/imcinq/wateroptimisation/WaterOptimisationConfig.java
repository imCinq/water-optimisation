package io.github.imcinq.wateroptimisation;

public final class WaterOptimisationConfig {
	public static final int MIN_PARTICLE_DISTANCE = 8;
	public static final int MAX_PARTICLE_DISTANCE = 128;

	public enum PerformanceProfile {
		VANILLA("wateroptimisation.profile.vanilla"),
		BALANCED("wateroptimisation.profile.balanced"),
		PERFORMANCE("wateroptimisation.profile.performance");

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
	private boolean diagnosticsHud;
	private boolean debugFallbackLogging;

	public WaterOptimisationConfig() {
		this.enabled = false;
		this.performanceProfile = PerformanceProfile.BALANCED;
		this.fluidCullingMode = FluidCullingMode.CONSERVATIVE;
		this.flatWaterFastPath = false;
		this.waterParticles = true;
		this.particleDistance = 32;
		this.particleFogCulling = false;
		this.diagnosticsHud = false;
		this.debugFallbackLogging = false;
	}

	public static WaterOptimisationConfig defaults() {
		return new WaterOptimisationConfig();
	}

	public WaterOptimisationConfig copy() {
		WaterOptimisationConfig copy = new WaterOptimisationConfig();
		copy.enabled = this.enabled;
		copy.performanceProfile = this.performanceProfile;
		copy.fluidCullingMode = this.fluidCullingMode;
		copy.flatWaterFastPath = this.flatWaterFastPath;
		copy.waterParticles = this.waterParticles;
		copy.particleDistance = this.particleDistance;
		copy.particleFogCulling = this.particleFogCulling;
		copy.diagnosticsHud = this.diagnosticsHud;
		copy.debugFallbackLogging = this.debugFallbackLogging;
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
		if (this.performanceProfile == null) {
			this.performanceProfile = PerformanceProfile.BALANCED;
		}
		if (this.fluidCullingMode == null) {
			this.fluidCullingMode = FluidCullingMode.CONSERVATIVE;
		}
		this.particleDistance = Math.max(MIN_PARTICLE_DISTANCE, Math.min(MAX_PARTICLE_DISTANCE, this.particleDistance));
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
				this.diagnosticsHud = false;
				this.debugFallbackLogging = false;
			}
			case BALANCED -> {
				this.fluidCullingMode = FluidCullingMode.CONSERVATIVE;
				this.flatWaterFastPath = false;
				this.waterParticles = true;
				this.particleDistance = 32;
				this.particleFogCulling = false;
				this.diagnosticsHud = false;
				this.debugFallbackLogging = false;
			}
			case PERFORMANCE -> {
				this.fluidCullingMode = FluidCullingMode.CONSERVATIVE;
				this.flatWaterFastPath = true;
				this.waterParticles = true;
				this.particleDistance = 24;
				this.particleFogCulling = false;
				this.diagnosticsHud = false;
				this.debugFallbackLogging = false;
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

	public boolean isDiagnosticsHud() {
		return this.diagnosticsHud;
	}

	public void setDiagnosticsHud(boolean diagnosticsHud) {
		this.diagnosticsHud = diagnosticsHud;
	}

	public boolean isDebugFallbackLogging() {
		return this.debugFallbackLogging;
	}

	public void setDebugFallbackLogging(boolean debugFallbackLogging) {
		this.debugFallbackLogging = debugFallbackLogging;
	}
}
