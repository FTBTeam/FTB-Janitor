package dev.ftb.mods.ftbjanitor.core.mixin;

import dev.ftb.mods.ftbjanitor.FTBJanitorConfig;
import dev.ftb.mods.ftbjanitor.command.DumpCommands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Commands.class)
public abstract class CommandsMixin {
	@Inject(method = "performCommand", at = @At(value = "INVOKE", target = "Lcom/mojang/brigadier/CommandDispatcher;execute(Lcom/mojang/brigadier/ParseResults;)I", remap = false))
	private void handleCommandFTBJ(CommandSourceStack source, String command, CallbackInfoReturnable<Integer> cir) {
		if (FTBJanitorConfig.get().printCommandStacktrace.get()) {
			DumpCommands.printStack("Command '" + command + "' executed", "", "");
		}
	}
}
