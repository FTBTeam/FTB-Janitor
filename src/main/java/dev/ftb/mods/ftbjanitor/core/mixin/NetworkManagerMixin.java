package dev.ftb.mods.ftbjanitor.core.mixin;

import dev.ftb.mods.ftbjanitor.FTBJanitor;
import dev.ftb.mods.ftbjanitor.FTBJanitorConfig;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.TimeoutException;
import net.minecraft.network.Connection;
import net.minecraft.network.PacketListener;
import net.minecraft.network.SkipPacketException;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * @author LatvianModder
 */
@Mixin(Connection.class)
public abstract class NetworkManagerMixin {
	@Inject(method = "exceptionCaught", at = @At("HEAD"))
	private void exceptionCaughtFTBJ(ChannelHandlerContext context, Throwable throwable, CallbackInfo ci) {
		if (FTBJanitorConfig.logNetworkErrors && !(throwable instanceof TimeoutException) && !(throwable instanceof SkipPacketException)) {
			if (getPacketListener() instanceof ServerGamePacketListenerImpl) {
				FTBJanitor.LOGGER.info("Internal network in " + context.name() + " / ServerPlayer Handler (" + ((ServerGamePacketListenerImpl) getPacketListener()).player.getScoreboardName() + ")");
			} else {
				FTBJanitor.LOGGER.info("Internal network in " + context.name() + " / " + getPacketListener().getClass().getName());
			}

			throwable.printStackTrace();
		}
	}

	@Shadow
	public abstract PacketListener getPacketListener();
}