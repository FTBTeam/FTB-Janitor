package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.feed_the_beast.mods.ftbjanitor.FTBJanitor;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author LatvianModder
 */
@Mixin(ResourceLocation.class)
public class ResourceLocationMixin {
	@Inject(method = {"validPathChar", "validNamespaceChar"}, at = @At("HEAD"), cancellable = true)
	private static void validateCharFTBJ(char charValue, CallbackInfoReturnable<Boolean> ci) {
		if (FTBJanitor.ignoreResourceLocationErrors) {
			ci.setReturnValue(true);
		}
	}
}
