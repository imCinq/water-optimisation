package io.github.imcinq.wateroptimisation;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class WaterOptimisationScreen extends Screen {
	private static final int SIDE_MARGIN = 24;
	private static final int MAX_TEXT_WIDTH = 520;
	private static final int MAX_BUTTON_WIDTH = 310;
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
	private int warningY;
	private int keybindY;

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

		int y = this.descriptionY + wrappedHeight(Component.translatable("screen.wateroptimisation.description")) + 8;
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
					button.setMessage(profileLabel());
					this.enabledButton.setMessage(enabledLabel());
				}
		).bounds(this.buttonLeft, y, this.buttonWidth, BUTTON_HEIGHT).build());

		y += BUTTON_HEIGHT + BUTTON_GAP;
		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.wateroptimisation.advanced"),
				button -> this.minecraft.gui.setScreen(new AdvancedWaterOptimisationScreen(this, this.workingCopy))
		).bounds(this.buttonLeft, y, this.buttonWidth, BUTTON_HEIGHT).build());

		y += BUTTON_HEIGHT;
		this.warningY = y + 8;
		this.keybindY = this.warningY + wrappedHeight(Component.translatable("screen.wateroptimisation.warning")) + 4;

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
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		drawCenteredWrapped(graphics, Component.translatable("screen.wateroptimisation.description"), this.descriptionY, 0xFFFFFFFF);
		drawCenteredWrapped(graphics, Component.translatable("screen.wateroptimisation.warning"), this.warningY, 0xFFFFCC66);
		drawCenteredWrapped(graphics, Component.translatable("screen.wateroptimisation.keybind"), this.keybindY, 0xFFAAAAAA);
	}

	@Override
	public void onClose() {
		closeWithoutSaving();
	}

	private void saveAndClose() {
		ConfigManager.save(this.workingCopy);
		this.minecraft.gui.setScreen(this.parent);
	}

	private void closeWithoutSaving() {
		this.minecraft.gui.setScreen(this.parent);
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

	private int lineHeight() {
		return this.font.lineHeight + 2;
	}

	private int wrappedHeight(Component text) {
		return Math.max(1, this.font.split(text, this.contentWidth).size()) * lineHeight();
	}

	private void drawCenteredWrapped(GuiGraphicsExtractor graphics, Component text, int top, int color) {
		List<FormattedCharSequence> lines = this.font.split(text, this.contentWidth);
		int y = top;
		int center = this.width / 2;
		for (FormattedCharSequence line : lines) {
			graphics.text(this.font, line, center - this.font.width(line) / 2, y, color, false);
			y += lineHeight();
		}
	}
}
