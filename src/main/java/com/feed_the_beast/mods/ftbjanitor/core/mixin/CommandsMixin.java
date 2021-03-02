package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.feed_the_beast.mods.ftbjanitor.FTBJanitorConfig;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Commands.class)
public abstract class CommandsMixin {
	@Inject(method = "handleCommand", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/ParseResults;)I"))
	private void handleCommandFTBJ(CommandSource source, String command, CallbackInfoReturnable<Integer> cir) {
		if (FTBJanitorConfig.printCommandStacktrace) {
			new Exception("Command '" + command + "': (This is not an error!)").printStackTrace();
		}
	}
}
