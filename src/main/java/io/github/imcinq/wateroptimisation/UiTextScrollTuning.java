package io.github.imcinq.wateroptimisation;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;

/**
 * Keeps the small amount of state needed to tune scrolling for this mod's
 * settings labels without changing scrolling in unrelated Minecraft screens.
 */
public final class UiTextScrollTuning {
	/**
	 * Vanilla uses 0.5 seconds per scrolled pixel. This is intentionally only a
	 * small reduction so long labels become readable sooner without looking
	 * rushed.
	 */
	public static final double MOD_LABEL_PERIOD_PER_PIXEL = 0.45D;

	private static final String MOD_SCREEN_KEY_PREFIX = "screen.wateroptimisation.";
	private static final ThreadLocal<Integer> MOD_LABEL_DEPTH = new ThreadLocal<>();

	private UiTextScrollTuning() {
	}

	public static boolean isModLabel(Component message) {
		return message != null
				&& message.getContents() instanceof TranslatableContents contents
				&& contents.getKey().startsWith(MOD_SCREEN_KEY_PREFIX);
	}

	public static void begin(Component message) {
		if (!isModLabel(message)) {
			return;
		}
		Integer depth = MOD_LABEL_DEPTH.get();
		MOD_LABEL_DEPTH.set(depth == null ? 1 : depth + 1);
	}

	public static void end(Component message) {
		if (!isModLabel(message)) {
			return;
		}
		Integer depth = MOD_LABEL_DEPTH.get();
		if (depth == null || depth <= 1) {
			MOD_LABEL_DEPTH.remove();
		} else {
			MOD_LABEL_DEPTH.set(depth - 1);
		}
	}

	public static boolean isModLabelActive() {
		Integer depth = MOD_LABEL_DEPTH.get();
		return depth != null && depth > 0;
	}
}
