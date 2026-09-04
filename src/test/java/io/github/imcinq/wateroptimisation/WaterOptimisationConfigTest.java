package io.github.imcinq.wateroptimisation;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
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
		assertEquals(WaterOptimisationConfig.CURRENT_CONFIG_VERSION, config.getConfigVersion());
		assertEquals(WaterOptimisationConfig.UNLIMITED_PARTICLE_BUDGET, config.getParticleBudget());
		assertFalse(config.isLimitForcedWaterParticles());
	}

	@Test
	void selectingNonVanillaProfileActivatesThePreset() {
		WaterOptimisationConfig config = WaterOptimisationConfig.defaults();

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
		assertEquals(128, config.getParticleBudget());
		assertFalse(config.isLimitForcedWaterParticles());

		config.selectProfile(WaterOptimisationConfig.PerformanceProfile.MAXIMUM);
		assertTrue(config.isEnabled());
		assertTrue(config.isFlatWaterFastPath());
		assertEquals(WaterOptimisationConfig.FluidCullingMode.EXPERIMENTAL, config.getFluidCullingMode());
		assertFalse(config.isWaterParticles());
		assertEquals(64, config.getParticleBudget());
		assertTrue(config.isLimitForcedWaterParticles());
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
		config.setParticleBudget(63);
		config.sanitize();
		assertEquals(WaterOptimisationConfig.PerformanceProfile.BALANCED, config.getPerformanceProfile());
		assertEquals(WaterOptimisationConfig.FluidCullingMode.CONSERVATIVE, config.getFluidCullingMode());
		assertEquals(WaterOptimisationConfig.UNLIMITED_PARTICLE_BUDGET, config.getParticleBudget());
	}

	@Test
	void migrationAddsVersionWithoutOverwritingExplicitEnabledState() {
		WaterOptimisationConfig config = WaterOptimisationConfig.defaults();
		config.setEnabled(false);
		config.migrateFrom(0, true);

		assertFalse(config.isEnabled());
		assertEquals(WaterOptimisationConfig.CURRENT_CONFIG_VERSION, config.getConfigVersion());

		config.migrateFrom(0, false);
		assertTrue(config.isEnabled());
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
	void unsampledFluidCallDoesNotCloseStaleSampleAfterDiagnosticsToggle() {
		WaterOptimisationConfig enabled = WaterOptimisationConfig.defaults();
		enabled.setDiagnosticsHud(true);
		WaterOptimisationConfig disabled = enabled.copy();
		disabled.setDiagnosticsHud(false);

		Diagnostics.updateConfig(enabled);
		Diagnostics.endFluidCompile();
		Diagnostics.reset();
		try {
			Diagnostics.beginFluidCompile();
			Diagnostics.updateConfig(disabled);
			Diagnostics.updateConfig(enabled);

			// The first call was sampled; the next call is unsampled. It must not
			// finish the sample that crossed the diagnostics toggle.
			Diagnostics.beginFluidCompile();
			Diagnostics.endFluidCompile();

			assertEquals(0, Diagnostics.snapshot().fluidCompileCalls());
		} finally {
			Diagnostics.endFluidCompile();
			Diagnostics.updateConfig(disabled);
			Diagnostics.reset();
		}
	}

	@Test
	void fluidRecordsStayWithTheInvocationGenerationAfterReset() {
		WaterOptimisationConfig enabled = WaterOptimisationConfig.defaults();
		enabled.setDiagnosticsHud(true);

		Diagnostics.updateConfig(enabled);
		Diagnostics.reset();
		try {
			Diagnostics.beginFluidCompile();
			Diagnostics.recordFluidFastPathSkip();
			Diagnostics.recordFluidFace(true);
			Diagnostics.recordReducedWaterBackface();

			// A reset must not move records from this in-flight invocation into the
			// newly visible generation, including sampled timing completion.
			Diagnostics.reset();
			Diagnostics.endFluidCompile();

			Diagnostics.Snapshot snapshot = Diagnostics.snapshot();
			assertEquals(0, snapshot.fluidBlocksVisited());
			assertEquals(0, snapshot.fluidFastPathSkips());
			assertEquals(0, snapshot.fluidFaces());
			assertEquals(0, snapshot.fluidReverseFaceRequests());
			assertEquals(0, snapshot.reducedWaterBackfaces());
			assertEquals(0, snapshot.fluidCompileCalls());
		} finally {
			Diagnostics.endFluidCompile();
			Diagnostics.updateConfig(WaterOptimisationConfig.defaults());
			Diagnostics.reset();
		}
	}

	@Test
	void hiddenWaterPredicateFailsOpenForEveryUnsafeNeighborDirection() {
		BlockState source = Blocks.WATER.defaultBlockState();
		FluidState sourceFluid = source.getFluidState();

		assertTrue(FluidOptimizationPolicy.shouldSkipInteriorSourceWater(
				source,
				sourceFluid,
				source,
				sourceFluid,
				source,
				sourceFluid,
				source,
				sourceFluid,
				source,
				sourceFluid,
				source,
				sourceFluid,
				source,
				sourceFluid
		));

		assertFastPathRejectsForEveryNeighbor(source, sourceFluid, Blocks.AIR.defaultBlockState(), Blocks.AIR.defaultBlockState().getFluidState());
		assertFastPathRejectsForEveryNeighbor(source, sourceFluid, source, Fluids.FLOWING_WATER.defaultFluidState());
		assertFastPathRejectsForEveryNeighbor(source, sourceFluid, Blocks.GLASS.defaultBlockState(), Blocks.GLASS.defaultBlockState().getFluidState());
		assertFastPathRejectsForEveryNeighbor(source, sourceFluid, Blocks.OAK_SLAB.defaultBlockState(), Blocks.OAK_SLAB.defaultBlockState().getFluidState());

		BlockState waterloggedStairs = Blocks.OAK_STAIRS.defaultBlockState()
				.setValue(BlockStateProperties.WATERLOGGED, true);
		assertFastPathRejectsForEveryNeighbor(source, sourceFluid, waterloggedStairs, waterloggedStairs.getFluidState());
	}

	private static void assertFastPathRejectsForEveryNeighbor(
			BlockState source,
			FluidState sourceFluid,
			BlockState unsafeState,
			FluidState unsafeFluid
	) {
		for (int unsafeIndex = 0; unsafeIndex < 6; unsafeIndex++) {
			BlockState[] states = {source, source, source, source, source, source};
			FluidState[] fluids = {sourceFluid, sourceFluid, sourceFluid, sourceFluid, sourceFluid, sourceFluid};
			states[unsafeIndex] = unsafeState;
			fluids[unsafeIndex] = unsafeFluid;

			assertFalse(FluidOptimizationPolicy.shouldSkipInteriorSourceWater(
					source,
					sourceFluid,
					states[0],
					fluids[0],
					states[1],
					fluids[1],
					states[2],
					fluids[2],
					states[3],
					fluids[3],
					states[4],
					fluids[4],
					states[5],
					fluids[5]
			), "unsafe neighbor index " + unsafeIndex);
		}
	}

	@Test
	void experimentalFluidModeRequiresFluidSectionRefresh() {
		WaterOptimisationConfig original = WaterOptimisationConfig.defaults();
		WaterOptimisationConfig changed = original.copy();
		changed.setFluidCullingMode(WaterOptimisationConfig.FluidCullingMode.EXPERIMENTAL);

		assertFalse(original.sameFluidRenderingConfiguration(changed));
	}

	@Test
	void effectivePolicyFailsClosedPerRendererCapability() {
		WaterOptimisationConfig config = WaterOptimisationConfig.defaults();
		config.selectProfile(WaterOptimisationConfig.PerformanceProfile.PERFORMANCE);

		EffectiveWaterPolicy vanilla = EffectiveWaterPolicy.resolve(config, RendererCapabilities.vanilla());
		assertTrue(vanilla.flatWaterFastPathActive());
		assertEquals(EffectiveWaterPolicy.GeometryPath.HIDDEN_WATER_COMPILE, vanilla.geometryPath());

		RendererCapabilities sodium = new RendererCapabilities(true, "Sodium", false);
		EffectiveWaterPolicy sodiumPolicy = EffectiveWaterPolicy.resolve(config, sodium);
		assertFalse(sodiumPolicy.fluidHooksActive());
		assertEquals(EffectiveWaterPolicy.GeometryPath.SODIUM_PARTICLES, sodiumPolicy.geometryPath());
	}

	@Test
	void effectivePolicyKeepsSodiumGeometryRendererOwned() {
		WaterOptimisationConfig config = WaterOptimisationConfig.defaults();
		config.selectProfile(WaterOptimisationConfig.PerformanceProfile.MAXIMUM);

		EffectiveWaterPolicy sodium = EffectiveWaterPolicy.resolve(
				config,
				new RendererCapabilities(true, "Sodium", false)
		);
		assertFalse(sodium.fluidHooksActive());
		assertFalse(sodium.reducedWaterBackfacesActive());
		assertEquals(EffectiveWaterPolicy.GeometryPath.SODIUM_PARTICLES, sodium.geometryPath());

		EffectiveWaterPolicy vanilla = EffectiveWaterPolicy.resolve(
				config,
				new RendererCapabilities(false, "Vanilla 26.2", true)
		);
		assertTrue(vanilla.reducedWaterBackfacesActive());
		assertEquals(EffectiveWaterPolicy.GeometryPath.REDUCED_INWARD_FACES, vanilla.geometryPath());

		EffectiveWaterPolicy legacy = EffectiveWaterPolicy.resolve(
				config,
				new RendererCapabilities(false, "Vanilla 1.21.1", false)
		);
		assertFalse(legacy.reducedWaterBackfacesActive());
		assertEquals(EffectiveWaterPolicy.GeometryPath.HIDDEN_WATER_COMPILE, legacy.geometryPath());
	}

}
