package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.registries.IForgeRegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
@Mixin(RegistryEvent.MissingMappings.class)
public class MissingMappingsMixin<T extends IForgeRegistryEntry<T>>
{
	@Shadow(remap = false)
	@Final
	private ImmutableList<RegistryEvent.MissingMappings.Mapping<T>> mappings;

	@Shadow(remap = false)
	private ModContainer activeMod;

	@Inject(method = "getMappings", at = @At("HEAD"), remap = false, cancellable = true)
	public void getMappingsFTBJ(CallbackInfoReturnable<ImmutableList<RegistryEvent.MissingMappings.Mapping<T>>> ci)
	{
		if (mappings.isEmpty() || activeMod == null)
		{
			ci.setReturnValue(ImmutableList.of());
		}
		else
		{
			ci.setReturnValue(ImmutableList.copyOf(mappings.stream().filter(e -> e.key != null && e.key.getNamespace().equals(activeMod.getModId())).collect(Collectors.toList())));
		}
	}
}
