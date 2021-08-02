package dev.ftb.mods.ftbjanitor.core.mixin;

import dev.ftb.mods.ftbjanitor.FTBJanitorConfig;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Inject(method = "reloadResources", at = @At("HEAD"))
	private void reloadFTBJ(Collection<String> packs, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		if (FTBJanitorConfig.get().printReloadStacktrace.get()) {
			new Exception("Reload '" + packs + "': (This is not an error!)").printStackTrace();
		}
	}
}
