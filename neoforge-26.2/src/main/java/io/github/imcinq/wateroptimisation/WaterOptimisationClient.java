package io.github.imcinq.wateroptimisation;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

@Mod(value = WaterOptimisationClient.MOD_ID, dist = Dist.CLIENT)
public final class WaterOptimisationClient {
	public static final String MOD_ID = "wateroptimisation";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	private static final KeyMapping.Category KEY_CATEGORY = new KeyMapping.Category(
			Identifier.fromNamespaceAndPath(MOD_ID, "general")
	);
	private static final KeyMapping OPEN_CONFIG_KEY = new KeyMapping(
			"key.wateroptimisation.open_config",
			InputConstants.Type.KEYSYM,
			InputConstants.KEY_O,
			KEY_CATEGORY
	);
	private static volatile boolean sodiumLoaded;
	private static volatile ParticleFilterSettings particleFilterSettings = ParticleFilterSettings.INACTIVE;
	private static volatile ParticleReference particleReference = ParticleReference.UNAVAILABLE;
	private static final AtomicInteger PARTICLE_BUDGET_USED = new AtomicInteger();
	private static final long DIAGNOSTICS_REFRESH_INTERVAL_NANOS = 250_000_000L;
	private static long diagnosticsRefreshDeadlineNanos;
	private static Component[] diagnosticsLines = new Component[0];

	public WaterOptimisationClient(IEventBus modEventBus, ModContainer modContainer) {
		modEventBus.addListener(this::onClientSetup);
		modEventBus.addListener(this::registerKeyMappings);
		modEventBus.addListener(this::registerGuiLayers);

		NeoForge.EVENT_BUS.addListener(this::onClientTickPre);
		NeoForge.EVENT_BUS.addListener(this::onClientTickPost);

		modContainer.registerExtensionPoint(
				IConfigScreenFactory.class,
				(container, parent) -> new WaterOptimisationScreen(parent)
		);
	}

	private void onClientSetup(FMLClientSetupEvent event) {
		sodiumLoaded = ModList.get().isLoaded("sodium");
		ConfigManager.load();
		FluidOptimizationPolicy.refresh();

		if (sodiumLoaded) {
			LOGGER.info("Sodium detected; vanilla fluid optimization hooks are disabled and Sodium remains the water-geometry owner. Water Optimisation applies local particle controls only.");
		}
		LOGGER.info("Water Optimisation initialized; rendering changes are opt-in and default to safe no-op behavior.");
	}

	private void registerKeyMappings(RegisterKeyMappingsEvent event) {
		event.registerCategory(KEY_CATEGORY);
		event.register(OPEN_CONFIG_KEY);
	}

	private void registerGuiLayers(RegisterGuiLayersEvent event) {
		event.registerAboveAll(
				Identifier.fromNamespaceAndPath(MOD_ID, "diagnostics"),
				WaterOptimisationClient::extractDiagnostics
		);
	}

	private void onClientTickPre(ClientTickEvent.Pre event) {
		beginParticleTick();
	}

	private void onClientTickPost(ClientTickEvent.Post event) {
		Minecraft client = Minecraft.getInstance();
		updateParticleReference(client);
		while (OPEN_CONFIG_KEY.consumeClick()) {
			if (client.gui.screen() == null) {
				client.gui.setScreen(new WaterOptimisationScreen(null));
			}
		}
	}

	public static boolean isSodiumLoaded() {
		return sodiumLoaded;
	}

	public static RendererCapabilities rendererCapabilities() {
		return new RendererCapabilities(
				sodiumLoaded,
				sodiumLoaded ? "Sodium" : "Vanilla",
				supportsReducedWaterBackfaces()
		);
	}

	/** The 26.2 renderer has the reviewed optional reverse-face hook. */
	public static boolean supportsReducedWaterBackfaces() {
		return true;
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
			PARTICLE_BUDGET_USED.set(0);
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
		PARTICLE_BUDGET_USED.set(0);
	}

	/** Forces the HUD to rebuild its cached lines after a reset or config change. */
	public static void invalidateDiagnosticsHud() {
		diagnosticsLines = new Component[0];
		diagnosticsRefreshDeadlineNanos = 0L;
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
		ParticleFilterSettings settings = particleFilterSettings;
		if (settings.active() && settings.particleBudget() > WaterOptimisationConfig.UNLIMITED_PARTICLE_BUDGET) {
			PARTICLE_BUDGET_USED.set(0);
		}
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
		Camera camera = client.gameRenderer.mainCamera();
		if (camera.isInitialized()) {
			Vec3 cameraPosition = camera.position();
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

	private static void extractDiagnostics(GuiGraphicsExtractor graphics, DeltaTracker deltaTracker) {
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
				Component.literal("fast path setting: " + onOff(config.isFlatWaterFastPath())),
				Component.literal("fast path active: " + onOff(policy.flatWaterFastPathActive())),
				Component.literal("fast-path hook: " + fastPathHookLabel(policy)),
				Component.literal("water backfaces: " + (sodiumLoaded ? "Sodium-owned" : FluidOptimizationPolicy.reducedWaterBackfacesActive() ? "reduced" : "vanilla")),
				Component.literal("fluid blocks: " + snapshot.fluidBlocksVisited()),
				Component.literal("fast-path skips (actual): " + snapshot.fluidFastPathSkips()),
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
			graphics.text(client.font, diagnosticsLines[index], x, y + lineHeight * index, 0xFFFFFFFF, index == 0);
		}
	}

	private static String onOff(boolean value) {
		return value ? "on" : "off";
	}

	private static String fastPathHookLabel(EffectiveWaterPolicy policy) {
		if (!policy.flatWaterFastPathActive()) {
			return "inactive";
		}
		return FluidOptimizationPolicy.flatWaterFastPathHookObserved() ? "observed" : "not observed";
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
