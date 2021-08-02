package dev.ftb.mods.ftbjanitor;

import dev.ftb.mods.ftblibrary.snbt.config.BooleanValue;
import dev.ftb.mods.ftblibrary.snbt.config.SNBTConfig;
import net.minecraftforge.fml.loading.FMLPaths;

import java.nio.file.Path;

/**
 * @author LatvianModder
 */
public class FTBJanitorConfig {
	private static FTBJanitorConfig instance;

	public static FTBJanitorConfig get() {
		if (instance == null) {
			instance = new FTBJanitorConfig();
		}

		return instance;
	}

	public final SNBTConfig config;
	public final BooleanValue logTagCreation;
	public final BooleanValue logNetworkErrors;
	public final BooleanValue disableRecipeShrinking;
	public final BooleanValue printReloadStacktrace;
	public final BooleanValue printCommandStacktrace;
	public final BooleanValue lazyDFU;

	private FTBJanitorConfig() {
		Path path = FMLPaths.CONFIGDIR.get().resolve("ftbjanitor.snbt");
		config = SNBTConfig.create(FTBJanitor.MOD_ID);
		logTagCreation = config.getBoolean("logTagCreation", false).comment("Prints new tag creation in console");
		logNetworkErrors = config.getBoolean("logNetworkErrors", false).comment("Prints network errors in normal log rather than debug");
		disableRecipeShrinking = config.getBoolean("disableRecipeShrinking", false).comment("Vanilla shrinks recipe patterns to try to optimise them, but it's sometimes breaking some modded recipes");
		printReloadStacktrace = config.getBoolean("printReloadStacktrace", false).comment("Print whenever a reload is triggered");
		printCommandStacktrace = config.getBoolean("printCommandStacktrace", false).comment("Print whenever a command is run");
		lazyDFU = config.getBoolean("lazyDFU", true).comment("Enables lazy DataFixerUpper");
		config.load(path);
	}
}