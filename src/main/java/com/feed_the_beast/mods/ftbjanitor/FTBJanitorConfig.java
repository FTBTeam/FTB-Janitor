package com.feed_the_beast.mods.ftbjanitor;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.tuple.Pair;

/**
 * @author LatvianModder
 */
public class FTBJanitorConfig
{
	public static boolean logTagCreation;
	public static boolean logTomlConfigGetters;
	public static boolean cacheTomlConfigGetters;
	public static boolean disableBlockStateCache;
	public static boolean logNetworkErrors;

	private static Pair<CommonConfig, ForgeConfigSpec> common;

	public static void init()
	{
		FMLJavaModLoadingContext.get().getModEventBus().register(FTBJanitorConfig.class);

		common = new ForgeConfigSpec.Builder().configure(CommonConfig::new);

		ModLoadingContext modLoadingContext = ModLoadingContext.get();
		modLoadingContext.registerConfig(ModConfig.Type.COMMON, common.getRight());
	}

	@SubscribeEvent
	public static void reload(ModConfig.ModConfigEvent event)
	{
		ModConfig config = event.getConfig();

		if (config.getSpec() == common.getRight())
		{
			CommonConfig c = common.getLeft();
			logTagCreation = c.logTagCreation.get();
			logTomlConfigGetters = c.logTomlConfigGetters.get();
			cacheTomlConfigGetters = c.cacheTomlConfigGetters.get();
			disableBlockStateCache = c.disableBlockStateCache.get();
			logNetworkErrors = c.logNetworkErrors.get();
		}
	}

	private static class CommonConfig
	{
		private final ForgeConfigSpec.BooleanValue logTagCreation;
		private final ForgeConfigSpec.BooleanValue logTomlConfigGetters;
		private final ForgeConfigSpec.BooleanValue cacheTomlConfigGetters;
		private final ForgeConfigSpec.BooleanValue disableBlockStateCache;
		private final ForgeConfigSpec.BooleanValue logNetworkErrors;

		private CommonConfig(ForgeConfigSpec.Builder builder)
		{
			logTagCreation = builder
					.comment("Prints new tag creation in console")
					.define("logTagCreation", false);

			logTomlConfigGetters = builder
					.comment("Prints get() calls from forge toml configs")
					.define("logTomlConfigGetters", false);

			cacheTomlConfigGetters = builder
					.comment("Cache get() calls from forge toml configs. This may improve performance, but may require client restart for some things")
					.define("cacheTomlConfigGetters", false);

			disableBlockStateCache = builder
					.comment("Disables block state caching. This may help with RAM but also massively increase CPU usage")
					.define("disableBlockStateCache", false);

			logNetworkErrors = builder
					.comment("Prints network errors in normal log rather than debug")
					.define("logNetworkErrors", false);
		}
	}
}