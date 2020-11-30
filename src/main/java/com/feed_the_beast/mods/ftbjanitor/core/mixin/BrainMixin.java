package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.feed_the_beast.mods.ftbjanitor.core.BrainFTBJ;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.Brain;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import java.util.Set;

/**
 * @author LatvianModder
 */
@Mixin(Brain.class)
public abstract class BrainMixin<E extends LivingEntity> implements BrainFTBJ<E>
{
	@Override
	@Accessor("taskPriorityMap")
	public abstract Map<Integer, Map<Activity, Set<Task<? super E>>>> getTaskPriorityMapFTBJ();
}
