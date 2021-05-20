package com.feed_the_beast.mods.ftbjanitor.core;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.schedule.Activity;

import java.util.Map;
import java.util.Set;

/**
 * @author LatvianModder
 */
public interface BrainFTBJ<E extends LivingEntity> {
	Map<Integer, Map<Activity, Set<Behavior<? super E>>>> getTaskPriorityMapFTBJ();
}
