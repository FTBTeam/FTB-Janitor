package dev.ftb.mods.ftbjanitor;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author LatvianModder
 */
public class FTBJanitorConfig {
	public static boolean logTagCreation;
	public static boolean logNetworkErrors;
	public static boolean disableRecipeShrinking;
	public static boolean printReloadStacktrace;
	public static boolean printCommandStacktrace;

	private static Pair<CommonConfig, ForgeConfigSpec> common;

	public static void init() {
		FMLJavaModLoadingContext.get().getModEventBus().register(FTBJanitorConfig.class);

		common = new ForgeConfigSpec.Builder().configure(CommonConfig::new);

		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		modLoadingContext.registerConfig(ModConfig.Type.COMMON, common.getRight());
	}

	@SubscribeEvent
	public static void reload(ModConfig.ModConfigEvent event) {
		ModConfig config = event.getConfig();

		if (config.getSpec() == common.getRight()) {
			CommonConfig c = common.getLeft();
			logTagCreation = c.logTagCreation.get();
			logNetworkErrors = c.logNetworkErrors.get();
			disableRecipeShrinking = c.disableRecipeShrinking.get();
			printReloadStacktrace = c.printReloadStacktrace.get();
			printCommandStacktrace = c.printCommandStacktrace.get();
		}
	}

	private static class CommonConfig {
		private final ForgeConfigSpec.BooleanValue logTagCreation;
		private final ForgeConfigSpec.BooleanValue logNetworkErrors;
		private final ForgeConfigSpec.BooleanValue disableRecipeShrinking;
		private final ForgeConfigSpec.BooleanValue printReloadStacktrace;
		private final ForgeConfigSpec.BooleanValue printCommandStacktrace;

		private CommonConfig(ForgeConfigSpec.Builder builder) {
			logTagCreation = builder
					.comment("Prints new tag creation in console")
					.define("logTagCreation", false);

			logNetworkErrors = builder
					.comment("Prints network errors in normal log rather than debug")
					.define("logNetworkErrors", false);

			disableRecipeShrinking = builder
					.comment("Vanilla shrinks recipe patterns to try to optimise them, but it's sometimes breaking some modded recipes")
					.define("disableRecipeShrinking", false);

			printReloadStacktrace = builder
					.comment("Print whenever a reload is triggered")
					.define("printReloadStacktrace", false);

			printCommandStacktrace = builder
					.comment("Print whenever a command is run")
					.define("printCommandStacktrace", false);
		}
	}
}