package com.feed_the_beast.mods.ftbjanitor.command;

import com.feed_the_beast.mods.ftbjanitor.FTBJanitor;
import com.feed_the_beast.mods.ftbjanitor.core.BehaviorFTBJ;
import com.feed_the_beast.mods.ftbjanitor.core.BrainFTBJ;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.util.UUIDTypeAdapter;
import com.sun.management.HotSpotDiagnosticMXBean;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.behavior.Behavior;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.memory.MemoryStatus;
import net.minecraft.world.entity.schedule.Activity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.loading.moddiscovery.ModInfo;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableLong;

import javax.management.MBeanServer;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
public class DumpCommands {
	public static void register(LiteralArgumentBuilder<CommandSourceStack> dump) {
		dump.then(Commands.literal("heap").executes(context -> heapdump(context.getSource())));
		dump.then(Commands.literal("modlist").executes(context -> dumpModlist(context.getSource())));
		dump.then(Commands.literal("entity_brains").executes(context -> dumpEntityBrains(context.getSource())));
		dump.then(Commands.literal("registry_keys").executes(context -> dumpRegistryKeys(context.getSource())));
		dump.then(Commands.literal("block_states").executes(context -> dumpBlockStates(context.getSource())));
		dump.then(Commands.literal("bad_synced_configs").executes(context -> dumpSyncedConfigs(context.getSource(), false)));
		dump.then(Commands.literal("all_synced_configs").executes(context -> dumpSyncedConfigs(context.getSource(), true)));
	}

	private static <T> T cast(Object o) {
		return (T) o;
	}

	private static int heapdump(CommandSourceStack source) {
		try {
			MBeanServer server = ManagementFactory.getPlatformMBeanServer();
			HotSpotDiagnosticMXBean mxBean = ManagementFactory.newPlatformMXBeanProxy(server, "com.sun.management:type=HotSpotDiagnostic", HotSpotDiagnosticMXBean.class);
			String filename = "ftbjanitor-heapdump-" + System.currentTimeMillis() + ".hprof";
			mxBean.dumpHeap(filename, true);
			source.sendSuccess(new TextComponent("Heap dump saved: " + filename), true);
		} catch (Throwable ex) {
			ex.printStackTrace();
			return 0;
		}

		return 1;
	}

	private static int dumpSyncedConfigs(CommandSourceStack source, boolean all) {
		if (!all) {
			source.sendSuccess(new TextComponent("Bad Config keys (size > 128):"), false);
		}

		ConfigTracker.INSTANCE.syncConfigs(false).forEach(stringS2CConfigDataPair -> {
			if (all || stringS2CConfigDataPair.getKey().length() > 128) {
				source.sendSuccess(new TextComponent(stringS2CConfigDataPair.getKey()).withStyle(stringS2CConfigDataPair.getKey().length() > 128 ? ChatFormatting.RED : ChatFormatting.WHITE), false);
			}
		});

		source.sendSuccess(new TextComponent("Done"), false);
		return 1;
	}

	private static int dumpModlist(CommandSourceStack source) {
		source.sendSuccess(new TextComponent("Mods:\n" + ModList.get().getMods().stream().map(ModInfo::getModId).sorted().collect(Collectors.joining("\n"))), true);
		return 1;
	}

	private static int dumpEntityBrains(CommandSourceStack source) {
		source.sendSuccess(new TextComponent("Creating entity brain dump (this may take a while...)"), true);

		try {
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

			for (ServerLevel world : source.getServer().getAllLevels()) {
				line[0] = world.dimension().location().toString();
				dimensionCount.increment();

				world.getEntities()
						.filter(entity -> entity instanceof LivingEntity)
						.map(entity -> (LivingEntity) entity)
						.filter(entity -> entity.getBrain() instanceof BrainFTBJ)
						.forEach(entity -> {
							entityCount.increment();

							Map<Integer, Map<Activity, Set<Behavior<?>>>> taskPriorityMap = ((BrainFTBJ) entity.getBrain()).getTaskPriorityMapFTBJ();

							if (taskPriorityMap.isEmpty()) {
								return;
							}

							entityWithTasksCount.increment();
							line[1] = entity.getScoreboardName();

							if (line[1] == null) {
								line[1] = entity.getClass().getName();
							}

							line[2] = UUIDTypeAdapter.fromUUID(entity.getUUID());

							for (Map.Entry<Integer, Map<Activity, Set<Behavior<?>>>> entry : taskPriorityMap.entrySet()) {
								line[3] = entry.getKey().toString();

								for (Map.Entry<Activity, Set<Behavior<?>>> entry1 : entry.getValue().entrySet()) {
									line[4] = entry1.getKey().getName();

									for (Behavior<?> task : entry1.getValue()) {
										line[5] = task.getClass().getSimpleName();
										line[6] = task.getStatus().name().toLowerCase();

										for (Map.Entry<MemoryModuleType<?>, MemoryStatus> entry2 : ((BehaviorFTBJ) task).getRequiredMemoryStateFTBJ().entrySet()) {
											line[7] = entry2.getKey().toString();
											line[8] = entry2.getValue().name().toLowerCase();
											print(lines, line);
										}
									}
								}
							}
						});
			}

			Files.write(source.getServer().getWorldPath(LevelResource.ROOT).resolve(filename), lines);

			source.sendSuccess(new TextComponent("Entity brain dump saved as " + filename + " (" + lines.size() + " lines)"), true);
			source.sendSuccess(new TextComponent("Dimension count: " + dimensionCount.getValue()), false);
			source.sendSuccess(new TextComponent("Entities with special tasks: " + entityWithTasksCount.getValue() + "/" + entityCount.getValue()), false);
			return 1;
		} catch (Exception ex) {
			source.sendSuccess(new TextComponent("Failed to create entity brain dump! See console for error"), true);
			ex.printStackTrace();
			return 0;
		}
	}

	private static void print(List<String> lines, String[] line) {
		lines.add(String.join(",", line));
	}

	private static int dumpRegistryKeys(CommandSourceStack source) {
		source.sendSuccess(new TextComponent("All registry keys:"), false);
		List<String> lines = new ArrayList<>(ResourceKey.VALUES.keySet());
		lines.sort(null);

		long mem = 0L;

		for (String s : lines) {
			mem += 8L * (((s.length() * 2L) + 45L) / 8L);
		}

		mem *= 2L; // its actually more but meh, this works
		double memd = mem / 1024D / 1024D;

		lines.add("");
		lines.add("Estimated memory usage: " + ((long) (memd * 100D) / 100D) + " MB");

		try {
			String filename = "registry-key-dump-" + Instant.now().toString().replaceAll("[:T]", "-") + ".txt";
			Files.write(source.getServer().getWorldPath(LevelResource.ROOT).resolve(filename), lines);
			source.sendSuccess(new TextComponent("Registry key dump saved as " + filename + " (" + lines.size() + " lines, " + ((long) (memd * 100D) / 100D) + " MB estimated memory use)"), true);
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}

	private static int dumpBlockStates(CommandSourceStack source) {
		List<String> lines = new ArrayList<>();
		long total = 0L;
		Map<String, MutableInt> statesPerMod = new HashMap<>();

		for (Block block : ForgeRegistries.BLOCKS) {
			ResourceLocation key = ForgeRegistries.BLOCKS.getKey(block);

			if (key == null) {
				FTBJanitor.LOGGER.warn("Null key for block " + block.getClass().getName());
				continue;
			}

			int t = block.getStateDefinition().getPossibleStates().size();
			total += t;
			lines.add("- " + key + "[" + t + "] - " + block.getClass().getName());
			statesPerMod.computeIfAbsent(key.getNamespace(), k -> new MutableInt(0)).add(t);

			if (t > 1) {
				int i = 1;

				for (BlockState state : block.getStateDefinition().getPossibleStates()) {
					StringBuilder sb = new StringBuilder();

					if (i < 10 && t >= 10) {
						sb.append('0');
					}

					if (i < 100 && t >= 100) {
						sb.append('0');
					}

					if (i < 1000 && t >= 1000) {
						sb.append('0');
					}

					if (i < 10000 && t >= 10000) {
						sb.append('0');
					}

					if (i < 100000 && t >= 100000) {
						sb.append('0');
					}

					if (i < 1000000 && t >= 1000000) {
						sb.append('0');
					}

					sb.append(i);
					sb.append(' ');
					sb.append(key);
					sb.append('[');

					boolean first = true;

					for (Map.Entry<Property<?>, Comparable<?>> val : state.getValues().entrySet()) {
						if (first) {
							first = false;
						} else {
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

		try {
			String filename = "block-state-dump-" + Instant.now().toString().replaceAll("[:T]", "-") + ".txt";
			Files.write(source.getServer().getWorldPath(LevelResource.ROOT).resolve(filename), lines);
			source.sendSuccess(new TextComponent("Block state dump saved as " + filename + " (" + total + " block states in total)"), true);
			return 1;
		} catch (Exception ex) {
			ex.printStackTrace();
			return 0;
		}
	}
}
