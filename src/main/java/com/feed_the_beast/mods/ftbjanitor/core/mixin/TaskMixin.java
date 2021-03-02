package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import com.feed_the_beast.mods.ftbjanitor.core.TaskFTBJ;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.task.Task;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

/**
 * @author LatvianModder
 */
@Mixin(Task.class)
public abstract class TaskMixin implements TaskFTBJ {
	@Override
	@Accessor("requiredMemoryState")
	public abstract Map<MemoryModuleType<?>, MemoryModuleStatus> getRequiredMemoryStateFTBJ();
}
