package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.feed_the_beast.mods.ftbjanitor.core.BehaviorFTBJ;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * @author LatvianModder
 */
@Mixin(Behavior.class)
public abstract class BehaviorMixin implements BehaviorFTBJ {
	@Override
	@Accessor("entryCondition")
	public abstract Map<MemoryModuleType<?>, MemoryStatus> getRequiredMemoryStateFTBJ();
}
