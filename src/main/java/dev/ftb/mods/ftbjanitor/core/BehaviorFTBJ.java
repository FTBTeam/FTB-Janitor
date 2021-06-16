package dev.ftb.mods.ftbjanitor.core;

import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;

import java.util.Map;

/**
 * @author LatvianModder
 */
public interface BehaviorFTBJ {
	Map<MemoryModuleType<?>, MemoryStatus> getRequiredMemoryStateFTBJ();
}
