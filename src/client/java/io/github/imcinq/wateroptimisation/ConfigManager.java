package io.github.imcinq.wateroptimisation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;

public final class ConfigManager {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static volatile WaterOptimisationConfig config = WaterOptimisationConfig.defaults();
	private static volatile Path configPath;

	private ConfigManager() {
	}

	public static void load() {
		Path path = getConfigPath();
		try {
			if (!Files.exists(path)) {
				applyConfig(WaterOptimisationConfig.defaults());
				return;
			}

			WaterOptimisationConfig loaded = GSON.fromJson(Files.readString(path, StandardCharsets.UTF_8), WaterOptimisationConfig.class);
			if (loaded == null) {
				throw new IllegalStateException("Configuration file contained no object");
			}
			loaded.sanitize();
			applyConfig(loaded);
		} catch (IOException | RuntimeException exception) {
			applyConfig(WaterOptimisationConfig.defaults());
			WaterOptimisationClient.LOGGER.warn("Could not load {}, using safe defaults.", path, exception);
		}
	}

	public static WaterOptimisationConfig get() {
		return config;
	}

	public static WaterOptimisationConfig copy() {
		return config.copy();
	}

	public static void save(WaterOptimisationConfig updated) {
		WaterOptimisationConfig safeCopy = updated == null ? WaterOptimisationConfig.defaults() : updated.copy();
		safeCopy.sanitize();
		Path path = getConfigPath();
		Path temporary = path.resolveSibling(path.getFileName() + ".tmp");

		try {
			Files.createDirectories(path.getParent());
			Files.writeString(
					temporary,
					GSON.toJson(safeCopy),
					StandardCharsets.UTF_8,
					StandardOpenOption.CREATE,
					StandardOpenOption.TRUNCATE_EXISTING,
					StandardOpenOption.WRITE
			);
			try {
				Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
			} catch (AtomicMoveNotSupportedException exception) {
				Files.move(temporary, path, StandardCopyOption.REPLACE_EXISTING);
			}
			applyConfig(safeCopy);
		} catch (IOException | RuntimeException exception) {
			WaterOptimisationClient.LOGGER.error("Could not save {}.", path, exception);
		}
	}

	private static void applyConfig(WaterOptimisationConfig updated) {
		WaterOptimisationConfig previous = config;
		config = updated;
		FluidOptimizationPolicy.refresh();
		Diagnostics.updateConfig(updated);
		Diagnostics.reset();

		if (!sameConfiguration(previous, updated)) {
			Minecraft client = Minecraft.getInstance();
			if (client.level != null && client.levelRenderer != null) {
				client.levelRenderer.invalidateCompiledGeometry(
						client.level,
						client.options,
						client.gameRenderer.mainCamera(),
						client.getBlockColors()
				);
			}
		}
	}

	private static boolean sameConfiguration(WaterOptimisationConfig first, WaterOptimisationConfig second) {
		return first != null
				&& first.isEnabled() == second.isEnabled()
				&& first.getPerformanceProfile() == second.getPerformanceProfile()
				&& first.getFluidCullingMode() == second.getFluidCullingMode()
				&& first.isFlatWaterFastPath() == second.isFlatWaterFastPath()
				&& first.isWaterParticles() == second.isWaterParticles()
				&& first.getParticleDistance() == second.getParticleDistance()
				&& first.isParticleFogCulling() == second.isParticleFogCulling()
				&& first.isDiagnosticsHud() == second.isDiagnosticsHud()
				&& first.isDebugFallbackLogging() == second.isDebugFallbackLogging();
	}

	private static Path getConfigPath() {
		Path path = configPath;
		if (path == null) {
			path = Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("wateroptimisation.json");
			configPath = path;
		}
		return path;
	}
}
