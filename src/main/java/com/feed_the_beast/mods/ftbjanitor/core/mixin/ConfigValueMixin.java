package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.feed_the_beast.mods.ftbjanitor.FTBJanitor;
import com.feed_the_beast.mods.ftbjanitor.FTBJanitorCommands;
import com.feed_the_beast.mods.ftbjanitor.FTBJanitorConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.mutable.MutableLong;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author LatvianModder
 */
@Mixin(ForgeConfigSpec.ConfigValue.class)
public abstract class ConfigValueMixin<T>
{
	private T cachedGetFTBJ;

	@Inject(method = "get", at = @At(value = "HEAD"), remap = false, cancellable = true)
	private void getFTBJHead(CallbackInfoReturnable<T> ci)
	{
		if (FTBJanitorConfig.cacheTomlConfigGetters && cachedGetFTBJ != null)
		{
			ci.setReturnValue(cachedGetFTBJ);
		}
	}

	@Inject(method = "get", at = @At(value = "RETURN"), remap = false)
	private void getFTBJReturn(CallbackInfoReturnable<T> ci)
	{
		if (FTBJanitorConfig.cacheTomlConfigGetters && cachedGetFTBJ == null)
		{
			ForgeConfigSpec.ConfigValue<T> configValue = (ForgeConfigSpec.ConfigValue<T>) (Object) this;
			FTBJanitor.LOGGER.info("Cached config: " + String.join(".", configValue.getPath()) + ": " + ci.getReturnValue() + " (from " + new Exception().getStackTrace()[2] + ")");
			cachedGetFTBJ = ci.getReturnValue();
		}

		if (FTBJanitorConfig.logTomlConfigGetters)
		{
			ForgeConfigSpec.ConfigValue<T> configValue = (ForgeConfigSpec.ConfigValue<T>) (Object) this;
			FTBJanitorCommands.LOGGED_TOML_CONFIGS.computeIfAbsent(String.join(".", configValue.getPath()) + ": " + ci.getReturnValue() + " (from " + new Exception().getStackTrace()[2] + ")", k -> new MutableLong(0L)).increment();
		}
	}
}