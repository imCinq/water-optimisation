package io.github.imcinq.wateroptimisation;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
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
	// A future schema is read-only for this version so unknown fields cannot be lost.
	private static volatile boolean futureConfigLoaded;

	private ConfigManager() {
	}

	public static void load() {
		futureConfigLoaded = false;
		Path path = getConfigPath();
		try {
			if (!Files.exists(path)) {
				applyConfig(WaterOptimisationConfig.defaults());
				return;
			}

			JsonElement parsed = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8));
			if (!parsed.isJsonObject()) {
				throw new IllegalStateException("Configuration file contained no object");
			}
			JsonObject object = parsed.getAsJsonObject();
			int sourceVersion = readConfigVersion(object);
			if (sourceVersion > WaterOptimisationConfig.CURRENT_CONFIG_VERSION) {
				futureConfigLoaded = true;
				applyConfig(WaterOptimisationConfig.defaults());
				WaterOptimisationClient.LOGGER.warn(
						"Configuration at {} uses newer schema version {}; using safe defaults without rewriting it.",
						path,
						sourceVersion
				);
				return;
			}
			boolean enabledWasPresent = object.has("enabled") && !object.get("enabled").isJsonNull();
			WaterOptimisationConfig loaded = GSON.fromJson(object, WaterOptimisationConfig.class);
			if (loaded == null) {
				throw new IllegalStateException("Configuration file contained no object");
			}
			loaded.migrateFrom(sourceVersion, enabledWasPresent);
			applyConfig(loaded);
			if (shouldRewriteConfig(sourceVersion, object)) {
				try {
					writeConfig(path, loaded);
				} catch (IOException | RuntimeException exception) {
					WaterOptimisationClient.LOGGER.warn("Could not persist the migrated configuration at {}.", path, exception);
				}
			}
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
		Path path = getConfigPath();
		if (futureConfigLoaded
				|| (updated != null && updated.getConfigVersion() > WaterOptimisationConfig.CURRENT_CONFIG_VERSION)) {
			WaterOptimisationClient.LOGGER.warn(
					"Not saving {} because it belongs to a newer configuration schema; upgrade the mod before editing it.",
					path
			);
			return;
		}
		WaterOptimisationConfig safeCopy = updated == null ? WaterOptimisationConfig.defaults() : updated.copy();
		safeCopy.sanitize();

		try {
			writeConfig(path, safeCopy);
			applyConfig(safeCopy);
		} catch (IOException | RuntimeException exception) {
			WaterOptimisationClient.LOGGER.error("Could not save {}.", path, exception);
		}
	}

	private static void writeConfig(Path path, WaterOptimisationConfig value) throws IOException {
		Files.createDirectories(path.getParent());
		if (Files.exists(path)) {
			Files.copy(
					path,
					path.resolveSibling(path.getFileName() + ".bak"),
					StandardCopyOption.REPLACE_EXISTING
			);
		}

		Path temporary = path.resolveSibling(path.getFileName() + ".tmp");
		try {
			Files.writeString(
					temporary,
					GSON.toJson(value),
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
		} finally {
			Files.deleteIfExists(temporary);
		}
	}

	private static int readConfigVersion(JsonObject object) {
		try {
			JsonElement value = object.get("configVersion");
			return value != null && value.isJsonPrimitive() ? value.getAsInt() : 0;
		} catch (RuntimeException exception) {
			return 0;
		}
	}

	static boolean shouldRewriteConfig(int sourceVersion, JsonObject object) {
		if (sourceVersion < WaterOptimisationConfig.CURRENT_CONFIG_VERSION) {
			return true;
		}
		if (sourceVersion > WaterOptimisationConfig.CURRENT_CONFIG_VERSION) {
			return false;
		}
		return !object.has("configVersion")
				|| object.has("farWaterPass")
				|| object.has("debugFallbackLogging");
	}
	private static void applyConfig(WaterOptimisationConfig updated) {
		WaterOptimisationConfig previous = config;
		config = updated;
		// Update the diagnostics gate before refreshing the cached renderer policy
		// so a newly enabled HUD can arm its one-shot hook observation immediately.
		Diagnostics.updateConfig(updated);
		FluidOptimizationPolicy.refresh();
		WaterOptimisationClient.refreshParticleFiltering(updated);
		Diagnostics.reset();
		WaterOptimisationClient.invalidateDiagnosticsHud();

		if (previous == null || !previous.sameFluidRenderingConfiguration(updated)) {
			Minecraft client = Minecraft.getInstance();
			if (client.level != null && client.levelRenderer != null) {
				client.levelRenderer.allChanged();
			}
		}
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
