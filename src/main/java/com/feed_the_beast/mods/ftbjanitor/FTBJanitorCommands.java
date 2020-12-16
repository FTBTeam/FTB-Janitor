package com.feed_the_beast.mods.ftbjanitor;

import com.feed_the_beast.mods.ftbjanitor.core.BrainFTBJ;
import com.feed_the_beast.mods.ftbjanitor.core.TaskFTBJ;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.util.UUIDTypeAdapter;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.brain.memory.MemoryModuleStatus;
import net.minecraft.entity.ai.brain.memory.MemoryModuleType;
import net.minecraft.entity.ai.brain.schedule.Activity;
import net.minecraft.entity.ai.brain.task.Task;
import net.minecraft.state.Property;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBJanitor.MOD_ID)
public class FTBJanitorCommands
{
	public static final Map<String, MutableLong> LOGGED_TOML_CONFIGS = new HashMap<>();

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event)
	{
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("ftbjanitor").requires(source -> source.getServer().isSinglePlayer() || source.hasPermissionLevel(2));
		LiteralArgumentBuilder<CommandSource> dump = Commands.literal("dump");

		dump.then(Commands.literal("entity_brains").executes(context -> dumpEntityBrains(context.getSource())));
		dump.then(Commands.literal("registry_keys").executes(context -> dumpRegistryKeys(context.getSource())));
		dump.then(Commands.literal("block_states").executes(context -> dumpBlockStates(context.getSource())));

		if (FTBJanitorConfig.logTomlConfigGetters)
		{
			command.then(Commands.literal("print_toml_config_calls").executes(context -> printTomlConfigCalls(context.getSource())));
			command.then(Commands.literal("reset_toml_config_calls").executes(context -> resetTomlConfigCalls(context.getSource())));
		}

		if (event.getEnvironment() != Commands.EnvironmentType.DEDICATED)
		{
			FTBJanitor.proxy.registerCommands(command, dump);
		}

		command.then(dump);
		event.getDispatcher().register(command);
	}

	private static int dumpEntityBrains(CommandSource source)
	{
		source.sendFeedback(new StringTextComponent("Creating entity brain dump (this may take a while...)"), true);

		try
		{
			String[] line = {
					"Dimension",
					"Entity Type",
					"Entity UUID",
					"Priority",
					"Activity",
					"Task Type",
					"Task Status",
					"Memory Module Type",
					"Memory Module Status",
			};

			List<String> lines = new ArrayList<>();
			print(lines, line);
			String filename = "entity-brain-dump-" + Instant.now().toString().replaceAll("[:T]", "-") + ".csv";

			MutableLong dimensionCount = new MutableLong(0L);
			MutableLong entityCount = new MutableLong(0L);
			MutableLong entityWithTasksCount = new MutableLong(0L);

			for (ServerWorld world : source.getServer().getWorlds())
			{
				line[0] = world.getDimensionKey().getLocation().toString();
				dimensionCount.increment();

				world.getEntities()
						.filter(entity -> entity instanceof LivingEntity)
						.map(entity -> (LivingEntity) entity)
						.filter(entity -> entity.getBrain() instanceof BrainFTBJ)
						.forEach(entity -> {
							entityCount.increment();

							Map<Integer, Map<Activity, Set<Task<?>>>> taskPriorityMap = ((BrainFTBJ) entity.getBrain()).getTaskPriorityMapFTBJ();

							if (taskPriorityMap.isEmpty())
							{
								return;
							}

							entityWithTasksCount.increment();
							line[1] = entity.getEntityString();

							if (line[1] == null)
							{
								line[1] = entity.getClass().getName();
							}

							line[2] = UUIDTypeAdapter.fromUUID(entity.getUniqueID());

							for (Map.Entry<Integer, Map<Activity, Set<Task<?>>>> entry : taskPriorityMap.entrySet())
							{
								line[3] = entry.getKey().toString();

								for (Map.Entry<Activity, Set<Task<?>>> entry1 : entry.getValue().entrySet())
								{
									line[4] = entry1.getKey().getKey();

									for (Task<?> task : entry1.getValue())
									{
										line[5] = task.getClass().getSimpleName();
										line[6] = task.getStatus().name().toLowerCase();

										for (Map.Entry<MemoryModuleType<?>, MemoryModuleStatus> entry2 : ((TaskFTBJ) task).getRequiredMemoryStateFTBJ().entrySet())
										{
											line[7] = entry2.getKey().toString();
											line[8] = entry2.getValue().name().toLowerCase();
											print(lines, line);
										}
									}
								}
							}
						});
			}

			Files.write(source.getServer().func_240776_a_(FolderName.DOT).resolve(filename), lines);

			source.sendFeedback(new StringTextComponent("Entity brain dump saved as " + filename + " (" + lines.size() + " lines)"), true);
			source.sendFeedback(new StringTextComponent("Dimension count: " + dimensionCount.getValue()), false);
			source.sendFeedback(new StringTextComponent("Entities with special tasks: " + entityWithTasksCount.getValue() + "/" + entityCount.getValue()), false);
			return 1;
		}
		catch (Exception ex)
		{
			source.sendFeedback(new StringTextComponent("Failed to create entity brain dump! See console for error"), true);
			ex.printStackTrace();
			return 0;
		}
	}

	private static void print(List<String> lines, String[] line)
	{
		lines.add(String.join(",", line));
	}

	private static int dumpRegistryKeys(CommandSource source)
	{
		source.sendFeedback(new StringTextComponent("All registry keys:"), false);
		List<String> lines = new ArrayList<>(RegistryKey.UNIVERSAL_KEY_MAP.keySet());
		lines.sort(null);

		long mem = 0L;

		for (String s : lines)
		{
			mem += 8L * (((s.length() * 2L) + 45L) / 8L);
		}

		mem *= 2L; // its actually more but meh, this works
		double memd = mem / 1024D / 1024D;

		lines.add("");
		lines.add("Estimated memory usage: " + ((long) (memd * 100D) / 100D) + " MB");

		try
		{
			String filename = "registry-key-dump-" + Instant.now().toString().replaceAll("[:T]", "-") + ".txt";
			Files.write(source.getServer().func_240776_a_(FolderName.DOT).resolve(filename), lines);
			source.sendFeedback(new StringTextComponent("Registry key dump saved as " + filename + " (" + lines.size() + " lines, " + ((long) (memd * 100D) / 100D) + " MB estimated memory use)"), true);
			return 1;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return 0;
		}
	}

	private static int dumpBlockStates(CommandSource source)
	{
		List<String> lines = new ArrayList<>();
		long total = 0L;
		Map<String, MutableInt> statesPerMod = new HashMap<>();

		for (Block block : ForgeRegistries.BLOCKS)
		{
			ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);

			if (key == null)
			{
				FTBJanitor.LOGGER.warn("Null key for block " + block.getClass().getName());
				continue;
			}

			int t = block.getStateContainer().getValidStates().size();
			total += t;
			lines.add("- " + key + "[" + t + "] - " + block.getClass().getName());
			statesPerMod.computeIfAbsent(key.getNamespace(), k -> new MutableInt(0)).add(t);

			if (t > 1)
			{
				int i = 1;

				for (BlockState state : block.getStateContainer().getValidStates())
				{
					StringBuilder sb = new StringBuilder();

					if (i < 10 && t >= 10)
					{
						sb.append('0');
					}

					if (i < 100 && t >= 100)
					{
						sb.append('0');
					}

					if (i < 1000 && t >= 1000)
					{
						sb.append('0');
					}

					if (i < 10000 && t >= 10000)
					{
						sb.append('0');
					}

					if (i < 100000 && t >= 100000)
					{
						sb.append('0');
					}

					if (i < 1000000 && t >= 1000000)
					{
						sb.append('0');
					}

					sb.append(i);
					sb.append(' ');
					sb.append(key);
					sb.append('[');

					boolean first = true;

					for (Map.Entry<Property<?>, Comparable<?>> val : state.getValues().entrySet())
					{
						if (first)
						{
							first = false;
						}
						else
						{
							sb.append(',');
						}

						sb.append(val.getKey().getName());
						sb.append('=');
						sb.append(val.getKey().getName(cast(val.getValue())));
					}

					sb.append(']');

					lines.add(sb.toString());
					i++;
				}
			}

			lines.add("");
		}

		lines.add(total + " block states in total");
		lines.add("");
		lines.add("States per mod:");

		double totald = total;
		statesPerMod.entrySet().stream().sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).forEach(entry -> lines.add(entry.getKey() + ": " + entry.getValue() + " [" + (entry.getValue().getValue().doubleValue() * 100D / totald) + "%]"));

		try
		{
			String filename = "block-state-dump-" + Instant.now().toString().replaceAll("[:T]", "-") + ".txt";
			Files.write(source.getServer().func_240776_a_(FolderName.DOT).resolve(filename), lines);
			source.sendFeedback(new StringTextComponent("Block state dump saved as " + filename + " (" + total + " block states in total)"), true);
			return 1;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return 0;
		}
	}

	private static <T> T cast(Object o)
	{
		return (T) o;
	}

	private static int printTomlConfigCalls(CommandSource source)
	{
		source.sendFeedback(new StringTextComponent("Suspicious toml config calls:"), false);
		LOGGED_TOML_CONFIGS.entrySet().stream().filter(e -> e.getValue().getValue() > 1L).sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue())).forEach(entry -> source.sendFeedback(new StringTextComponent(entry.getKey() + " = " + Long.toUnsignedString(entry.getValue().getValue())), false));
		return 1;
	}

	private static int resetTomlConfigCalls(CommandSource source)
	{
		source.sendFeedback(new StringTextComponent("Config logger reset"), false);
		LOGGED_TOML_CONFIGS.clear();
		return 1;
	}
}
