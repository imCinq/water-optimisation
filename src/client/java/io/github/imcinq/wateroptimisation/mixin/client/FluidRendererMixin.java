package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.Diagnostics;
import io.github.imcinq.wateroptimisation.FluidOptimizationPolicy;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.FluidRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(FluidRenderer.class)
public abstract class FluidRendererMixin {
	@Unique
	private static final ThreadLocal<Boolean> wateroptimisation$waterTessellation = new ThreadLocal<>();
	@Unique
	private static final ThreadLocal<FlatWaterSurfacePolicy.Patch> wateroptimisation$flatWaterPatch = new ThreadLocal<>();

	/**
	 * Vanilla's addFace method emits the outward face and, when requested, its
	 * reverse face from the same call. Change only the argument so the outward
	 * face remains intact when the experimental mode is enabled.
	 */
	@ModifyVariable(method = "addFace", at = @At("HEAD"), argsOnly = true, ordinal = 0)
	private boolean wateroptimisation$disableOptionalBackFace(boolean addBackFace) {
		if (Diagnostics.isEnabled()) {
			Diagnostics.recordFluidFace(addBackFace);
		}
		if (!addBackFace || !FluidOptimizationPolicy.reducedWaterBackfacesActive()) {
			return addBackFace;
		}
		if (!Boolean.TRUE.equals(wateroptimisation$waterTessellation.get())) {
			return addBackFace;
		}
		if (Diagnostics.isEnabled()) {
			Diagnostics.recordReducedWaterBackface();
		}
		return false;
	}

	/**
	 * Reuses the vanilla top-face material and lighting while expanding one
	 * ordinary one-block quad into a validated 4x4 surface patch. Vanilla emits
	 * the upward face first when the surface is visible, so this targets only the
	 * first addFace invocation inside tesselate. The exact descriptor and
	 * argument count are intentional: if Mojang changes the call shape, this
	 * prototype silently stays on the normal renderer.
	 */
	@ModifyArgs(
			method = "tesselate",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/block/FluidRenderer;addFace(Lcom/mojang/blaze3d/vertex/VertexConsumer;FFFFFFFFFFFFFFFFFFFFIIZ)V",
					ordinal = 0
			),
			require = 0
	)
	private void wateroptimisation$expandFlatWaterSurface(Args args) {
		FlatWaterSurfacePolicy.Patch patch = wateroptimisation$flatWaterPatch.get();
		if (patch == null) {
			return;
		}
		if (args.size() != 24) {
			FlatWaterSurfacePolicy.rejectSurfaceGeometryHook();
			return;
		}

		float[] x = new float[4];
		float[] y = new float[4];
		float[] z = new float[4];
		float[] u = new float[4];
		float[] v = new float[4];
		for (int vertex = 0; vertex < 4; vertex++) {
			int base = 1 + vertex * 5;
			x[vertex] = args.get(base);
			y[vertex] = args.get(base + 1);
			z[vertex] = args.get(base + 2);
			u[vertex] = args.get(base + 3);
			v[vertex] = args.get(base + 4);
		}

		float xMin = min(x);
		float xMax = max(x);
		float yMin = min(y);
		float yMax = max(y);
		float zMin = min(z);
		float zMax = max(z);
		if (yMax - yMin > 0.001F
				|| Math.abs((xMax - xMin) - 1.0F) > 0.05F
				|| Math.abs((zMax - zMin) - 1.0F) > 0.05F) {
			FlatWaterSurfacePolicy.rejectSurfaceGeometryHook();
			return;
		}

		int lowXLowZ = corner(x, z, xMin, zMin);
		int highXLowZ = corner(x, z, xMax, zMin);
		int lowXHighZ = corner(x, z, xMin, zMax);
		int highXHighZ = corner(x, z, xMax, zMax);
		if (lowXLowZ < 0 || highXLowZ < 0 || lowXHighZ < 0 || highXHighZ < 0) {
			FlatWaterSurfacePolicy.rejectSurfaceGeometryHook();
			return;
		}

		float u00 = u[lowXLowZ];
		float u10 = u[highXLowZ];
		float u01 = u[lowXHighZ];
		float v00 = v[lowXLowZ];
		float v10 = v[highXLowZ];
		float v01 = v[lowXHighZ];
		if (!approximately(u[highXHighZ], u00 + (u10 - u00) + (u01 - u00))
				|| !approximately(v[highXHighZ], v00 + (v10 - v00) + (v01 - v00))) {
			FlatWaterSurfacePolicy.rejectSurfaceGeometryHook();
			return;
		}

		float width = patch.width();
		float depth = patch.depth();
		for (int vertex = 0; vertex < 4; vertex++) {
			int base = 1 + vertex * 5;
			boolean lowX = approximately(x[vertex], xMin);
			boolean lowZ = approximately(z[vertex], zMin);
			float xFactor = lowX ? 0.0F : width;
			float zFactor = lowZ ? 0.0F : depth;
			args.set(base, xMin + (xMax - xMin) * xFactor);
			args.set(base + 2, zMin + (zMax - zMin) * zFactor);
			args.set(base + 3, u00 + (u10 - u00) * xFactor + (u01 - u00) * zFactor);
			args.set(base + 4, v00 + (v10 - v00) * xFactor + (v01 - v00) * zFactor);
		}
		FlatWaterSurfacePolicy.markSurfaceGeometryApplied();
	}

	@Inject(method = "tesselate", at = @At("HEAD"), cancellable = true)
	private void wateroptimisation$beforeTesselate(
			BlockAndTintGetter level,
			BlockPos pos,
			FluidRenderer.Output output,
			BlockState blockState,
			FluidState fluidState,
			CallbackInfo callback
	) {
		wateroptimisation$flatWaterPatch.remove();
		if (FluidOptimizationPolicy.reducedWaterBackfacesActive()) {
			if (FluidOptimizationPolicy.isOrdinarySourceWater(blockState, fluidState)) {
				wateroptimisation$waterTessellation.set(Boolean.TRUE);
			} else {
				wateroptimisation$waterTessellation.set(Boolean.FALSE);
			}
		}
		boolean diagnosticsEnabled = Diagnostics.isEnabled();
		if (diagnosticsEnabled) {
			Diagnostics.beginFluidCompile();
		}

		FlatWaterSurfacePolicy.Decision decision = FlatWaterSurfacePolicy.prepare(level, pos, blockState, fluidState);
		if (decision.patch() == null) {
			return;
		}
		if (diagnosticsEnabled) {
			Diagnostics.recordFlatWaterCandidate();
		}
		if (decision.cancelCurrentBlock()) {
			if (diagnosticsEnabled) {
				Diagnostics.endFluidCompile();
			}
			callback.cancel();
			return;
		}
		wateroptimisation$flatWaterPatch.set(decision.patch());
		if (diagnosticsEnabled) {
			Diagnostics.recordFlatWaterPatch();
		}
	}

	@Inject(
			method = "tesselate",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/renderer/block/FluidRenderer;shouldRenderFace(Lnet/minecraft/world/level/material/FluidState;Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/core/Direction;Lnet/minecraft/world/level/material/FluidState;)Z",
					ordinal = 0
			),
			cancellable = true,
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void wateroptimisation$skipInteriorSourceWater(
			BlockAndTintGetter level,
			BlockPos pos,
			FluidRenderer.Output output,
			BlockState blockState,
			FluidState fluidState,
			CallbackInfo callback,
			BlockState blockStateDown,
			FluidState fluidStateDown,
			BlockState blockStateUp,
			FluidState fluidStateUp,
			BlockState blockStateNorth,
			FluidState fluidStateNorth,
			BlockState blockStateSouth,
			FluidState fluidStateSouth,
			BlockState blockStateWest,
			FluidState fluidStateWest,
			BlockState blockStateEast,
			FluidState fluidStateEast,
			boolean renderUp
	) {
		if (!FluidOptimizationPolicy.flatWaterFastPathActive()
				|| !FluidOptimizationPolicy.shouldSkipInteriorSourceWater(
						blockState,
						fluidState,
						blockStateDown,
						fluidStateDown,
						blockStateUp,
						fluidStateUp,
						blockStateNorth,
						fluidStateNorth,
						blockStateSouth,
						fluidStateSouth,
						blockStateWest,
						fluidStateWest,
						blockStateEast,
						fluidStateEast
				)) {
			return;
		}

		if (Diagnostics.isEnabled()) {
			Diagnostics.recordFluidFastPathSkip();
			Diagnostics.endFluidCompile();
		}
		wateroptimisation$flatWaterPatch.remove();
		wateroptimisation$waterTessellation.remove();
		callback.cancel();
	}

	@Inject(method = "tesselate", at = @At("RETURN"))
	private void wateroptimisation$afterTesselate(
			BlockAndTintGetter level,
			BlockPos pos,
			FluidRenderer.Output output,
			BlockState blockState,
			FluidState fluidState,
			CallbackInfo callback
	) {
		wateroptimisation$flatWaterPatch.remove();
		wateroptimisation$waterTessellation.remove();
		if (Diagnostics.isEnabled()) {
			Diagnostics.endFluidCompile();
		}
	}

	@Unique
	private static float min(float[] values) {
		float result = values[0];
		for (int index = 1; index < values.length; index++) {
			result = Math.min(result, values[index]);
		}
		return result;
	}

	@Unique
	private static float max(float[] values) {
		float result = values[0];
		for (int index = 1; index < values.length; index++) {
			result = Math.max(result, values[index]);
		}
		return result;
	}

	@Unique
	private static int corner(float[] x, float[] z, float expectedX, float expectedZ) {
		for (int index = 0; index < x.length; index++) {
			if (approximately(x[index], expectedX) && approximately(z[index], expectedZ)) {
				return index;
			}
		}
		return -1;
	}

	@Unique
	private static boolean approximately(float left, float right) {
		return Math.abs(left - right) <= 0.01F;
	}
}
