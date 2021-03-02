package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.feed_the_beast.mods.ftbjanitor.FTBJanitorConfig;
import net.minecraft.item.crafting.ShapedRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * @author LatvianModder
 */
@Mixin(ShapedRecipe.class)
public class ShapedRecipeMixin {
	@Inject(method = "shrink", at = @At("HEAD"), cancellable = true)
	private static void shrinkFTBJ(String[] in, CallbackInfoReturnable<String[]> ci) {
		if (FTBJanitorConfig.disableRecipeShrinking) {
			ci.setReturnValue(in);
		}
	}
}
