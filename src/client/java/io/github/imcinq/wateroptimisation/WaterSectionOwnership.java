package io.github.imcinq.wateroptimisation;

/**
 * Immutable section-local handoff data for the future far-water buffer.
 *
 * <p>This record deliberately contains counts, not renderable geometry. It
 * can therefore travel with a compiled section without changing ownership of
 * vanilla vertex data or creating a second draw. The separate pass must not
 * become active until it has a real water mesh to consume.</p>
 */
public record WaterSectionOwnership(
		long candidateBlocks,
		long candidateFaces,
		long candidateVertices,
		long fallbackBlocks
) {
	public static final WaterSectionOwnership EMPTY = new WaterSectionOwnership(0L, 0L, 0L, 0L);

	public boolean hasCandidateGeometry() {
		return candidateFaces > 0L && candidateVertices > 0L;
	}

	public void publishDiagnostics() {
		Diagnostics.recordFarWaterSection(candidateBlocks, candidateFaces, candidateVertices, fallbackBlocks);
	}
}
