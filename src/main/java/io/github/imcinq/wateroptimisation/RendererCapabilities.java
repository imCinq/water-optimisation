package io.github.imcinq.wateroptimisation;

/**
 * Runtime renderer facts used to choose an effective policy. The values are
 * capability probes, not user settings, so the policy can fail closed when a
 * renderer owns a part of the pipeline or an optional path is not known to be
 * safe.
 */
public record RendererCapabilities(
		boolean sodiumLoaded,
		String rendererName,
		boolean supportsReducedWaterBackfaces
) {
	public RendererCapabilities {
		rendererName = rendererName == null || rendererName.isBlank() ? "Unknown" : rendererName;
	}

	public static RendererCapabilities vanilla() {
		return new RendererCapabilities(false, "Vanilla", false);
	}
}
