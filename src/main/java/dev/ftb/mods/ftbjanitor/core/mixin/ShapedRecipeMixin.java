package dev.ftb.mods.ftbjanitor.core.mixin;

import dev.ftb.mods.ftbjanitor.FTBJanitorConfig;
import net.minecraft.world.item.crafting.ShapedRecipe;
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
		if (FTBJanitorConfig.get().disableRecipeShrinking.get()) {
			ci.setReturnValue(in);
		}
	}
}
