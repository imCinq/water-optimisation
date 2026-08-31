package io.github.imcinq.wateroptimisation;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;

/**
 * Finds a deliberately small, conservative still-water surface patch.
 *
 * <p>This is a retained 26.2-only prototype and is currently capability-gated
 * off because one atlas quad cannot preserve vanilla's repeated water texture.
 * It never crosses a render section, never
 * crosses a biome/light change, and requires a full source-water ring and a
 * source-water block below every cell. The ring is what lets the renderer
 * cancel the other fifteen block tessellations without losing a side face.</p>
 */
public final class FlatWaterSurfacePolicy {
	private static final int PATCH_SIZE = 4;
	private static volatile boolean surfaceGeometryHookReady;
	private static volatile boolean surfaceGeometryHookRejected;

	private FlatWaterSurfacePolicy() {
	}

	public static Decision prepare(
			BlockAndTintGetter level,
			BlockPos pos,
			BlockState blockState,
			FluidState fluidState
	) {
		if (!FluidOptimizationPolicy.flatWaterSurfaceMeshingActive()
				|| !isEligibleSurfaceCell(level, pos, blockState, fluidState)) {
			return Decision.NONE;
		}

		int anchorX = pos.getX() & ~3;
		int anchorZ = pos.getZ() & ~3;
		// A patch must stay inside the current 16x16 render section. This also
		// keeps the addFace coordinate expansion local to one section.
		if ((anchorX & 15) > 12 || (anchorZ & 15) > 12) {
			return Decision.NONE;
		}

		int y = pos.getY();
		int anchorTint = BiomeColors.getAverageWaterColor(level, new BlockPos(anchorX, y, anchorZ));
		int anchorSkyLight = level.getBrightness(LightLayer.SKY, new BlockPos(anchorX, y, anchorZ));
		int anchorBlockLight = level.getBrightness(LightLayer.BLOCK, new BlockPos(anchorX, y, anchorZ));

		for (int dx = 0; dx < PATCH_SIZE; dx++) {
			for (int dz = 0; dz < PATCH_SIZE; dz++) {
				BlockPos cell = new BlockPos(anchorX + dx, y, anchorZ + dz);
				if (!isEligibleSurfaceCell(level, cell, level.getBlockState(cell), level.getFluidState(cell))
						|| !matchesLightingAndTint(level, cell, anchorTint, anchorSkyLight, anchorBlockLight)) {
					return Decision.NONE;
				}
			}
		}

		// Keep one source-water cell around the patch. Without this ring, a
		// canceled cell could own a visible side face at the patch boundary.
		for (int dx = -1; dx <= PATCH_SIZE; dx++) {
			for (int dz = -1; dz <= PATCH_SIZE; dz++) {
				if (dx >= 0 && dx < PATCH_SIZE && dz >= 0 && dz < PATCH_SIZE) {
					continue;
				}
				BlockPos ringCell = new BlockPos(anchorX + dx, y, anchorZ + dz);
				if (!isOrdinarySourceWater(level.getBlockState(ringCell), level.getFluidState(ringCell))) {
					return Decision.NONE;
				}
			}
		}

		Patch patch = new Patch(anchorX, y, anchorZ, PATCH_SIZE, PATCH_SIZE);
		boolean cancelCurrentBlock = pos.getX() != anchorX || pos.getZ() != anchorZ;
		if (cancelCurrentBlock && !isSurfaceGeometryHookReady()) {
			// Do not cancel any block until the first owner has proved that the
			// reviewed addFace modifier actually matched. A missing or changed
			// mixin then produces vanilla geometry instead of a hole in the pool.
			return Decision.NONE;
		}
		return new Decision(patch, cancelCurrentBlock);
	}

	public static boolean isSurfaceGeometryHookReady() {
		return surfaceGeometryHookReady && !surfaceGeometryHookRejected;
	}

	public static void markSurfaceGeometryApplied() {
		if (!surfaceGeometryHookRejected) {
			surfaceGeometryHookReady = true;
		}
	}

	public static void rejectSurfaceGeometryHook() {
		surfaceGeometryHookRejected = true;
		surfaceGeometryHookReady = false;
	}

	private static boolean isEligibleSurfaceCell(
			BlockAndTintGetter level,
			BlockPos pos,
			BlockState blockState,
			FluidState fluidState
	) {
		if (!isOrdinarySourceWater(blockState, fluidState)
				|| !level.getBlockState(pos.above()).isAir()) {
			return false;
		}

		// The block below must also be source water. This prevents the merged
		// surface from erasing pool walls, bottoms, or other visible geometry.
		BlockPos below = pos.below();
		return isOrdinarySourceWater(level.getBlockState(below), level.getFluidState(below));
	}

	private static boolean isOrdinarySourceWater(BlockState blockState, FluidState fluidState) {
		return blockState.is(Blocks.WATER)
				&& FluidOptimizationPolicy.isOrdinarySourceWater(blockState, fluidState);
	}

	private static boolean matchesLightingAndTint(
			BlockAndTintGetter level,
			BlockPos pos,
			int tint,
			int skyLight,
			int blockLight
	) {
		return BiomeColors.getAverageWaterColor(level, pos) == tint
				&& level.getBrightness(LightLayer.SKY, pos) == skyLight
				&& level.getBrightness(LightLayer.BLOCK, pos) == blockLight;
	}

	public record Decision(Patch patch, boolean cancelCurrentBlock) {
		private static final Decision NONE = new Decision(null, false);
	}

	public record Patch(int anchorX, int anchorY, int anchorZ, int width, int depth) {
	}
}
