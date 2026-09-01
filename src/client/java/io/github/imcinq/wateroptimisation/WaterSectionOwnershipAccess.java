package io.github.imcinq.wateroptimisation;

/**
 * Read-only access for the future far-water drawing stage.
 *
 * <p>The interface is intentionally tiny: a later pass can inspect the
 * compiled section that vanilla already selected without making the current
 * renderer hide or duplicate any geometry.</p>
 */
public interface WaterSectionOwnershipAccess {
	WaterSectionOwnership wateroptimisation$getWaterOwnership();
}
