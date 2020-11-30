package com.feed_the_beast.mods.ftbjanitor.core;

import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;

import java.util.Map;

/**
 * @author LatvianModder
 */
public interface TaskFTBJ
{
	Map<MemoryModuleType<?>, MemoryModuleStatus> getRequiredMemoryStateFTBJ();
}
