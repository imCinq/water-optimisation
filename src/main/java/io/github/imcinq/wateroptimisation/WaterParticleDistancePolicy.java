package io.github.imcinq.wateroptimisation;

/**
 * Pure distance math for cosmetic water-particle admission. The caller supplies
 * the camera position, or a safe lifecycle fallback, so this class stays
 * independent of Minecraft client state and remains unit-testable.
 */
public final class WaterParticleDistancePolicy {
	public static final double FOG_DISTANCE_SCALE = 0.75D;

	private WaterParticleDistancePolicy() {
	}

	public static double effectiveDistance(WaterOptimisationConfig config) {
		double distance = config.getParticleDistance();
		return config.isParticleFogCulling() ? distance * FOG_DISTANCE_SCALE : distance;
	}

	public static boolean isWithinDistance(
			WaterOptimisationConfig config,
			double referenceX,
			double referenceY,
			double referenceZ,
			double particleX,
			double particleY,
			double particleZ
	) {
		double maxDistance = effectiveDistance(config);
		double dx = referenceX - particleX;
		double dy = referenceY - particleY;
		double dz = referenceZ - particleZ;
		return dx * dx + dy * dy + dz * dz <= maxDistance * maxDistance;
	}
}
