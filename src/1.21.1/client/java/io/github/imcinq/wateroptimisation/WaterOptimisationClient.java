package io.github.imcinq.wateroptimisation;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public final class WaterOptimisationClient implements ClientModInitializer {
	public static final String MOD_ID = "wateroptimisation";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final String KEY_CATEGORY = KeyMapping.CATEGORY_MISC;
	private static KeyMapping openConfigKey;
	private static volatile boolean sodiumLoaded;
	private static volatile ParticleFilterSettings particleFilterSettings = ParticleFilterSettings.INACTIVE;
	private static volatile ParticleReference particleReference = ParticleReference.UNAVAILABLE;
	private static final AtomicInteger PARTICLE_BUDGET_USED = new AtomicInteger();
	private static final long DIAGNOSTICS_REFRESH_INTERVAL_NANOS = 250_000_000L;
	private static long diagnosticsRefreshDeadlineNanos;
	private static Component[] diagnosticsLines = new Component[0];

	@Override
	public void onInitializeClient() {
		ConfigManager.load();
		sodiumLoaded = FabricLoader.getInstance().isModLoaded("sodium");
		FluidOptimizationPolicy.refresh();
		openConfigKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.wateroptimisation.open_config",
				InputConstants.Type.KEYSYM,
				InputConstants.KEY_O,
				KEY_CATEGORY
		));

		ClientTickEvents.START_CLIENT_TICK.register(client -> beginParticleTick());
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			updateParticleReference(client);
			while (openConfigKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new WaterOptimisationScreen(null));
				}
			}
		});

		HudRenderCallback.EVENT.register(WaterOptimisationClient::renderDiagnostics);

		if (sodiumLoaded) {
			LOGGER.info("Sodium detected on Minecraft 1.21.1; vanilla fluid optimization hooks are disabled and this target remains on the particle-only fallback.");
		}
		LOGGER.info("Water Optimisation initialized for Minecraft 1.21.1; rendering changes are opt-in and default to safe no-op behavior.");
	}

	public static boolean isSodiumLoaded() {
		return sodiumLoaded;
	}

	/** The 1.21.1 renderer has no reviewed reverse-face hook yet. */
	public static boolean supportsReducedWaterBackfaces() {
		return false;
	}

	public static RendererCapabilities rendererCapabilities() {
		return new RendererCapabilities(sodiumLoaded, sodiumLoaded ? "Sodium" : "Vanilla");
	}

	public static EffectiveWaterPolicy effectivePolicy(WaterOptimisationConfig config) {
		return EffectiveWaterPolicy.resolve(config, rendererCapabilities());
	}

	/**
	 * Describes the path that the current working configuration can actually
	 * use. This is intentionally based on the supplied copy rather than the
	 * saved global policy, so the settings screen stays truthful before Apply.
	 */
	public static Component effectivePath(WaterOptimisationConfig config) {
		return Component.translatable(effectivePolicy(config).geometryPath().translationKey());
	}

	public static void refreshParticleFiltering(WaterOptimisationConfig config) {
		if (config == null) {
			particleFilterSettings = ParticleFilterSettings.INACTIVE;
			return;
		}

		EffectiveWaterPolicy policy = effectivePolicy(config);
		double maxDistance = WaterParticleDistancePolicy.effectiveDistance(config);
		particleFilterSettings = new ParticleFilterSettings(
				policy.particleFilteringActive(),
				config.isWaterParticles(),
				maxDistance * maxDistance,
				policy.particleBudget(),
				policy.limitForcedWaterParticles()
		);
	}

	public static boolean shouldKeepWaterParticle(ParticleOptions particle, boolean alwaysShow, double x, double y, double z) {
		ParticleFilterSettings settings = particleFilterSettings;
		if (!settings.active()) {
			return true;
		}
		if (!isWaterParticle(particle)) {
			return true;
		}
		boolean diagnosticsEnabled = Diagnostics.isEnabled();
		if (diagnosticsEnabled) {
			Diagnostics.recordParticleCandidate();
		}

		if (alwaysShow && !settings.limitForcedWaterParticles()) {
			if (diagnosticsEnabled) {
				Diagnostics.recordParticleForcedPreserved();
			}
			return true;
		}
		if (!settings.keepWaterParticles()) {
			if (diagnosticsEnabled) {
				Diagnostics.recordParticleRejected(false);
			}
			return false;
		}

		ParticleReference reference = particleReference;
		if (reference.available() && !WaterParticleDistancePolicy.isWithinDistanceSquared(
				settings.maxDistanceSquared(),
				reference.x(), reference.y(), reference.z(),
				x, y, z
		)) {
			if (diagnosticsEnabled) {
				Diagnostics.recordParticleRejected(true);
			}
			return false;
		}
		if (settings.particleBudget() > WaterOptimisationConfig.UNLIMITED_PARTICLE_BUDGET
				&& !reserveParticleBudget(settings.particleBudget())) {
			if (diagnosticsEnabled) {
				Diagnostics.recordParticleBudgetRejected();
			}
			return false;
		}
		return true;
	}

	private static void beginParticleTick() {
		PARTICLE_BUDGET_USED.set(0);
	}

	private static boolean reserveParticleBudget(int budget) {
		int current;
		do {
			current = PARTICLE_BUDGET_USED.get();
			if (current >= budget) {
				return false;
			}
		} while (!PARTICLE_BUDGET_USED.compareAndSet(current, current + 1));
		return true;
	}

	private static void updateParticleReference(Minecraft client) {
		if (client.player == null) {
			particleReference = ParticleReference.UNAVAILABLE;
			return;
		}

		double referenceX = client.player.getX();
		double referenceY = client.player.getY();
		double referenceZ = client.player.getZ();
		Camera camera = client.gameRenderer.getMainCamera();
		if (camera.isInitialized()) {
			Vec3 cameraPosition = camera.getPosition();
			referenceX = cameraPosition.x;
			referenceY = cameraPosition.y;
			referenceZ = cameraPosition.z;
		}
		particleReference = new ParticleReference(referenceX, referenceY, referenceZ, true);
	}

	private static boolean isWaterParticle(ParticleOptions particle) {
		ParticleType<?> type = particle.getType();
		return type == ParticleTypes.BUBBLE
			|| type == ParticleTypes.BUBBLE_COLUMN_UP
			|| type == ParticleTypes.BUBBLE_POP
			|| type == ParticleTypes.CURRENT_DOWN
			|| type == ParticleTypes.DRIPPING_WATER
			|| type == ParticleTypes.FALLING_WATER
			|| type == ParticleTypes.SPLASH
			|| type == ParticleTypes.UNDERWATER;
	}

	private static void renderDiagnostics(GuiGraphics graphics, DeltaTracker deltaTracker) {
		WaterOptimisationConfig config = ConfigManager.get();
		if (!config.isDiagnosticsHud()) {
			return;
		}

		Minecraft client = Minecraft.getInstance();
		long now = System.nanoTime();
		if (diagnosticsLines.length == 0 || now >= diagnosticsRefreshDeadlineNanos) {
			Diagnostics.Snapshot snapshot = Diagnostics.snapshot();
			EffectiveWaterPolicy policy = effectivePolicy(config);
			diagnosticsLines = new Component[]{
				Component.literal("Water Optimisation"),
				Component.literal("renderer: " + rendererCapabilities().rendererName()),
				Component.literal("geometry path: " + policy.geometryPath().name().toLowerCase(java.util.Locale.ROOT)),
				Component.literal("fluid hooks: " + onOff(FluidOptimizationPolicy.fluidHooksActive())),
				Component.literal("fast path: " + onOff(FluidOptimizationPolicy.flatWaterFastPathActive())),
				Component.literal("water backfaces: " + (sodiumLoaded ? "Sodium-owned" : "vanilla (1.21.1 compatibility)")),
				Component.literal("fluid blocks: " + snapshot.fluidBlocksVisited()),
				Component.literal("fast-path skips: " + snapshot.fluidFastPathSkips()),
				Component.literal("mod reverse faces removed: " + snapshot.reducedWaterBackfaces()),
				Component.literal("fluid avg (1/16): " + String.format(java.util.Locale.ROOT, "%.3f ms", snapshot.averageFluidCompileMillis())),
				Component.literal("section compile avg: " + String.format(java.util.Locale.ROOT, "%.3f ms", snapshot.averageSectionCompileMillis())),
				Component.literal("translucent resort avg: " + String.format(java.util.Locale.ROOT, "%.3f ms", snapshot.averageTranslucentResortMillis())),
				Component.literal("particle budget: " + particleBudgetLabel(policy.particleBudget())),
				Component.literal("budget rejects: " + snapshot.particleBudgetRejected()),
				Component.literal("particles rejected: " + snapshot.particleRejected() + "/" + snapshot.particleCandidates())
			};
			diagnosticsRefreshDeadlineNanos = now + DIAGNOSTICS_REFRESH_INTERVAL_NANOS;
		}

		int x = 6;
		int y = 6;
		int lineHeight = client.font.lineHeight + 2;
		int lines = diagnosticsLines.length;
		graphics.fill(x - 3, y - 3, x + 290, y + lineHeight * lines + 2, 0x90000000);
		for (int index = 0; index < lines; index++) {
			graphics.drawString(client.font, diagnosticsLines[index], x, y + lineHeight * index, 0xFFFFFFFF);
		}
	}

	private static String onOff(boolean value) {
		return value ? "on" : "off";
	}

	private static String particleBudgetLabel(int budget) {
		return budget == WaterOptimisationConfig.UNLIMITED_PARTICLE_BUDGET ? "unlimited" : budget + "/tick";
	}

	private record ParticleFilterSettings(
			boolean active,
			boolean keepWaterParticles,
			double maxDistanceSquared,
			int particleBudget,
			boolean limitForcedWaterParticles
	) {
		private static final ParticleFilterSettings INACTIVE = new ParticleFilterSettings(false, true, 0.0D, 0, false);
	}

	private record ParticleReference(double x, double y, double z, boolean available) {
		private static final ParticleReference UNAVAILABLE = new ParticleReference(0.0D, 0.0D, 0.0D, false);
	}
}
