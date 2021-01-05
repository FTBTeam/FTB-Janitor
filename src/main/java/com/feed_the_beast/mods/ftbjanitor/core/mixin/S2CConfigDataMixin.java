package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.feed_the_beast.mods.ftbjanitor.FTBJanitorConfig;
import net.minecraftforge.fml.network.FMLHandshakeMessages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

/**
 * @author LatvianModder
 */
@Mixin(FMLHandshakeMessages.S2CConfigData.class)
public abstract class S2CConfigDataMixin
{
	@ModifyConstant(method = "decode", remap = false, constant = @Constant(intValue = 128))
	private static int maxStringSizeFTBJ(int i)
	{
		return FTBJanitorConfig.increase2CConfigMaxKeySize ? Short.MAX_VALUE : i;
	}
}