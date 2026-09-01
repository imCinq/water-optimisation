package io.github.imcinq.wateroptimisation;

/** Immutable section-local handoff data for the optional 26.2 water pass. */
public record WaterSectionOwnership(
		long candidateBlocks,
		long candidateFaces,
		long candidateVertices,
		long fallbackBlocks,
		WaterOwnedMesh ownedMesh
) {
	public static final WaterSectionOwnership EMPTY = new WaterSectionOwnership(0L, 0L, 0L, 0L, null);

	public WaterSectionOwnership(long candidateBlocks, long candidateFaces, long candidateVertices, long fallbackBlocks) {
		this(candidateBlocks, candidateFaces, candidateVertices, fallbackBlocks, null);
	}

	public boolean hasCandidateGeometry() {
		return candidateFaces > 0L && candidateVertices > 0L;
	}

	public boolean hasOwnedGeometry() {
		return this.ownedMesh != null && this.hasCandidateGeometry();
	}

	public WaterSectionOwnership withoutOwnedMesh() {
		return this.ownedMesh == null
				? this
				: new WaterSectionOwnership(this.candidateBlocks, this.candidateFaces, this.candidateVertices, this.fallbackBlocks, null);
	}

	public void publishDiagnostics() {
		Diagnostics.recordFarWaterSection(candidateBlocks, candidateFaces, candidateVertices, fallbackBlocks);
	}
}
