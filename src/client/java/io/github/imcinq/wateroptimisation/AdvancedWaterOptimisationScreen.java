package io.github.imcinq.wateroptimisation;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class AdvancedWaterOptimisationScreen extends Screen {
	private final Screen parent;
	private final WaterOptimisationConfig workingCopy;
	private Button cullingButton;
	private Button fastPathButton;
	private Button particlesButton;
	private Button particleDistanceButton;
	private Button fogButton;
	private Button diagnosticsButton;
	private Button fallbackButton;

	public AdvancedWaterOptimisationScreen(Screen parent, WaterOptimisationConfig workingCopy) {
		super(Component.translatable("screen.wateroptimisation.advanced.title"));
		this.parent = parent;
		this.workingCopy = workingCopy;
	}

	@Override
	protected void init() {
		int left = this.width / 2 - 155;
		this.cullingButton = this.addRenderableWidget(Button.builder(cullingLabel(), button -> {
			this.workingCopy.setFluidCullingMode(this.workingCopy.getFluidCullingMode().next());
			button.setMessage(cullingLabel());
		}).bounds(left, 40, 310, 20).build());

		this.fastPathButton = this.addRenderableWidget(Button.builder(fastPathLabel(), button -> {
			this.workingCopy.setFlatWaterFastPath(!this.workingCopy.isFlatWaterFastPath());
			button.setMessage(fastPathLabel());
		}).bounds(left, 66, 310, 20).build());

		this.particlesButton = this.addRenderableWidget(Button.builder(particlesLabel(), button -> {
			this.workingCopy.setWaterParticles(!this.workingCopy.isWaterParticles());
			button.setMessage(particlesLabel());
		}).bounds(left, 92, 310, 20).build());

		this.particleDistanceButton = this.addRenderableWidget(Button.builder(particleDistanceLabel(), button -> {
			int distance = this.workingCopy.getParticleDistance();
			this.workingCopy.setParticleDistance(distance >= 128 ? 8 : distance * 2);
			button.setMessage(particleDistanceLabel());
		}).bounds(left, 118, 310, 20).build());

		this.fogButton = this.addRenderableWidget(Button.builder(fogLabel(), button -> {
			this.workingCopy.setParticleFogCulling(!this.workingCopy.isParticleFogCulling());
			button.setMessage(fogLabel());
		}).bounds(left, 144, 310, 20).build());

		this.diagnosticsButton = this.addRenderableWidget(Button.builder(diagnosticsLabel(), button -> {
			this.workingCopy.setDiagnosticsHud(!this.workingCopy.isDiagnosticsHud());
			button.setMessage(diagnosticsLabel());
		}).bounds(left, 170, 310, 20).build());

		this.fallbackButton = this.addRenderableWidget(Button.builder(fallbackLabel(), button -> {
			this.workingCopy.setDebugFallbackLogging(!this.workingCopy.isDebugFallbackLogging());
			button.setMessage(fallbackLabel());
		}).bounds(left, 196, 310, 20).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("screen.wateroptimisation.reset"),
				button -> {
					this.workingCopy.resetToProfile();
					this.rebuildWidgets();
				}
		).bounds(left, 226, 150, 20).build());

		this.addRenderableWidget(Button.builder(
				Component.translatable("gui.done"),
				button -> this.minecraft.gui.setScreen(this.parent)
		).bounds(left + 160, 226, 150, 20).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		int left = this.width / 2 - 155;
		graphics.text(this.font, Component.translatable("screen.wateroptimisation.advanced.description"), left, 16, 0xFFFFFFFF, false);
		graphics.text(this.font, Component.translatable("screen.wateroptimisation.advanced.warning"), left, 252, 0xFFFFCC66, false);
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
}
