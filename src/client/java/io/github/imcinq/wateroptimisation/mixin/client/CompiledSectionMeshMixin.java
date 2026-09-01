package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.WaterOwnedMesh;
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
	@Unique
	private WaterOwnedMesh wateroptimisation$ownedMesh;

	@Inject(method = "<init>", at = @At("TAIL"), require = 0)
	private void wateroptimisation$copyWaterOwnership(
			TranslucencyPointOfView translucencyPointOfView,
			SectionCompiler.Results results,
			CallbackInfo callback
	) {
		Object resultObject = results;
		if (resultObject instanceof WaterSectionOwnershipResultsAccess access) {
			wateroptimisation$waterOwnership = access.wateroptimisation$takeWaterOwnership();
			wateroptimisation$ownedMesh = wateroptimisation$waterOwnership.ownedMesh();
		}
	}

	@Inject(method = "close", at = @At("HEAD"))
	private void wateroptimisation$closeOwnedMesh(CallbackInfo callback) {
		if (this.wateroptimisation$ownedMesh != null) {
			this.wateroptimisation$ownedMesh.close();
			this.wateroptimisation$ownedMesh = null;
		}
	}

	@Override
	public WaterSectionOwnership wateroptimisation$getWaterOwnership() {
		return wateroptimisation$waterOwnership;
	}

	@Override
	public WaterOwnedMesh wateroptimisation$getOwnedMesh() {
		return this.wateroptimisation$ownedMesh;
	}
}
