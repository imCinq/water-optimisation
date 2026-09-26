package io.github.imcinq.wateroptimisation;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

/**
 * Tabbed settings screen. Every option sits in one row: an icon, the option
 * button, and a plain-language explanation underneath. Changes are kept in a
 * working copy until Done is pressed.
 */
public final class WaterOptimisationScreen extends Screen {
	private static final int SIDE_MARGIN = 16;
	private static final int MAX_ROW_WIDTH = 380;
	private static final int BUTTON_HEIGHT = 20;
	private static final int ICON_SIZE = 16;
	private static final int ICON_SLOT = 22;
	private static final int ROW_GAP = 10;
	private static final int TITLE_Y = 10;
	private static final int TABS_Y = 26;
	private static final int TAB_GAP = 4;
	private static final String ICON_SPACER = "     ";
	private static final int TITLE_COLOR = 0xFFFFFFFF;
	private static final int TEXT_COLOR = 0xFFE0E0E0;
	private static final int DESCRIPTION_COLOR = 0xFFA8A8A8;
	private static final int NOTE_COLOR = 0xFFFFD166;
	private static final int ACCENT_COLOR = 0xFF4FA8FF;
	private static final int SEPARATOR_COLOR = 0x66FFFFFF;

	private static final Identifier ICON_GENERAL = icon("settings");
	private static final Identifier ICON_WATER = icon("water");
	private static final Identifier ICON_PARTICLES = icon("particles");
	private static final Identifier ICON_POWER = icon("power");
	private static final Identifier ICON_PRESET = icon("preset");
	private static final Identifier ICON_RESET = icon("autorenew");
	private static final Identifier ICON_STATS = icon("stats");
	private static final Identifier ICON_FACES = icon("faces");
	private static final Identifier ICON_DISTANCE = icon("distance");
	private static final Identifier ICON_FOG = icon("fog");
	private static final Identifier ICON_BUDGET = icon("budget");
	private static final Identifier ICON_FORCED = icon("forced");

	private enum Tab {
		GENERAL("general", ICON_GENERAL),
		WATER("water", ICON_WATER),
		PARTICLES("particles", ICON_PARTICLES);

		private final String key;
		private final Identifier icon;

		Tab(String key, Identifier icon) {
			this.key = key;
			this.icon = icon;
		}
	}

	private record TextLine(FormattedCharSequence text, int color) {
	}

	private record Row(Identifier icon, Button button, List<TextLine> lines, int top) {
		int textTop() {
			return this.button == null ? this.top : this.top + BUTTON_HEIGHT + 3;
		}
	}

	/** Remembers the last tab for the rest of the session. */
	private static Tab lastTab = Tab.GENERAL;

	private final Screen parent;
	private final WaterOptimisationConfig workingCopy;
	private final List<Row> rows = new ArrayList<>();
	private final List<Button> chromeButtons = new ArrayList<>();
	private final Button[] tabButtons = new Button[Tab.values().length];
	private Tab tab = lastTab;
	private int rowLeft;
	private int rowWidth;
	private int viewportTop;
	private int viewportBottom;
	private int contentHeight;
	private int scrollOffset;
	private int maxScrollOffset;

	public WaterOptimisationScreen(Screen parent) {
		this(parent, ConfigManager.copy());
	}

	WaterOptimisationScreen(Screen parent, WaterOptimisationConfig workingCopy) {
		super(Component.translatable("screen.wateroptimisation.title"));
		this.parent = parent;
		this.workingCopy = workingCopy;
	}

	private static Identifier icon(String name) {
		return Identifier.fromNamespaceAndPath(WaterOptimisationClient.MOD_ID, "icons/" + name);
	}

	@Override
	protected void init() {
		this.rows.clear();
		this.chromeButtons.clear();
		this.rowWidth = Math.max(1, Math.min(MAX_ROW_WIDTH, this.width - SIDE_MARGIN * 2));
		this.rowLeft = (this.width - this.rowWidth) / 2;

		// Tabs and footer are added first so they win clicks over scrolled rows.
		Tab[] tabs = Tab.values();
		int tabWidth = Math.max(1, (this.rowWidth - TAB_GAP * (tabs.length - 1)) / tabs.length);
		for (int index = 0; index < tabs.length; index++) {
			Tab target = tabs[index];
			this.tabButtons[index] = addChrome(Button.builder(tabLabel(target), button -> selectTab(target))
					.bounds(this.rowLeft + index * (tabWidth + TAB_GAP), TABS_Y, tabWidth, BUTTON_HEIGHT)
					.build());
		}

		int footerY = this.height - 26;
		int actionWidth = Math.max(1, (this.rowWidth - 8) / 2);
		addChrome(Button.builder(Component.translatable("gui.done"), button -> saveAndClose())
				.bounds(this.rowLeft, footerY, actionWidth, BUTTON_HEIGHT)
				.build());
		addChrome(Button.builder(Component.translatable("gui.cancel"), button -> closeWithoutSaving())
				.bounds(this.rowLeft + actionWidth + 8, footerY, this.rowWidth - actionWidth - 8, BUTTON_HEIGHT)
				.build());

		this.viewportTop = TABS_Y + BUTTON_HEIGHT + 6;
		this.viewportBottom = Math.max(this.viewportTop + BUTTON_HEIGHT, footerY - 6);
		this.contentHeight = 6;
		switch (this.tab) {
			case GENERAL -> initGeneralTab();
			case WATER -> initWaterTab();
			case PARTICLES -> initParticlesTab();
		}
		this.maxScrollOffset = Math.max(0, this.contentHeight - (this.viewportBottom - this.viewportTop));
		this.scrollOffset = Math.max(0, Math.min(this.maxScrollOffset, this.scrollOffset));
		updateContentPositions();
	}

	private void initGeneralTab() {
		addText(null, List.of(
				line(Component.translatable("screen.wateroptimisation.description"), DESCRIPTION_COLOR),
				line(Component.translatable("screen.wateroptimisation.effective_path", WaterOptimisationClient.effectivePath(this.workingCopy)), TEXT_COLOR)
		));

		boolean enabled = this.workingCopy.isEnabled();
		addOption(ICON_POWER,
				optionLabel("screen.wateroptimisation.enabled", onOff(enabled)),
				() -> this.workingCopy.setEnabled(!this.workingCopy.isEnabled()),
				true,
				Component.translatable("screen.wateroptimisation.enabled.description"),
				null);

		addOption(ICON_PRESET,
				optionLabel("screen.wateroptimisation.profile",
						Component.translatable(SettingsPresentation.profileTranslationKey(this.workingCopy))),
				() -> this.workingCopy.selectProfile(this.workingCopy.getPerformanceProfile().next()),
				true,
				profileDescription(),
				enabled ? null : Component.translatable("screen.wateroptimisation.note.mod_off"));

		boolean custom = SettingsPresentation.isCustom(this.workingCopy);
		addOption(ICON_RESET,
				Component.translatable("screen.wateroptimisation.reset"),
				this.workingCopy::resetToProfile,
				custom,
				Component.translatable("screen.wateroptimisation.reset.description"),
				custom ? null : Component.translatable("screen.wateroptimisation.note.nothing_to_reset"));

		addOption(ICON_STATS,
				optionLabel("screen.wateroptimisation.diagnostics", onOff(this.workingCopy.isDiagnosticsHud())),
				() -> this.workingCopy.setDiagnosticsHud(!this.workingCopy.isDiagnosticsHud()),
				true,
				Component.translatable("screen.wateroptimisation.diagnostics.description"),
				null);
	}

	private void initWaterTab() {
		addInactiveNotice();
		boolean sodiumLoaded = WaterOptimisationClient.isSodiumLoaded();
		if (sodiumLoaded) {
			addText(ICON_GENERAL, List.of(line(Component.translatable("screen.wateroptimisation.sodium_notice"), NOTE_COLOR)));
		}

		boolean hiddenWater = SettingsPresentation.hiddenWaterSkipping(this.workingCopy);
		addOption(ICON_WATER,
				optionLabel("screen.wateroptimisation.fast_path", sodiumLoaded ? unavailable() : onOff(hiddenWater)),
				() -> SettingsPresentation.setHiddenWaterSkipping(this.workingCopy, !hiddenWater),
				!sodiumLoaded,
				Component.translatable("screen.wateroptimisation.fast_path.description"),
				null);

		boolean rendererSupported = WaterOptimisationClient.supportsReducedWaterBackfaces();
		boolean reducedFaces = SettingsPresentation.reducedInwardFaces(this.workingCopy);
		boolean available = !sodiumLoaded && rendererSupported;
		// Sodium is already explained by the banner above.
		Component reason = !sodiumLoaded && !rendererSupported
				? Component.translatable("screen.wateroptimisation.note.unsupported_version")
				: null;
		addOption(ICON_FACES,
				optionLabel("screen.wateroptimisation.reduced_faces", available ? onOff(reducedFaces) : unavailable()),
				() -> SettingsPresentation.setReducedInwardFaces(this.workingCopy, !reducedFaces),
				available,
				Component.translatable("screen.wateroptimisation.reduced_faces.description"),
				reason);
	}

	private void initParticlesTab() {
		addInactiveNotice();
		boolean particles = this.workingCopy.isWaterParticles();
		Component needsParticles = particles ? null : Component.translatable("screen.wateroptimisation.note.needs_particles");

		addOption(ICON_PARTICLES,
				optionLabel("screen.wateroptimisation.particles", onOff(particles)),
				() -> this.workingCopy.setWaterParticles(!particles),
				true,
				Component.translatable("screen.wateroptimisation.particles.description"),
				null);

		addOption(ICON_DISTANCE,
				optionLabel("screen.wateroptimisation.particle_distance",
						Component.translatable("screen.wateroptimisation.blocks", this.workingCopy.getParticleDistance())),
				() -> {
					int distance = this.workingCopy.getParticleDistance();
					this.workingCopy.setParticleDistance(distance >= WaterOptimisationConfig.MAX_PARTICLE_DISTANCE
							? WaterOptimisationConfig.MIN_PARTICLE_DISTANCE
							: distance * 2);
				},
				particles,
				Component.translatable("screen.wateroptimisation.particle_distance.description"),
				needsParticles);

		addOption(ICON_FOG,
				optionLabel("screen.wateroptimisation.fog", onOff(this.workingCopy.isParticleFogCulling())),
				() -> this.workingCopy.setParticleFogCulling(!this.workingCopy.isParticleFogCulling()),
				particles,
				Component.translatable("screen.wateroptimisation.fog.description"),
				needsParticles);

		addOption(ICON_BUDGET,
				optionLabel("screen.wateroptimisation.particle_budget", particleBudgetValue()),
				() -> this.workingCopy.setParticleBudget(WaterOptimisationConfig.nextParticleBudget(this.workingCopy.getParticleBudget())),
				particles,
				Component.translatable("screen.wateroptimisation.particle_budget.description"),
				needsParticles);

		addOption(ICON_FORCED,
				optionLabel("screen.wateroptimisation.forced_particles", onOff(this.workingCopy.isLimitForcedWaterParticles())),
				() -> this.workingCopy.setLimitForcedWaterParticles(!this.workingCopy.isLimitForcedWaterParticles()),
				true,
				Component.translatable("screen.wateroptimisation.forced_particles.description"),
				null);
	}

	/** Explains why Water and Particles settings currently have no effect. */
	private void addInactiveNotice() {
		String key = null;
		if (!this.workingCopy.isEnabled()) {
			key = "screen.wateroptimisation.note.inactive_off";
		} else if (this.workingCopy.getPerformanceProfile() == WaterOptimisationConfig.PerformanceProfile.VANILLA) {
			key = "screen.wateroptimisation.note.inactive_vanilla";
		}
		if (key != null) {
			addText(ICON_POWER, List.of(line(Component.translatable(key), NOTE_COLOR)));
		}
	}

	private void addOption(Identifier icon, Component label, Runnable action, boolean active, Component description, Component note) {
		Button button = this.addRenderableWidget(Button.builder(label, clicked -> {
			action.run();
			// Options affect each other's availability and the preset label,
			// so every change rebuilds the tab.
			this.rebuildWidgets();
		}).bounds(this.rowLeft + ICON_SLOT, 0, this.rowWidth - ICON_SLOT, BUTTON_HEIGHT).build());
		button.active = active;

		List<TextLine> lines = new ArrayList<>(line(description, DESCRIPTION_COLOR));
		if (note != null) {
			lines.addAll(line(note, NOTE_COLOR));
		}
		addRow(new Row(icon, button, lines, this.contentHeight));
	}

	private void addText(Identifier icon, List<List<TextLine>> blocks) {
		List<TextLine> lines = new ArrayList<>();
		for (List<TextLine> block : blocks) {
			lines.addAll(block);
		}
		addRow(new Row(icon, null, lines, this.contentHeight));
	}

	private void addRow(Row row) {
		this.rows.add(row);
		int height = (row.button() == null ? 0 : BUTTON_HEIGHT + 3) + row.lines().size() * lineHeight();
		this.contentHeight += Math.max(ICON_SIZE, height) + ROW_GAP;
	}

	private List<TextLine> line(Component text, int color) {
		List<TextLine> lines = new ArrayList<>();
		for (FormattedCharSequence sequence : this.font.split(text, this.rowWidth - ICON_SLOT)) {
			lines.add(new TextLine(sequence, color));
		}
		return lines;
	}

	private Button addChrome(Button button) {
		this.chromeButtons.add(button);
		return this.addRenderableWidget(button);
	}

	private void selectTab(Tab target) {
		if (this.tab != target) {
			this.tab = target;
			lastTab = target;
			this.scrollOffset = 0;
			this.rebuildWidgets();
		}
	}

	private int rowY(Row row) {
		return this.viewportTop + row.top() - this.scrollOffset;
	}

	private void updateContentPositions() {
		for (Row row : this.rows) {
			if (row.button() != null) {
				int y = rowY(row);
				row.button().setY(y);
				row.button().visible = y + BUTTON_HEIGHT > this.viewportTop && y < this.viewportBottom;
			}
		}
	}

	private void setContentVisible(boolean visible) {
		for (Row row : this.rows) {
			if (row.button() != null) {
				row.button().visible = visible;
			}
		}
	}

	private void setChromeVisible(boolean visible) {
		for (Button button : this.chromeButtons) {
			button.visible = visible;
		}
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		if (this.maxScrollOffset > 0 && verticalAmount != 0) {
			int next = Math.max(0, Math.min(this.maxScrollOffset,
					this.scrollOffset - (int) Math.round(verticalAmount * (BUTTON_HEIGHT + ROW_GAP))));
			if (next != this.scrollOffset) {
				this.scrollOffset = next;
				updateContentPositions();
				return true;
			}
		}
		return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
		// Pass 1: scrollable rows, clipped to the viewport.
		setChromeVisible(false);
		graphics.enableScissor(0, this.viewportTop, this.width, this.viewportBottom);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		for (Row row : this.rows) {
			drawRow(graphics, row);
		}
		graphics.disableScissor();

		// Pass 2: tabs and footer, never clipped.
		setContentVisible(false);
		setChromeVisible(true);
		super.extractRenderState(graphics, mouseX, mouseY, delta);
		updateContentPositions();

		graphics.text(this.font, this.title, (this.width - this.font.width(this.title)) / 2, TITLE_Y, TITLE_COLOR, true);
		Tab[] tabs = Tab.values();
		for (int index = 0; index < tabs.length; index++) {
			Button button = this.tabButtons[index];
			drawIcon(graphics, tabs[index].icon, labelLeft(button), button.getY() + (BUTTON_HEIGHT - ICON_SIZE) / 2);
			if (tabs[index] == this.tab) {
				graphics.fill(button.getX() + 2, button.getBottom() + 1, button.getRight() - 2, button.getBottom() + 3, ACCENT_COLOR);
			}
		}
		graphics.fill(this.rowLeft, this.viewportTop - 1, this.rowLeft + this.rowWidth, this.viewportTop, SEPARATOR_COLOR);
		graphics.fill(this.rowLeft, this.viewportBottom, this.rowLeft + this.rowWidth, this.viewportBottom + 1, SEPARATOR_COLOR);
		drawScrollbar(graphics);
	}

	private void drawRow(GuiGraphicsExtractor graphics, Row row) {
		int top = rowY(row);
		if (top > this.viewportBottom) {
			return;
		}
		if (row.icon() != null) {
			int iconTop = row.button() == null ? top - 4 : top + (BUTTON_HEIGHT - ICON_SIZE) / 2;
			drawIcon(graphics, row.icon(), this.rowLeft, iconTop);
		}
		int y = this.viewportTop + row.textTop() - this.scrollOffset;
		for (TextLine line : row.lines()) {
			graphics.text(this.font, line.text(), this.rowLeft + ICON_SLOT, y, line.color(), false);
			y += lineHeight();
		}
	}

	private void drawIcon(GuiGraphicsExtractor graphics, Identifier icon, int left, int top) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon, left, top, ICON_SIZE, ICON_SIZE);
	}

	private void drawScrollbar(GuiGraphicsExtractor graphics) {
		if (this.maxScrollOffset <= 0) {
			return;
		}
		int left = Math.min(this.width - 3, this.rowLeft + this.rowWidth + 4);
		int trackHeight = this.viewportBottom - this.viewportTop;
		int thumbHeight = Math.max(12, trackHeight * trackHeight / this.contentHeight);
		int thumbTop = this.viewportTop + (trackHeight - thumbHeight) * this.scrollOffset / this.maxScrollOffset;
		graphics.fill(left, this.viewportTop, left + 2, this.viewportBottom, 0x33FFFFFF);
		graphics.fill(left, thumbTop, left + 2, thumbTop + thumbHeight, 0xCCFFFFFF);
	}

	/** Left edge of a centered label that starts with {@link #ICON_SPACER}. */
	private int labelLeft(Button button) {
		int messageWidth = this.font.width(button.getMessage());
		return button.getX() + (button.getWidth() - messageWidth) / 2 - 2;
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

	private Component tabLabel(Tab target) {
		MutableComponent name = Component.translatable("screen.wateroptimisation.tab." + target.key);
		return Component.literal(ICON_SPACER).append(target == this.tab
				? name.withStyle(ChatFormatting.WHITE)
				: name.withStyle(ChatFormatting.GRAY));
	}

	private Component optionLabel(String key, Component value) {
		return Component.translatable("options.generic_value", Component.translatable(key), value);
	}

	private Component onOff(boolean value) {
		return value
				? Component.translatable("options.on").withStyle(ChatFormatting.GREEN)
				: Component.translatable("options.off");
	}

	private Component unavailable() {
		return Component.translatable("screen.wateroptimisation.unavailable").withStyle(ChatFormatting.GRAY);
	}

	private Component particleBudgetValue() {
		int budget = this.workingCopy.getParticleBudget();
		return budget == WaterOptimisationConfig.UNLIMITED_PARTICLE_BUDGET
				? Component.translatable("wateroptimisation.particle_budget.unlimited")
				: Component.translatable("screen.wateroptimisation.per_tick", budget);
	}

	private Component profileDescription() {
		String profileKey = SettingsPresentation.profileTranslationKey(this.workingCopy);
		return Component.translatable(profileKey.replace("wateroptimisation.profile.", "screen.wateroptimisation.profile_description."));
	}

	private int lineHeight() {
		return this.font.lineHeight + 1;
	}
}
