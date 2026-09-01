package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.WaterSectionOwnership;
import io.github.imcinq.wateroptimisation.WaterSectionOwnershipAccess;
import io.github.imcinq.wateroptimisation.WaterSectionOwnershipResultsAccess;
import net.minecraft.client.renderer.chunk.CompiledSectionMesh;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import net.minecraft.client.renderer.chunk.TranslucencyPointOfView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Keeps the ownership handoff aligned with the lifetime of the section mesh. */
@Mixin(CompiledSectionMesh.class)
public abstract class CompiledSectionMeshMixin implements WaterSectionOwnershipAccess {
	@Unique
	private WaterSectionOwnership wateroptimisation$waterOwnership = WaterSectionOwnership.EMPTY;

	@Inject(method = "<init>", at = @At("TAIL"), require = 0)
	private void wateroptimisation$copyWaterOwnership(
			TranslucencyPointOfView translucencyPointOfView,
			SectionCompiler.Results results,
			CallbackInfo callback
	) {
		Object resultObject = results;
		if (resultObject instanceof WaterSectionOwnershipResultsAccess access) {
			wateroptimisation$waterOwnership = access.wateroptimisation$getWaterOwnership();
		}
	}

	@Override
	public WaterSectionOwnership wateroptimisation$getWaterOwnership() {
		return wateroptimisation$waterOwnership;
	}
}
