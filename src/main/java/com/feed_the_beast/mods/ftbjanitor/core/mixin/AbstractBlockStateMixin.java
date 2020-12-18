package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.feed_the_beast.mods.ftbjanitor.FTBJanitorConfig;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AirBlock;
import net.minecraft.block.FlowingFluidBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author LatvianModder
 */
@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin
{
	@Inject(method = "cacheState", at = @At("HEAD"), cancellable = true)
	public void cacheState(CallbackInfo ci)
	{
		AbstractBlock.AbstractBlockState state = (AbstractBlock.AbstractBlockState) (Object) this;

		if (FTBJanitorConfig.disableBlockStateCache
				&& !(state.getBlock() instanceof AirBlock)
				&& !(state.getBlock() instanceof FlowingFluidBlock)
				&& !state.equals(state.getBlock().getDefaultState())
		)
		{
			ci.cancel();
		}
	}
}
