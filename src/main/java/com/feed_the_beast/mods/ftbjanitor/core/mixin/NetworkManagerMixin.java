package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.feed_the_beast.mods.ftbjanitor.FTBJanitor;
import com.feed_the_beast.mods.ftbjanitor.FTBJanitorConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.TimeoutException;
import net.minecraft.network.INetHandler;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.SkipableEncoderException;
import net.minecraft.network.play.ServerPlayNetHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author LatvianModder
 */
@Mixin(NetworkManager.class)
public abstract class NetworkManagerMixin {
	@Inject(method = "exceptionCaught", at = @At("HEAD"))
	private void exceptionCaughtFTBJ(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
		if (FTBJanitorConfig.logNetworkErrors && !(throwable instanceof TimeoutException) && !(throwable instanceof SkipableEncoderException)) {
			if (getNetHandler() instanceof ServerPlayNetHandler) {
				FTBJanitor.LOGGER.info("Internal network in " + context.name() + " / ServerPlayer Handler (" + ((ServerPlayNetHandler) getNetHandler()).player.getScoreboardName() + ")");
			} else {
				FTBJanitor.LOGGER.info("Internal network in " + context.name() + " / " + getNetHandler().getClass().getName());
			}

			throwable.printStackTrace();
		}
	}

	@Shadow
	public abstract INetHandler getNetHandler();
}