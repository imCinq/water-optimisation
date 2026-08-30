package io.github.imcinq.wateroptimisation;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WaterOptimisationClient implements ClientModInitializer {
	public static final String MOD_ID = "wateroptimisation";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(MOD_ID, "general")
	);
	private static KeyMapping openConfigKey;
	private static volatile boolean sodiumLoaded;
	private static volatile ParticleFilterSettings particleFilterSettings = ParticleFilterSettings.INACTIVE;
	private static final long DIAGNOSTICS_REFRESH_INTERVAL_NANOS = 250_000_000L;
	private static long diagnosticsRefreshDeadlineNanos;
	private static Component[] diagnosticsLines = new Component[0];

	@Override
	public void onInitializeClient() {
		ConfigManager.load();
		sodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");
		FluidOptimizationPolicy.refresh();
		openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.wateroptimisation.open_config",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_O,
				KEY_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfigKey.consumeClick()) {
				if (client.gui.screen() == null) {
					client.gui.setScreen(new WaterOptimisationScreen(null));
				}
			}
		});

		HudElementRegistry.addLast(
				Identifier.fromNamespaceAndPath(MOD_ID, "diagnostics"),
				WaterOptimisationClient::extractDiagnostics
		);

		if (sodiumLoaded) {
			LOGGER.info("Sodium detected; vanilla fluid optimization hooks are disabled for renderer ownership compatibility.");
		}
		LOGGER.info("Water Optimisation initialized; rendering changes are opt-in and default to safe no-op behavior.");
	}

	public static boolean isSodiumLoaded() {
		return sodiumLoaded;
	}

	public static void refreshParticleFiltering(WaterOptimisationConfig config) {
		if (config == null) {
			particleFilterSettings = ParticleFilterSettings.INACTIVE;
			return;
		}

		boolean active = config.isEnabled()
				&& config.getPerformanceProfile() != WaterOptimisationConfig.PerformanceProfile.VANILLA;
		double maxDistance = WaterParticleDistancePolicy.effectiveDistance(config);
		particleFilterSettings = new ParticleFilterSettings(active, config.isWaterParticles(), maxDistance * maxDistance);
	}

	public static boolean shouldKeepWaterParticle(ParticleOptions particle, boolean alwaysShow, double x, double y, double z) {
		ParticleFilterSettings settings = particleFilterSettings;
		if (!settings.active()) {
			return true;
		}
		if (!isWaterParticle(particle)) {
			return true;
		}
		boolean diagnosticsEnabled = Diagnostics.isEnabled();
		if (diagnosticsEnabled) {
			Diagnostics.recordParticleCandidate();
		}

		if (alwaysShow) {
			return true;
		}
		if (!settings.keepWaterParticles()) {
			if (diagnosticsEnabled) {
				Diagnostics.recordParticleRejected(false);
			}
			return false;
		}

		Minecraft client = Minecraft.getInstance();
		if (client.player == null) {
			return true;
		}

		double referenceX = client.player.getX();
		double referenceY = client.player.getY();
		double referenceZ = client.player.getZ();
		Camera camera = client.gameRenderer.mainCamera();
		if (camera.isInitialized()) {
			Vec3 cameraPosition = camera.position();
			referenceX = cameraPosition.x;
			referenceY = cameraPosition.y;
			referenceZ = cameraPosition.z;
		}

		if (!WaterParticleDistancePolicy.isWithinDistanceSquared(settings.maxDistanceSquared(), referenceX, referenceY, referenceZ, x, y, z)) {
			if (diagnosticsEnabled) {
				Diagnostics.recordParticleRejected(true);
			}
			return false;
		}
		return true;
	}

	private static boolean isWaterParticle(ParticleOptions particle) {
		ParticleType<?> type = particle.getType();
		return type == ParticleTypes.BUBBLE
			|| type == ParticleTypes.BUBBLE_COLUMN_UP
			|| type == ParticleTypes.BUBBLE_POP
			|| type == ParticleTypes.CURRENT_DOWN
			|| type == ParticleTypes.DRIPPING_WATER
			|| type == ParticleTypes.FALLING_WATER
			|| type == ParticleTypes.SPLASH
			|| type == ParticleTypes.UNDERWATER;
	}

	private static void extractDiagnostics(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
		WaterOptimisationConfig config = ConfigManager.get();
		if (!config.isDiagnosticsHud()) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		long now = System.nanoTime();
		if (diagnosticsLines.length == 0 || now >= diagnosticsRefreshDeadlineNanos) {
			Diagnostics.Snapshot snapshot = Diagnostics.snapshot();
			diagnosticsLines = new Component[]{
				Component.literal("Water Optimisation"),
				Component.literal("mode: " + modeLabel(config)),
				Component.literal("fluid hooks: " + onOff(FluidOptimizationPolicy.fluidHooksActive())),
				Component.literal("fast path: " + onOff(FluidOptimizationPolicy.flatWaterFastPathActive())),
				Component.literal("water backfaces: " + (FluidOptimizationPolicy.reducedWaterBackfacesActive() ? "reduced" : "vanilla")),
				Component.literal("fluid blocks: " + snapshot.fluidBlocksVisited()),
				Component.literal("fast-path skips: " + snapshot.fluidFastPathSkips()),
				Component.literal("reverse faces removed: " + snapshot.reducedWaterBackfaces()),
				Component.literal("fluid avg (1/16): " + String.format(java.util.Locale.ROOT, "%.3f ms", snapshot.averageFluidCompileMillis())),
				Component.literal("section compile avg: " + String.format(java.util.Locale.ROOT, "%.3f ms", snapshot.averageSectionCompileMillis())),
				Component.literal("translucent resort avg: " + String.format(java.util.Locale.ROOT, "%.3f ms", snapshot.averageTranslucentResortMillis())),
				Component.literal("particles rejected: " + snapshot.particleRejected() + "/" + snapshot.particleCandidates())
			};
			diagnosticsRefreshDeadlineNanos = now + DIAGNOSTICS_REFRESH_INTERVAL_NANOS;
		}

		int x = 6;
		int y = 6;
		int lineHeight = client.font.lineHeight + 2;
		int lines = diagnosticsLines.length;
		graphics.fill(x - 3, y - 3, x + 290, y + lineHeight * lines + 2, 0x90000000);
		for (int index = 0; index < lines; index++) {
			graphics.text(client.font, diagnosticsLines[index], x, y + lineHeight * index, 0xFFFFFFFF, index == 0);
		}
	}

	private static String modeLabel(WaterOptimisationConfig config) {
		if (!config.isEnabled()) {
			return "disabled";
		}
		if (isSodiumLoaded()) {
			return "particles only (Sodium owns fluids)";
		}
		return config.getPerformanceProfile().name().toLowerCase(java.util.Locale.ROOT);
	}

	private static String onOff(boolean value) {
		return value ? "on" : "off";
	}

	private record ParticleFilterSettings(boolean active, boolean keepWaterParticles, double maxDistanceSquared) {
		private static final ParticleFilterSettings INACTIVE = new ParticleFilterSettings(false, true, 0.0D);
	}
}
