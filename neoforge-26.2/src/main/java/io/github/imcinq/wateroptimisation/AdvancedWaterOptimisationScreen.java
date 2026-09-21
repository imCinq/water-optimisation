package io.github.imcinq.wateroptimisation;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class AdvancedWaterOptimisationScreen extends Screen {
	private static final int SIDE_MARGIN = 24;
	private static final int MAX_TEXT_WIDTH = 660;
	private static final int MAX_BUTTON_WIDTH = 660;
	private static final int COLUMN_GAP = 12;
	private static final int SECTION_GAP = 8;
	private static final int BUTTON_HEIGHT = 20;
	private static final int BUTTON_GAP = 4;

	private final Screen parent;
	private final WaterOptimisationConfig workingCopy;
	private Button cullingButton;
	private Button fastPathButton;
	private Button particlesButton;
	private Button particleDistanceButton;
	private Button fogButton;
	private Button particleBudgetButton;
	private Button forcedParticlesButton;
	private Button diagnosticsButton;
	private Button resetButton;
	private Button doneButton;
	private int contentWidth;
	private int buttonLeft;
	private int buttonWidth;
	private int columnWidth;
	private int rightColumnLeft;
	private int descriptionY;
	private int safeSectionY;
	private int experimentalSectionY;
	private int diagnosticsSectionY;
	private int actionY;
	private int sodiumNoticeY;
	private int viewportTop;
	private int viewportBottom;
	private int contentBottom;
	private int maxScrollOffset;
	private int scrollOffset;
	private boolean columns;

	public AdvancedWaterOptimisationScreen(Screen parent, WaterOptimisationConfig workingCopy) {
		super(Component.translatable("screen.wateroptimisation.advanced.title"));
		this.parent = parent;
		this.workingCopy = workingCopy;
	}

	@Override
	protected void init() {
		this.contentWidth = Math.max(1, Math.min(MAX_TEXT_WIDTH, this.width - SIDE_MARGIN * 2));
		this.buttonWidth = Math.max(1, Math.min(MAX_BUTTON_WIDTH, this.contentWidth));
		this.buttonLeft = (this.width - this.buttonWidth) / 2;
		this.descriptionY = 16;

		Component description = Component.translatable("screen.wateroptimisation.advanced.description");
		int y = this.descriptionY + wrappedHeight(description) + 8;
		if (WaterOptimisationClient.isSodiumLoaded()) {
			this.sodiumNoticeY = y;
			y += wrappedHeight(sodiumNotice()) + 8;
		} else {
			this.sodiumNoticeY = -1;
		}
		this.actionY = this.height - 34;
		this.viewportTop = y;
		this.viewportBottom = Math.max(this.viewportTop + BUTTON_HEIGHT, this.actionY - 8);
		this.scrollOffset = 0;
		this.columns = this.contentWidth >= 600
				|| (this.contentWidth >= 360 && singleColumnBottom(y) > this.height - 42);
		if (this.columns) {
			this.columnWidth = (this.contentWidth - COLUMN_GAP) / 2;
			this.buttonWidth = this.columnWidth;
			this.buttonLeft = (this.width - (this.columnWidth * 2 + COLUMN_GAP)) / 2;
			this.rightColumnLeft = this.buttonLeft + this.columnWidth + COLUMN_GAP;
			initColumns(y);
		} else {
			this.columnWidth = this.buttonWidth;
			this.rightColumnLeft = this.buttonLeft;
			initSingleColumn(y);
		}

		int actionWidth = Math.max(1, (this.buttonWidth - 10) / 2);
		this.resetButton = this.addRenderableWidget(Button.builder(
				Component.translatable("screen.wateroptimisation.reset"),
				button -> {
					this.workingCopy.resetToProfile();
					this.rebuildWidgets();
				}
		).bounds(this.buttonLeft, this.actionY, actionWidth, BUTTON_HEIGHT).build());

		this.doneButton = this.addRenderableWidget(Button.builder(
				Component.translatable("gui.done"),
				button -> this.minecraft.gui.setScreen(this.parent)
		).bounds(this.buttonLeft + actionWidth + 10, this.actionY, this.buttonWidth - actionWidth - 10, BUTTON_HEIGHT).build());
		this.contentBottom = calculateContentBottom();
		this.maxScrollOffset = Math.max(0, this.contentBottom - this.viewportBottom);
		updateContentVisibility();
	}

	private void initColumns(int top) {
		this.safeSectionY = top;
		this.experimentalSectionY = top;
		int controlsY = top + lineHeight() + 4;

		this.fastPathButton = addFastPathButton(this.buttonLeft, controlsY);
		this.particlesButton = addParticlesButton(this.buttonLeft, controlsY + BUTTON_HEIGHT + BUTTON_GAP);
		this.particleDistanceButton = addParticleDistanceButton(this.buttonLeft, controlsY + 2 * (BUTTON_HEIGHT + BUTTON_GAP));
		this.fogButton = addFogButton(this.buttonLeft, controlsY + 3 * (BUTTON_HEIGHT + BUTTON_GAP));
		this.particleBudgetButton = addParticleBudgetButton(this.buttonLeft, controlsY + 4 * (BUTTON_HEIGHT + BUTTON_GAP));
		this.forcedParticlesButton = addForcedParticlesButton(this.buttonLeft, controlsY + 5 * (BUTTON_HEIGHT + BUTTON_GAP));

		this.cullingButton = addCullingButton(this.rightColumnLeft, controlsY);
		this.diagnosticsSectionY = controlsY + BUTTON_HEIGHT + BUTTON_GAP + 8;
		int diagnosticsY = this.diagnosticsSectionY + lineHeight() + 4;
		this.diagnosticsButton = addDiagnosticsButton(this.rightColumnLeft, diagnosticsY);
	}

	private void initSingleColumn(int top) {
		this.safeSectionY = top;
		int controlsY = top + lineHeight() + 4;
		this.fastPathButton = addFastPathButton(this.buttonLeft, controlsY);
		this.particlesButton = addParticlesButton(this.buttonLeft, controlsY + BUTTON_HEIGHT + BUTTON_GAP);
		this.particleDistanceButton = addParticleDistanceButton(this.buttonLeft, controlsY + 2 * (BUTTON_HEIGHT + BUTTON_GAP));
		this.fogButton = addFogButton(this.buttonLeft, controlsY + 3 * (BUTTON_HEIGHT + BUTTON_GAP));
		this.particleBudgetButton = addParticleBudgetButton(this.buttonLeft, controlsY + 4 * (BUTTON_HEIGHT + BUTTON_GAP));
		this.forcedParticlesButton = addForcedParticlesButton(this.buttonLeft, controlsY + 5 * (BUTTON_HEIGHT + BUTTON_GAP));
		int safeBottom = controlsY + 6 * (BUTTON_HEIGHT + BUTTON_GAP) - BUTTON_GAP;

		this.experimentalSectionY = safeBottom + SECTION_GAP;
		int cullingY = this.experimentalSectionY + lineHeight() + 4;
		this.cullingButton = addCullingButton(this.buttonLeft, cullingY);

		this.diagnosticsSectionY = cullingY + BUTTON_HEIGHT + BUTTON_GAP + SECTION_GAP;
		int diagnosticsY = this.diagnosticsSectionY + lineHeight() + 4;
		this.diagnosticsButton = addDiagnosticsButton(this.buttonLeft, diagnosticsY);
	}

	private int singleColumnBottom(int top) {
		int controlsY = top + lineHeight() + 4;
		int safeBottom = controlsY + 6 * (BUTTON_HEIGHT + BUTTON_GAP) - BUTTON_GAP;
		int cullingY = safeBottom + SECTION_GAP + lineHeight() + 4;
		int diagnosticsY = cullingY + BUTTON_HEIGHT + BUTTON_GAP + SECTION_GAP + lineHeight() + 4;
		return diagnosticsY + BUTTON_HEIGHT;
	}

	private Button[] contentButtons() {
		return new Button[] {
				this.fastPathButton,
				this.particlesButton,
				this.particleDistanceButton,
				this.fogButton,
				this.particleBudgetButton,
				this.forcedParticlesButton,
				this.cullingButton,
				this.diagnosticsButton
		};
	}

	private int calculateContentBottom() {
		int bottom = this.viewportTop;
		for (Button button : contentButtons()) {
			bottom = Math.max(bottom, button.getBottom());
		}
		return bottom + 8;
	}

	private void updateContentVisibility() {
		for (Button button : contentButtons()) {
			button.visible = this.maxScrollOffset == 0
					|| (button.getBottom() > this.viewportTop && button.getY() < this.viewportBottom);
		}
	}

	private void setContentVisible(boolean visible) {
		for (Button button : contentButtons()) {
			button.visible = visible;
		}
	}

	private void setFooterVisible(boolean visible) {
		this.resetButton.visible = visible;
		this.doneButton.visible = visible;
	}

	private void applyScrollOffset(int newOffset) {
		newOffset = Math.max(0, Math.min(this.maxScrollOffset, newOffset));
		int delta = this.scrollOffset - newOffset;
		if (delta == 0) {
			return;
		}
		for (Button button : contentButtons()) {
			button.setY(button.getY() + delta);
		}
		this.safeSectionY += delta;
		this.experimentalSectionY += delta;
		this.diagnosticsSectionY += delta;
		this.scrollOffset = newOffset;
		updateContentVisibility();
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.maxScrollOffset > 0
				&& mouseY >= this.viewportTop
				&& mouseY <= this.viewportBottom
				&& verticalAmount != 0) {
			int nextOffset = this.scrollOffset - (int) Math.round(verticalAmount * (BUTTON_HEIGHT + BUTTON_GAP));
			int previousOffset = this.scrollOffset;
			applyScrollOffset(nextOffset);
			if (this.scrollOffset != previousOffset) {
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	private Button addCullingButton(int left, int top) {
		Button button = this.addRenderableWidget(Button.builder(cullingLabel(), clicked -> {
			SettingsPresentation.setReducedInwardFaces(this.workingCopy, !SettingsPresentation.reducedInwardFaces(this.workingCopy));
			clicked.setMessage(cullingLabel());
		}).bounds(left, top, this.buttonWidth, BUTTON_HEIGHT).build());
		button.setTooltip(Tooltip.create(Component.translatable("screen.wateroptimisation.reduced_faces.tooltip")));
		button.active = !WaterOptimisationClient.isSodiumLoaded()
				&& WaterOptimisationClient.supportsReducedWaterBackfaces();
		return button;
	}

	private Button addFastPathButton(int left, int top) {
		Button button = this.addRenderableWidget(Button.builder(fastPathLabel(), clicked -> {
			SettingsPresentation.setHiddenWaterSkipping(this.workingCopy, !SettingsPresentation.hiddenWaterSkipping(this.workingCopy));
			clicked.setMessage(fastPathLabel());
		}).bounds(left, top, this.buttonWidth, BUTTON_HEIGHT).build());
		button.setTooltip(Tooltip.create(Component.translatable("screen.wateroptimisation.fast_path.tooltip")));
		button.active = !WaterOptimisationClient.isSodiumLoaded();
		return button;
	}

	private Button addParticlesButton(int left, int top) {
		return this.addRenderableWidget(Button.builder(particlesLabel(), button -> {
			this.workingCopy.setWaterParticles(!this.workingCopy.isWaterParticles());
			button.setMessage(particlesLabel());
		}).bounds(left, top, this.buttonWidth, BUTTON_HEIGHT).build());
	}

	private Button addParticleDistanceButton(int left, int top) {
		return this.addRenderableWidget(Button.builder(particleDistanceLabel(), button -> {
			int distance = this.workingCopy.getParticleDistance();
			this.workingCopy.setParticleDistance(distance >= WaterOptimisationConfig.MAX_PARTICLE_DISTANCE
					? WaterOptimisationConfig.MIN_PARTICLE_DISTANCE
					: distance * 2);
			button.setMessage(particleDistanceLabel());
		}).bounds(left, top, this.buttonWidth, BUTTON_HEIGHT).build());
	}

	private Button addFogButton(int left, int top) {
		return this.addRenderableWidget(Button.builder(fogLabel(), button -> {
			this.workingCopy.setParticleFogCulling(!this.workingCopy.isParticleFogCulling());
			button.setMessage(fogLabel());
		}).bounds(left, top, this.buttonWidth, BUTTON_HEIGHT).build());
	}

	private Button addParticleBudgetButton(int left, int top) {
		return this.addRenderableWidget(Button.builder(particleBudgetLabel(), button -> {
			this.workingCopy.setParticleBudget(WaterOptimisationConfig.nextParticleBudget(this.workingCopy.getParticleBudget()));
			button.setMessage(particleBudgetLabel());
		}).bounds(left, top, this.buttonWidth, BUTTON_HEIGHT).build());
	}

	private Button addForcedParticlesButton(int left, int top) {
		return this.addRenderableWidget(Button.builder(forcedParticlesLabel(), button -> {
			this.workingCopy.setLimitForcedWaterParticles(!this.workingCopy.isLimitForcedWaterParticles());
			button.setMessage(forcedParticlesLabel());
		}).bounds(left, top, this.buttonWidth, BUTTON_HEIGHT).build());
	}

	private Button addDiagnosticsButton(int left, int top) {
		return this.addRenderableWidget(Button.builder(diagnosticsLabel(), button -> {
			this.workingCopy.setDiagnosticsHud(!this.workingCopy.isDiagnosticsHud());
			button.setMessage(diagnosticsLabel());
		}).bounds(left, top, this.buttonWidth, BUTTON_HEIGHT).build());
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		setFooterVisible(false);
		graphics.enableScissor(contentClipLeft(), this.viewportTop, contentClipRight(), this.viewportBottom);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		graphics.disableScissor();
		setContentVisible(false);
		setFooterVisible(true);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		setContentVisible(true);
		updateContentVisibility();
		drawCenteredWrapped(graphics, Component.translatable("screen.wateroptimisation.advanced.description"), this.descriptionY, 0xFFFFFFFF);
		if (this.sodiumNoticeY >= 0) {
			drawCenteredWrapped(graphics, sodiumNotice(), this.sodiumNoticeY, 0xFFB0B0B0);
		}
		graphics.enableScissor(contentClipLeft(), this.viewportTop, contentClipRight(), this.viewportBottom);
		if (this.columns) {
			drawSectionLabel(graphics, Component.translatable("screen.wateroptimisation.section.safe"), this.buttonLeft, this.safeSectionY, this.columnWidth);
			drawSectionLabel(graphics, Component.translatable("screen.wateroptimisation.section.experimental"), this.rightColumnLeft, this.experimentalSectionY, this.columnWidth);
			drawSectionLabel(graphics, Component.translatable("screen.wateroptimisation.section.diagnostics"), this.rightColumnLeft, this.diagnosticsSectionY, this.columnWidth);
		} else {
			drawSectionLabel(graphics, Component.translatable("screen.wateroptimisation.section.safe"), this.buttonLeft, this.safeSectionY, this.buttonWidth);
			drawSectionLabel(graphics, Component.translatable("screen.wateroptimisation.section.experimental"), this.buttonLeft, this.experimentalSectionY, this.buttonWidth);
			drawSectionLabel(graphics, Component.translatable("screen.wateroptimisation.section.diagnostics"), this.buttonLeft, this.diagnosticsSectionY, this.buttonWidth);
		}
		graphics.disableScissor();
	}

	private int contentClipLeft() {
		return Math.max(0, this.buttonLeft - 2);
	}

	private int contentClipRight() {
		int right = this.columns ? this.rightColumnLeft + this.columnWidth : this.buttonLeft + this.buttonWidth;
		return Math.min(this.width, right + 2);
	}

	@Override
	public void onClose() {
		this.minecraft.gui.setScreen(this.parent);
	}

	private Component cullingLabel() {
		if (WaterOptimisationClient.isSodiumLoaded()) {
			return Component.translatable("screen.wateroptimisation.culling_sodium_unavailable")
					.withStyle(ChatFormatting.GRAY);
		}
		return Component.translatable("screen.wateroptimisation.reduced_faces", yesNo(SettingsPresentation.reducedInwardFaces(this.workingCopy)))
				.withStyle(SettingsPresentation.reducedInwardFaces(this.workingCopy)
						? ChatFormatting.RED
						: ChatFormatting.WHITE);
	}

	private Component fastPathLabel() {
		if (WaterOptimisationClient.isSodiumLoaded()) {
			return Component.translatable("screen.wateroptimisation.fast_path_unavailable")
					.withStyle(ChatFormatting.GRAY);
		}
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

	private Component particleBudgetLabel() {
		return Component.translatable("screen.wateroptimisation.particle_budget", particleBudgetValue());
	}

	private Component forcedParticlesLabel() {
		return Component.translatable("screen.wateroptimisation.forced_particles", yesNo(this.workingCopy.isLimitForcedWaterParticles()));
	}

	private Component particleBudgetValue() {
		return this.workingCopy.getParticleBudget() == WaterOptimisationConfig.UNLIMITED_PARTICLE_BUDGET
				? Component.translatable("wateroptimisation.particle_budget.unlimited")
				: Component.literal(this.workingCopy.getParticleBudget() + "/tick");
	}

	private Component diagnosticsLabel() {
		return Component.translatable("screen.wateroptimisation.diagnostics", yesNo(this.workingCopy.isDiagnosticsHud()));
	}

	private Component sodiumNotice() {
		return Component.translatable("screen.wateroptimisation.sodium_notice");
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

	private void drawSectionLabel(GuiGraphicsExtractor graphics, Component text, int left, int top, int width) {
		graphics.text(this.font, text, left, top, 0xFFE0E0E0, true);
		int lineY = top + lineHeight() - 2;
		graphics.fill(left, lineY, left + width, lineY + 1, 0x66555555);
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
