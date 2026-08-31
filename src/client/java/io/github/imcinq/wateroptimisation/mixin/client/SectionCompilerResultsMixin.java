package io.github.imcinq.wateroptimisation.mixin.client;

import io.github.imcinq.wateroptimisation.WaterSectionOwnership;
import io.github.imcinq.wateroptimisation.WaterSectionOwnershipResultsAccess;
import net.minecraft.client.renderer.chunk.SectionCompiler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

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
}
