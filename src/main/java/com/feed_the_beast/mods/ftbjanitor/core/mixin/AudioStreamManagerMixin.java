package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import net.minecraft.client.audio.AudioStreamManager;
import net.minecraft.client.audio.Sound;
import net.minecraftforge.fml.loading.progress.StartupMessageManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

/**
 * @author LatvianModder
 */
@Mixin(AudioStreamManager.class)
public class AudioStreamManagerMixin {
	@Inject(method = "preload", at = @At("RETURN"))
	private void preloadFTBJ(Collection<Sound> sounds, CallbackInfoReturnable<CompletableFuture<?>> cir) {
		StartupMessageManager.mcLoaderConsumer().ifPresent(c -> c.accept("Sound Engine Loaded"));
	}
}
