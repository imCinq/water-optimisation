package io.github.imcinq.wateroptimisation;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class WaterOptimisationClient implements ClientModInitializer {
	public static final String MOD_ID = "wateroptimisation";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final KeyMapping.Category KEY_CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(MOD_ID, "general")
	);
	private static KeyMapping openConfigKey;

	@Override
	public void onInitializeClient() {
		ConfigManager.load();
		openConfigKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
				"key.wateroptimisation.open_config",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_O,
				KEY_CATEGORY
		));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (openConfigKey.consumeClick()) {
				if (client.screen == null) {
					client.gui.setScreen(new WaterOptimisationScreen(null));
				}
			}
		});

		HudElementRegistry.addLast(
				Identifier.fromNamespaceAndPath(MOD_ID, "diagnostics"),
				WaterOptimisationClient::extractDiagnostics
		);

		if (FabricLoader.getInstance().isModLoaded("sodium")) {
			LOGGER.info("Sodium detected; vanilla fluid optimization hooks are disabled for renderer ownership compatibility.");
		}
		LOGGER.info("Water Optimisation initialized; rendering changes are opt-in and default to safe no-op behavior.");
	}

	public static boolean isSodiumLoaded() {
		return FabricLoader.getInstance().isModLoaded("sodium");
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
		int lines = 7;
		graphics.fill(x - 3, y - 3, x + 290, y + lineHeight * lines + 2, 0x90000000);
		graphics.text(client.font, Component.literal("Water Optimisation"), x, y, 0xFFFFFFFF, true);
		graphics.text(client.font, Component.literal("mode: " + modeLabel(config)), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("fluid blocks: " + snapshot.fluidBlocksVisited()), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("faces kept/cut: " + snapshot.fluidFacesAccepted() + "/" + snapshot.fluidFacesCulled()), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("fast-path skips: " + snapshot.fluidFastPathSkips()), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("fluid compile avg: " + String.format(java.util.Locale.ROOT, "%.3f ms", snapshot.averageFluidCompileMillis())), x, y += lineHeight, 0xFFFFFFFF, false);
		graphics.text(client.font, Component.literal("particles rejected: " + snapshot.particleRejected()), x, y += lineHeight, 0xFFFFFFFF, false);
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
}
