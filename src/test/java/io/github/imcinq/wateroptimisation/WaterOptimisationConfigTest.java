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
		assertEquals(48, config.getParticleDistance());
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
	void copiesAreIndependent() {
		WaterOptimisationConfig original = WaterOptimisationConfig.defaults();
		WaterOptimisationConfig copy = original.copy();

		assertNotSame(original, copy);
		copy.setEnabled(true);
		copy.setParticleDistance(64);

		assertFalse(original.isEnabled());
		assertEquals(32, original.getParticleDistance());
	}
}
