package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.WaterOwnedMesh;
import io.github.imcinq.wateroptimisation.WaterSectionOwnership;
import io.github.imcinq.wateroptimisation.WaterSectionOwnershipResultsAccess;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Carries the probe result through vanilla's compiled-section handoff. */
@Mixin(SectionCompiler.Results.class)
public abstract class SectionCompilerResultsMixin implements WaterSectionOwnershipResultsAccess {
	@Unique
	private WaterSectionOwnership wateroptimisation$waterOwnership = WaterSectionOwnership.EMPTY;

	@Override
	public WaterSectionOwnership wateroptimisation$getWaterOwnership() {
		return wateroptimisation$waterOwnership;
	}

	@Override
	public void wateroptimisation$setWaterOwnership(WaterSectionOwnership ownership) {
		wateroptimisation$waterOwnership = ownership == null ? WaterSectionOwnership.EMPTY : ownership;
	}

	@Override
	public WaterSectionOwnership wateroptimisation$takeWaterOwnership() {
		WaterSectionOwnership ownership = wateroptimisation$waterOwnership;
		wateroptimisation$waterOwnership = ownership.withoutOwnedMesh();
		return ownership;
	}

	@Inject(method = "release", at = @At("HEAD"))
	private void wateroptimisation$closeUnclaimedMesh(CallbackInfo callback) {
		WaterOwnedMesh mesh = wateroptimisation$waterOwnership.ownedMesh();
		wateroptimisation$waterOwnership = wateroptimisation$waterOwnership.withoutOwnedMesh();
		if (mesh != null) {
			mesh.close();
		}
	}
}
