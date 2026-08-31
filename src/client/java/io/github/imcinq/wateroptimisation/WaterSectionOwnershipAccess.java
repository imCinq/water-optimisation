package io.github.imcinq.wateroptimisation;

/** Read-only access to the optional water-owned mesh on a compiled section. */
public interface WaterSectionOwnershipAccess {
	WaterSectionOwnership wateroptimisation$getWaterOwnership();

	WaterOwnedMesh wateroptimisation$getOwnedMesh();
}
