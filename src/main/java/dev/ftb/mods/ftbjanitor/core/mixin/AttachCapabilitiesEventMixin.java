package dev.ftb.mods.ftbjanitor.core.mixin;

import dev.ftb.mods.ftbjanitor.command.DumpCommands;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AttachCapabilitiesEvent.class)
public abstract class AttachCapabilitiesEventMixin<T> {
	@Inject(method = "addCapability", at = @At("HEAD"), remap = false)
	private void addCapabilityFTBJ(ResourceLocation key, ICapabilityProvider cap, CallbackInfo ci) {
		if (DumpCommands.dumpItemCapabilityAttachStacks) {
			DumpCommands.printStack("addCapability(" + key + ", " + cap.getClass().getName() + ") called", "net.minecraftforge.event.AttachCapabilitiesEvent.addCapability", "net.minecraftforge.eventbus.EventBus.post");
		}
	}

	@Inject(method = "getObject", at = @At("HEAD"), remap = false)
	private void getObjectFTBJ(CallbackInfoReturnable<T> ci) {
		if (DumpCommands.dumpItemCapabilityAttachStacks) {
			DumpCommands.printStack("getObject() called", "net.minecraftforge.event.AttachCapabilitiesEvent.getObject", "net.minecraftforge.eventbus.EventBus.post");
		}
	}
}
