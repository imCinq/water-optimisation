package io.github.imcinq.wateroptimisation;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class WaterOptimisationScreen extends Screen {
	private static final int SIDE_MARGIN = 24;
	private static final int MAX_TEXT_WIDTH = 560;
	private static final int MAX_BUTTON_WIDTH = 360;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_GAP = 6;

	private final Screen parent;
	private final WaterOptimisationConfig workingCopy;
	private Button enabledButton;
	private Button profileButton;
	private int contentWidth;
	private int buttonLeft;
	private int buttonWidth;
	private int descriptionY;
	private int effectivePathY;

	public WaterOptimisationScreen(Screen parent) {
		this(parent, ConfigManager.copy());
	}

	WaterOptimisationScreen(Screen parent, WaterOptimisationConfig workingCopy) {
		super(Component.translatable("screen.wateroptimisation.title"));
		this.parent = parent;
		this.workingCopy = workingCopy;
	}

	@Override
	protected void init() {
		this.contentWidth = Math.max(1, Math.min(MAX_TEXT_WIDTH, this.width - SIDE_MARGIN * 2));
		this.buttonWidth = Math.max(1, Math.min(MAX_BUTTON_WIDTH, this.width - SIDE_MARGIN * 2));
		this.buttonLeft = (this.width - this.buttonWidth) / 2;
		this.descriptionY = 34;

		Component description = Component.translatable("screen.wateroptimisation.description");
		int y = this.descriptionY + wrappedHeight(description) + 4;
		this.effectivePathY = y;
		y += wrappedHeight(effectivePathLabel()) + 8;
		this.enabledButton = this.addRenderableWidget(Button.builder(
				enabledLabel(),
				button -> {
					this.workingCopy.setEnabled(!this.workingCopy.isEnabled());
					button.setMessage(enabledLabel());
				}
		).bounds(this.buttonLeft, y, this.buttonWidth, BUTTON_HEIGHT).build());

		y += BUTTON_HEIGHT + BUTTON_GAP;
		this.profileButton = this.addRenderableWidget(Button.builder(
				profileLabel(),
				button -> {
					this.workingCopy.selectProfile(this.workingCopy.getPerformanceProfile().next());
					// The effective-path text can wrap differently for each preset.
					// Rebuild the layout so the buttons never retain stale positions.
					this.rebuildWidgets();
				}
		).bounds(this.buttonLeft, y, this.buttonWidth, BUTTON_HEIGHT).build());

		y += BUTTON_HEIGHT + BUTTON_GAP;
		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.wateroptimisation.advanced"),
				button -> this.minecraft.setScreen(new AdvancedWaterOptimisationScreen(this, this.workingCopy))
		).bounds(this.buttonLeft, y, this.buttonWidth, BUTTON_HEIGHT).build());

		int actionWidth = Math.max(1, (this.buttonWidth - 10) / 2);
		int actionY = this.height - 34;
		this.addRenderableWidget(Button.builder(
				Component.translatable("gui.done"),
				button -> saveAndClose()
		).bounds(this.buttonLeft, actionY, actionWidth, BUTTON_HEIGHT).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("gui.cancel"),
				button -> closeWithoutSaving()
		).bounds(this.buttonLeft + actionWidth + 10, actionY, this.buttonWidth - actionWidth - 10, BUTTON_HEIGHT).build());
	}

	@Override
	public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
		super.render(graphics, mouseX, mouseY, delta);
		drawCenteredWrapped(graphics, Component.translatable("screen.wateroptimisation.description"), this.descriptionY, 0xFFFFFFFF);
		drawCenteredWrapped(graphics, effectivePathLabel(), this.effectivePathY, 0xFFB0B0B0);
	}

	@Override
	public void onClose() {
		closeWithoutSaving();
	}

	private void saveAndClose() {
		ConfigManager.save(this.workingCopy);
		this.minecraft.setScreen(this.parent);
	}

	private void closeWithoutSaving() {
		this.minecraft.setScreen(this.parent);
	}

	private Component enabledLabel() {
		return Component.translatable(
				this.workingCopy.isEnabled()
						? "screen.wateroptimisation.enabled.on"
						: "screen.wateroptimisation.enabled.off"
		);
	}

	private Component profileLabel() {
		return Component.translatable(
				"screen.wateroptimisation.profile",
				Component.translatable(this.workingCopy.getPerformanceProfile().translationKey())
		);
	}

	private Component effectivePathLabel() {
		return Component.translatable("screen.wateroptimisation.effective_path", WaterOptimisationClient.effectivePath(this.workingCopy));
	}

	private int lineHeight() {
		return this.font.lineHeight + 2;
	}

	private int wrappedHeight(Component text) {
		return Math.max(1, this.font.split(text, this.contentWidth).size()) * lineHeight();
	}

	private void drawCenteredWrapped(GuiGraphics graphics, Component text, int top, int color) {
		List<FormattedCharSequence> lines = this.font.split(text, this.contentWidth);
		int y = top;
		int center = this.width / 2;
		for (FormattedCharSequence line : lines) {
			graphics.drawString(this.font, line, center - this.font.width(line) / 2, y, color);
			y += lineHeight();
		}
	}
}
