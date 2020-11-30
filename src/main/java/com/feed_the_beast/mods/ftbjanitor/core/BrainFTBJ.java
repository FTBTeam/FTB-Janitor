package com.feed_the_beast.mods.ftbjanitor.core;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;

import java.util.Map;
import java.util.Set;

/**
 * @author LatvianModder
 */
public interface BrainFTBJ<E extends LivingEntity>
{
	Map<Integer, Map<Activity, Set<Task<? super E>>>> getTaskPriorityMapFTBJ();
}
