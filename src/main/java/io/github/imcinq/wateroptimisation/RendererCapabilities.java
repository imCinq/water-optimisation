package io.github.imcinq.wateroptimisation;

/**
 * Runtime renderer facts used to choose an effective policy. The values are
 * capability probes, not user settings, so the policy can fail closed when a
 * renderer or optional bridge is not known to be safe.
 */
public record RendererCapabilities(
		boolean sodiumLoaded,
		boolean sodiumGeometryHooksAvailable,
		boolean flatWaterSurfaceMeshingSupported,
		boolean farWaterPassSupported,
		String rendererName
) {
	public RendererCapabilities {
		rendererName = rendererName == null || rendererName.isBlank() ? "Unknown" : rendererName;
	}

	public static RendererCapabilities vanilla() {
		return new RendererCapabilities(false, false, false, false, "Vanilla");
	}
}
