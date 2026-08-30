package io.github.imcinq.wateroptimisation;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class WaterOptimisationScreen extends Screen {
	private final Screen parent;
	private final WaterOptimisationConfig workingCopy;
	private Button enabledButton;
	private Button profileButton;

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
		int center = this.width / 2;
		this.enabledButton = this.addRenderableWidget(Button.builder(
				enabledLabel(),
				button -> {
					this.workingCopy.setEnabled(!this.workingCopy.isEnabled());
					button.setMessage(enabledLabel());
				}
		).bounds(center - 155, 62, 310, 20).build());

		this.profileButton = this.addRenderableWidget(Button.builder(
				profileLabel(),
				button -> {
					this.workingCopy.selectProfile(this.workingCopy.getPerformanceProfile().next());
					button.setMessage(profileLabel());
					this.enabledButton.setMessage(enabledLabel());
				}
		).bounds(center - 155, 88, 310, 20).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.wateroptimisation.advanced"),
				button -> this.minecraft.gui.setScreen(new AdvancedWaterOptimisationScreen(this, this.workingCopy))
		).bounds(center - 155, 122, 310, 20).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("gui.done"),
				button -> saveAndClose()
		).bounds(center - 155, this.height - 34, 150, 20).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("gui.cancel"),
				button -> closeWithoutSaving()
		).bounds(center + 5, this.height - 34, 150, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		int center = this.width / 2;
		graphics.text(this.font, Component.translatable("screen.wateroptimisation.description"), center - 155, 34, 0xFFFFFFFF, false);
		graphics.text(this.font, Component.translatable("screen.wateroptimisation.warning"), center - 155, 151, 0xFFFFCC66, false);
		graphics.text(this.font, Component.translatable("screen.wateroptimisation.keybind"), center - 155, 169, 0xFFAAAAAA, false);
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
}
