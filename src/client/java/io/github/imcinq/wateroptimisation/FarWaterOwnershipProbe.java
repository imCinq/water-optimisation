package io.github.imcinq.wateroptimisation;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

/**
 * Observes the exact fluid geometry that could become water-owned later.
 *
 * <p>This is deliberately a probe rather than a render pass. It runs only
 * while diagnostics are enabled, records no mesh data, and never suppresses
 * vanilla output. That keeps the first ownership step cheap and reversible
 * while exposing whether ordinary source-water geometry is present in the
 * shared translucent section buffer.</p>
 */
public final class FarWaterOwnershipProbe {
	private static final ThreadLocal<SectionCapture> SECTION = new ThreadLocal<>();
	private static final ThreadLocal<FluidCapture> FLUID = new ThreadLocal<>();

	private FarWaterOwnershipProbe() {
	}

	public static void beginSection() {
		FLUID.remove();
		if (!Diagnostics.isEnabled()) {
			SECTION.remove();
			return;
		}
		SECTION.set(new SectionCapture());
	}

	public static WaterSectionOwnership endSection() {
		FLUID.remove();
		SectionCapture capture = SECTION.get();
		SECTION.remove();
		if (capture == null) {
			return WaterSectionOwnership.EMPTY;
		}
		WaterSectionOwnership ownership = capture.finish();
		ownership.publishDiagnostics();
		return ownership;
	}

	public static void beginFluid(BlockState blockState, FluidState fluidState) {
		if (!Diagnostics.isEnabled()) {
			FLUID.remove();
			return;
		}
		FLUID.set(new FluidCapture(
				fluidState.getType() == Fluids.WATER,
				FluidOptimizationPolicy.isOrdinarySourceWater(blockState, fluidState)
		));
	}

	public static void recordFace(boolean reverseFaceRequested) {
		FluidCapture capture = FLUID.get();
		if (capture == null) {
			return;
		}
		capture.faces++;
		capture.vertices += reverseFaceRequested ? 8 : 4;
	}

	public static void endFluid() {
		FluidCapture capture = FLUID.get();
		FLUID.remove();
		SectionCapture section = SECTION.get();
		if (capture == null || section == null) {
			return;
		}
		if (capture.ordinarySourceWater && capture.faces > 0) {
			section.candidateBlocks++;
			section.candidateFaces += capture.faces;
			section.candidateVertices += capture.vertices;
		} else if (capture.water) {
			section.fallbackBlocks++;
		}
	}

	private static final class SectionCapture {
		private long candidateBlocks;
		private long candidateFaces;
		private long candidateVertices;
		private long fallbackBlocks;

		private SectionCapture() {
		}

		private WaterSectionOwnership finish() {
			return new WaterSectionOwnership(
					candidateBlocks,
					candidateFaces,
					candidateVertices,
					fallbackBlocks
			);
		}
	}

	private static final class FluidCapture {
		private final boolean water;
		private final boolean ordinarySourceWater;
		private int faces;
		private int vertices;

		private FluidCapture(boolean water, boolean ordinarySourceWater) {
			this.water = water;
			this.ordinarySourceWater = ordinarySourceWater;
		}
	}
}
