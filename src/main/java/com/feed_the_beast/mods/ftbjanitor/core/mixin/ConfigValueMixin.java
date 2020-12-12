package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.feed_the_beast.mods.ftbjanitor.FTBJanitor;
import com.feed_the_beast.mods.ftbjanitor.FTBJanitorConfig;
import net.minecraftforge.common.ForgeConfigSpec;
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
	@Inject(method = "get", at = @At(value = "RETURN"), remap = false)
	private void getFTBJ(CallbackInfoReturnable<T> ci)
	{
		if (FTBJanitorConfig.logTomlConfigGetters)
		{
			ForgeConfigSpec.ConfigValue<T> configValue = (ForgeConfigSpec.ConfigValue<T>) (Object) this;
			FTBJanitor.LOGGER.info("Config value get(): " + String.join(".", configValue.getPath()) + ": " + ci.getReturnValue() + " (from " + new Exception().getStackTrace()[2] + ")");
		}
	}
}