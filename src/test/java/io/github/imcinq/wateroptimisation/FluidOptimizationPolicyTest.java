package io.github.imcinq.wateroptimisation;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FluidOptimizationPolicyTest {
	@BeforeAll
	static void bootstrap() {
		SharedConstants.tryDetectVersion();
		Bootstrap.bootStrap();
	}

	@Test
	void solidCeilingWithHiddenNeighborsDoesNotSkip() {
		StatePair solid = solidPair();
		assertFalse(shouldSkip(waterPair(), solid, solid));
	}

	@Test
	void sourceWaterAboveWithHiddenNeighborsSkips() {
		assertTrue(shouldSkip(waterPair(), waterPair(), solidPair()));
	}

	@Test
	void openWestSideDoesNotSkip() {
		StatePair west = solidPair();
		when(west.block().isSolidRender()).thenReturn(false);
		assertFalse(shouldSkip(waterPair(), waterPair(), west));
	}

	@Test
	void nonSourceSelfFluidDoesNotSkip() {
		StatePair self = waterPair();
		when(self.fluid().isSource()).thenReturn(false);
		assertFalse(shouldSkip(self, waterPair(), solidPair()));
	}

	@Test
	void nonWaterSelfBlockDoesNotSkip() {
		StatePair self = waterPair();
		when(self.block().is(Blocks.WATER)).thenReturn(false);
		assertFalse(shouldSkip(self, waterPair(), solidPair()));
	}

	private static boolean shouldSkip(StatePair self, StatePair above, StatePair west) {
		StatePair solid = solidPair();
		StatePair water = waterPair();
		return FluidOptimizationPolicy.shouldSkipInteriorSourceWater(
				self.block(), self.fluid(),
				solid.block(), solid.fluid(),
				above.block(), above.fluid(),
				water.block(), water.fluid(),
				solid.block(), solid.fluid(),
				west.block(), west.fluid(),
				water.block(), water.fluid());
	}

	private static StatePair waterPair() {
		BlockState block = mock(BlockState.class);
		when(block.is(any(Block.class))).thenReturn(false);
		when(block.is(eq(Blocks.WATER))).thenReturn(true);
		FluidState fluid = mock(FluidState.class);
		when(fluid.getType()).thenReturn(Fluids.WATER);
		when(fluid.isSource()).thenReturn(true);
		return new StatePair(block, fluid);
	}

	private static StatePair solidPair() {
		BlockState block = mock(BlockState.class);
		when(block.isSolidRender()).thenReturn(true);
		when(block.is(any(Block.class))).thenReturn(false);
		FluidState fluid = mock(FluidState.class);
		when(fluid.getType()).thenReturn(Fluids.EMPTY);
		return new StatePair(block, fluid);
	}

	private record StatePair(BlockState block, FluidState fluid) {
	}
}
