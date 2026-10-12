package io.github.imcinq.wateroptimisation;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

/**
 * Client API seam for members that were renamed in 26.1.2. Each src/<version> tree has its own copy.
 */
final class ClientCompat {
	private ClientCompat() {
	}

	static void setScreen(Minecraft client, Screen screen) {
		client.setScreen(screen);
	}

	static Screen currentScreen(Minecraft client) {
		return client.screen;
	}

	static void markAllChanged(Minecraft client) {
		if (client.level != null && client.levelRenderer != null) {
			client.levelRenderer.allChanged();
		}
	}

	static Camera mainCamera(Minecraft client) {
		return client.gameRenderer.getMainCamera();
	}
}
