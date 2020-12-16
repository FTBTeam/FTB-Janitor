package com.feed_the_beast.mods.ftbjanitor;

import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Locale;

/**
 * @author LatvianModder
 */
@Mod(FTBJanitor.MOD_ID)
public class FTBJanitor
{
	public static final String MOD_ID = "ftbjanitor";
	public static final String MOD_NAME = "FTB Janitor";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
	public static FTBJanitorCommon proxy;

	public FTBJanitor()
	{
		Locale.setDefault(Locale.US);
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		FTBJanitorConfig.init();
		proxy = DistExecutor.safeRunForDist(() -> FTBJanitorClient::new, () -> FTBJanitorCommon::new);
	}
}