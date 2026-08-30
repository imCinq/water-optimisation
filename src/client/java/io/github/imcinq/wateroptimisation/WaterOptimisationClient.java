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

	public static boolean shouldKeepWaterParticle(ParticleOptions particle, boolean alwaysShow, double x, double y, double z) {
		if (!isWaterParticle(particle)) {
			return true;
		}
		Diagnostics.recordParticleCandidate();

		WaterOptimisationConfig config = ConfigManager.get();
		if (!config.isEnabled() || config.getPerformanceProfile() == WaterOptimisationConfig.PerformanceProfile.VANILLA) {
			return true;
		}
		if (alwaysShow) {
			return true;
		}
		if (!config.isWaterParticles()) {
			Diagnostics.recordParticleRejected(false);
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

		if (!WaterParticleDistancePolicy.isWithinDistance(config, referenceX, referenceY, referenceZ, x, y, z)) {
			Diagnostics.recordParticleRejected(true);
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

		Diagnostics.Snapshot snapshot = Diagnostics.snapshot();
		Minecraft client = Minecraft.getInstance();
		int x = 6;
		int y = 6;
		int lineHeight = client.font.lineHeight + 2;
		int lines = 12;
		graphics.fill(x - 3, y - 3, x + 290, y + lineHeight * lines + 2, 0x90000000);
		graphics.text(client.font, Component.literal("Water Optimisation"), x, y, 0xFFFFFFFF, true);
		graphics.text(client.font, Component.literal("mode: " + modeLabel(config)), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("fluid hooks: " + onOff(FluidOptimizationPolicy.fluidHooksActive())), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("fast path: " + onOff(FluidOptimizationPolicy.flatWaterFastPathActive())), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("fluid blocks: " + snapshot.fluidBlocksVisited()), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("faces kept/cut: " + snapshot.fluidFacesAccepted() + "/" + snapshot.fluidFacesCulled()), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("face overrides: " + snapshot.fluidFaceOverrides()), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("fast-path skips: " + snapshot.fluidFastPathSkips()), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("fluid compile avg: " + String.format(java.util.Locale.ROOT, "%.3f ms", snapshot.averageFluidCompileMillis())), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("section compile avg: " + String.format(java.util.Locale.ROOT, "%.3f ms", snapshot.averageSectionCompileMillis())), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("translucent resort avg: " + String.format(java.util.Locale.ROOT, "%.3f ms", snapshot.averageTranslucentResortMillis())), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("particles rejected: " + snapshot.particleRejected() + "/" + snapshot.particleCandidates()), x, y += lineHeight, 0xFFFFFFFF, false);
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
}
