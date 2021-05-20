package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.feed_the_beast.mods.ftbjanitor.core.BrainFTBJ;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.Brain;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.schedule.Activity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

/**
 * @author LatvianModder
 */
@Mixin(Brain.class)
public abstract class BrainMixin<E extends LivingEntity> implements BrainFTBJ<E> {
	@Override
	@Accessor("availableBehaviorsByPriority")
	public abstract Map<Integer, Map<Activity, Set<Behavior<? super E>>>> getTaskPriorityMapFTBJ();
}
