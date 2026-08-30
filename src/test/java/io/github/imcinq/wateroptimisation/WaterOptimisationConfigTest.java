package io.github.imcinq.wateroptimisation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WaterOptimisationConfigTest {
	@Test
	void defaultsAreSafeAndBalanced() {
		WaterOptimisationConfig config = WaterOptimisationConfig.defaults();

		assertFalse(config.isEnabled());
		assertEquals(WaterOptimisationConfig.PerformanceProfile.BALANCED, config.getPerformanceProfile());
		assertEquals(WaterOptimisationConfig.FluidCullingMode.CONSERVATIVE, config.getFluidCullingMode());
		assertFalse(config.isFlatWaterFastPath());
		assertTrue(config.isWaterParticles());
		assertEquals(32, config.getParticleDistance());
		assertFalse(config.isParticleFogCulling());
	}

	@Test
	void profileResetKeepsNonVanillaProfilesOptIn() {
		WaterOptimisationConfig config = WaterOptimisationConfig.defaults();
		config.setEnabled(true);

		config.selectProfile(WaterOptimisationConfig.PerformanceProfile.BALANCED);
		assertTrue(config.isEnabled());
		assertFalse(config.isFlatWaterFastPath());
		assertEquals(WaterOptimisationConfig.FluidCullingMode.CONSERVATIVE, config.getFluidCullingMode());

		config.selectProfile(WaterOptimisationConfig.PerformanceProfile.PERFORMANCE);
		assertTrue(config.isEnabled());
		assertTrue(config.isFlatWaterFastPath());
		assertFalse(config.isWaterParticles());
		assertEquals(16, config.getParticleDistance());
		assertTrue(config.isParticleFogCulling());

		config.selectProfile(WaterOptimisationConfig.PerformanceProfile.MAXIMUM);
		assertTrue(config.isEnabled());
		assertTrue(config.isFlatWaterFastPath());
		assertEquals(WaterOptimisationConfig.FluidCullingMode.EXPERIMENTAL, config.getFluidCullingMode());
		assertFalse(config.isWaterParticles());
	}

	@Test
	void vanillaProfileDisablesTheMasterSwitch() {
		WaterOptimisationConfig config = WaterOptimisationConfig.defaults();
		config.setEnabled(true);
		config.selectProfile(WaterOptimisationConfig.PerformanceProfile.VANILLA);

		assertFalse(config.isEnabled());
		assertEquals(WaterOptimisationConfig.FluidCullingMode.DISABLED, config.getFluidCullingMode());
		assertFalse(config.isFlatWaterFastPath());
	}

	@Test
	void invalidValuesRecoverToSafeBounds() {
		WaterOptimisationConfig config = WaterOptimisationConfig.defaults();
		config.setParticleDistance(-100);
		assertEquals(WaterOptimisationConfig.MIN_PARTICLE_DISTANCE, config.getParticleDistance());

		config.setParticleDistance(1000);
		assertEquals(WaterOptimisationConfig.MAX_PARTICLE_DISTANCE, config.getParticleDistance());

		config.setPerformanceProfile(null);
		config.setFluidCullingMode(null);
		config.sanitize();
		assertEquals(WaterOptimisationConfig.PerformanceProfile.BALANCED, config.getPerformanceProfile());
		assertEquals(WaterOptimisationConfig.FluidCullingMode.CONSERVATIVE, config.getFluidCullingMode());
	}

	@Test
	void particleDistancePolicyUsesCameraReferenceAndEuclideanBounds() {
		WaterOptimisationConfig config = WaterOptimisationConfig.defaults();

		assertTrue(WaterParticleDistancePolicy.isWithinDistance(config, 10.0D, 20.0D, 30.0D, 42.0D, 20.0D, 30.0D));
		assertFalse(WaterParticleDistancePolicy.isWithinDistance(config, 10.0D, 20.0D, 30.0D, 42.001D, 20.0D, 30.0D));
		assertTrue(WaterParticleDistancePolicy.isWithinDistance(config, 10.0D, 20.0D, 30.0D, 10.0D, 20.0D, 62.0D));
		assertTrue(WaterParticleDistancePolicy.isWithinDistanceSquared(32.0D * 32.0D, 10.0D, 20.0D, 30.0D, 42.0D, 20.0D, 30.0D));
		assertFalse(WaterParticleDistancePolicy.isWithinDistanceSquared(32.0D * 32.0D, 10.0D, 20.0D, 30.0D, 42.001D, 20.0D, 30.0D));
	}

	@Test
	void particleFogPolicyTightensTheAdmissionDistance() {
		WaterOptimisationConfig config = WaterOptimisationConfig.defaults();
		config.setParticleFogCulling(true);

		assertEquals(24.0D, WaterParticleDistancePolicy.effectiveDistance(config), 0.000001D);
		assertTrue(WaterParticleDistancePolicy.isWithinDistance(config, 0.0D, 0.0D, 0.0D, 24.0D, 0.0D, 0.0D));
		assertFalse(WaterParticleDistancePolicy.isWithinDistance(config, 0.0D, 0.0D, 0.0D, 24.001D, 0.0D, 0.0D));
	}

	@Test
	void copiesAreIndependent() {
		WaterOptimisationConfig original = WaterOptimisationConfig.defaults();
		WaterOptimisationConfig copy = original.copy();

		assertNotSame(original, copy);
		copy.setEnabled(true);
		copy.setParticleDistance(64);

		assertFalse(original.isEnabled());
		assertEquals(32, original.getParticleDistance());
	}

	@Test
	void cosmeticSettingsDoNotRequireFluidSectionRefresh() {
		WaterOptimisationConfig original = WaterOptimisationConfig.defaults();
		WaterOptimisationConfig changed = original.copy();
		changed.setWaterParticles(false);
		changed.setParticleDistance(64);
		changed.setParticleFogCulling(true);
		changed.setDiagnosticsHud(true);
		changed.setDebugFallbackLogging(true);

		assertTrue(original.sameFluidRenderingConfiguration(changed));
	}

	@Test
	void fluidSettingsRequireFluidSectionRefresh() {
		WaterOptimisationConfig original = WaterOptimisationConfig.defaults();
		WaterOptimisationConfig changed = original.copy();
		changed.setFlatWaterFastPath(true);

		assertFalse(original.sameFluidRenderingConfiguration(changed));
	}

	@Test
	void experimentalFluidModeRequiresFluidSectionRefresh() {
		WaterOptimisationConfig original = WaterOptimisationConfig.defaults();
		WaterOptimisationConfig changed = original.copy();
		changed.setFluidCullingMode(WaterOptimisationConfig.FluidCullingMode.EXPERIMENTAL);

		assertFalse(original.sameFluidRenderingConfiguration(changed));
	}
}
