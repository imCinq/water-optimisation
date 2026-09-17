package io.github.imcinq.wateroptimisation;

import com.mojang.blaze3d.platform.InputConstants;

final class KeyboardInput {
	private KeyboardInput() {
	}

	static InputConstants.Type type() {
		return InputConstants.Type.KEYBOARD;
	}
}
