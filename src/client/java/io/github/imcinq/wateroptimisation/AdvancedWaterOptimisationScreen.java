package io.github.imcinq.wateroptimisation;

import java.util.List;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class AdvancedWaterOptimisationScreen extends Screen {
	private static final int SIDE_MARGIN = 24;
	private static final int MAX_TEXT_WIDTH = 520;
	private static final int MAX_BUTTON_WIDTH = 310;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_GAP = 6;

	private final Screen parent;
	private final WaterOptimisationConfig workingCopy;
	private Button cullingButton;
	private Button fastPathButton;
	private Button particlesButton;
	private Button particleDistanceButton;
	private Button fogButton;
	private Button diagnosticsButton;
	private Button fallbackButton;
	private int contentWidth;
	private int buttonLeft;
	private int buttonWidth;
	private int descriptionY;
	private int warningY;
	private int actionY;

	public AdvancedWaterOptimisationScreen(Screen parent, WaterOptimisationConfig workingCopy) {
		super(Component.translatable("screen.wateroptimisation.advanced.title"));
		this.parent = parent;
		this.workingCopy = workingCopy;
	}

	@Override
	protected void init() {
		this.contentWidth = Math.max(1, Math.min(MAX_TEXT_WIDTH, this.width - SIDE_MARGIN * 2));
		this.buttonWidth = Math.max(1, Math.min(MAX_BUTTON_WIDTH, this.width - SIDE_MARGIN * 2));
		this.buttonLeft = (this.width - this.buttonWidth) / 2;
		this.descriptionY = 16;

		int y = this.descriptionY + wrappedHeight(Component.translatable("screen.wateroptimisation.advanced.description")) + 8;
		this.cullingButton = this.addRenderableWidget(Button.builder(cullingLabel(), button -> {
			this.workingCopy.setFluidCullingMode(this.workingCopy.getFluidCullingMode().next());
			button.setMessage(cullingLabel());
		}).bounds(this.buttonLeft, y, this.buttonWidth, BUTTON_HEIGHT).build());

		y += BUTTON_HEIGHT + BUTTON_GAP;
		this.fastPathButton = this.addRenderableWidget(Button.builder(fastPathLabel(), button -> {
			this.workingCopy.setFlatWaterFastPath(!this.workingCopy.isFlatWaterFastPath());
			button.setMessage(fastPathLabel());
		}).bounds(this.buttonLeft, y, this.buttonWidth, BUTTON_HEIGHT).build());

		y += BUTTON_HEIGHT + BUTTON_GAP;
		this.particlesButton = this.addRenderableWidget(Button.builder(particlesLabel(), button -> {
			this.workingCopy.setWaterParticles(!this.workingCopy.isWaterParticles());
			button.setMessage(particlesLabel());
		}).bounds(this.buttonLeft, y, this.buttonWidth, BUTTON_HEIGHT).build());

		y += BUTTON_HEIGHT + BUTTON_GAP;
		this.particleDistanceButton = this.addRenderableWidget(Button.builder(particleDistanceLabel(), button -> {
			int distance = this.workingCopy.getParticleDistance();
			this.workingCopy.setParticleDistance(distance >= 128 ? 8 : distance * 2);
			button.setMessage(particleDistanceLabel());
		}).bounds(this.buttonLeft, y, this.buttonWidth, BUTTON_HEIGHT).build());

		y += BUTTON_HEIGHT + BUTTON_GAP;
		this.fogButton = this.addRenderableWidget(Button.builder(fogLabel(), button -> {
			this.workingCopy.setParticleFogCulling(!this.workingCopy.isParticleFogCulling());
			button.setMessage(fogLabel());
		}).bounds(this.buttonLeft, y, this.buttonWidth, BUTTON_HEIGHT).build());

		y += BUTTON_HEIGHT + BUTTON_GAP;
		this.diagnosticsButton = this.addRenderableWidget(Button.builder(diagnosticsLabel(), button -> {
			this.workingCopy.setDiagnosticsHud(!this.workingCopy.isDiagnosticsHud());
			button.setMessage(diagnosticsLabel());
		}).bounds(this.buttonLeft, y, this.buttonWidth, BUTTON_HEIGHT).build());

		y += BUTTON_HEIGHT + BUTTON_GAP;
		this.fallbackButton = this.addRenderableWidget(Button.builder(fallbackLabel(), button -> {
			this.workingCopy.setDebugFallbackLogging(!this.workingCopy.isDebugFallbackLogging());
			button.setMessage(fallbackLabel());
		}).bounds(this.buttonLeft, y, this.buttonWidth, BUTTON_HEIGHT).build());

		y += BUTTON_HEIGHT;
		this.warningY = y + 8;
		this.actionY = this.warningY + wrappedHeight(Component.translatable("screen.wateroptimisation.advanced.warning")) + 8;
		int actionWidth = Math.max(1, (this.buttonWidth - 10) / 2);

		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.wateroptimisation.reset"),
				button -> {
					this.workingCopy.resetToProfile();
					this.rebuildWidgets();
				}
		).bounds(this.buttonLeft, this.actionY, actionWidth, BUTTON_HEIGHT).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("gui.done"),
				button -> this.minecraft.gui.setScreen(this.parent)
		).bounds(this.buttonLeft + actionWidth + 10, this.actionY, this.buttonWidth - actionWidth - 10, BUTTON_HEIGHT).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		drawCenteredWrapped(graphics, Component.translatable("screen.wateroptimisation.advanced.description"), this.descriptionY, 0xFFFFFFFF);
		drawCenteredWrapped(graphics, Component.translatable("screen.wateroptimisation.advanced.warning"), this.warningY, 0xFFFFCC66);
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.parent);
	}

	private Component cullingLabel() {
		return Component.translatable("screen.wateroptimisation.culling", Component.translatable(this.workingCopy.getFluidCullingMode().translationKey()));
	}

	private Component fastPathLabel() {
		return Component.translatable("screen.wateroptimisation.fast_path", yesNo(this.workingCopy.isFlatWaterFastPath()));
	}

	private Component particlesLabel() {
		return Component.translatable("screen.wateroptimisation.particles", yesNo(this.workingCopy.isWaterParticles()));
	}

	private Component particleDistanceLabel() {
		return Component.translatable("screen.wateroptimisation.particle_distance", this.workingCopy.getParticleDistance());
	}

	private Component fogLabel() {
		return Component.translatable("screen.wateroptimisation.fog", yesNo(this.workingCopy.isParticleFogCulling()));
	}

	private Component diagnosticsLabel() {
		return Component.translatable("screen.wateroptimisation.diagnostics", yesNo(this.workingCopy.isDiagnosticsHud()));
	}

	private Component fallbackLabel() {
		return Component.translatable("screen.wateroptimisation.fallback_logging", yesNo(this.workingCopy.isDebugFallbackLogging()));
	}

	private Component yesNo(boolean value) {
		return Component.translatable(value ? "options.on" : "options.off");
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
