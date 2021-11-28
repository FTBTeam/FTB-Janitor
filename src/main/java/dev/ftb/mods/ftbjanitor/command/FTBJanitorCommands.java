package dev.ftb.mods.ftbjanitor.command;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbjanitor.FTBJanitor;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.Tag;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBJanitor.MOD_ID)
public class FTBJanitorCommands {
	public static final Tag<Block> CLEAR_AREA_TAG = BlockTags.bind("ftbjanitor:clear_area");
	public static boolean autofly = false;

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("ftbjanitor").requires(source -> source.getServer().isSingleplayer() || source.hasPermission(2));

		command.then(Commands.literal("autofly").executes(context -> autofly()));

		command.then(Commands.literal("dev")
				.executes(context -> devEnv(context.getSource(), true))
				.then(Commands.argument("enabled", BoolArgumentType.bool())
						.executes(context -> devEnv(context.getSource(), BoolArgumentType.getBool(context, "enabled")))
				)
		);

		command.then(Commands.literal("heal").executes(context -> heal(context.getSource().getPlayerOrException())));
		command.then(Commands.literal("clear_area_from_tag")
				.executes(context -> clearArea(context.getSource(), 30, false, true))
				.then(Commands.argument("radius", IntegerArgumentType.integer(3, 200))
						.executes(context -> clearArea(context.getSource(), IntegerArgumentType.getInteger(context, "radius"), false, true))
						.then(Commands.argument("keep_structures", BoolArgumentType.bool())
								.executes(context -> clearArea(context.getSource(), IntegerArgumentType.getInteger(context, "radius"), BoolArgumentType.getBool(context, "keep_structures"), true))
						)
				)
		);

		command.then(Commands.literal("clear_area_from_vanilla_blocks")
				.executes(context -> clearArea(context.getSource(), 30, false, false))
				.then(Commands.argument("radius", IntegerArgumentType.integer(3, 200))
						.executes(context -> clearArea(context.getSource(), IntegerArgumentType.getInteger(context, "radius"), false, false))
						.then(Commands.argument("keep_structures", BoolArgumentType.bool())
								.executes(context -> clearArea(context.getSource(), IntegerArgumentType.getInteger(context, "radius"), BoolArgumentType.getBool(context, "keep_structures"), false))
						)
				)
		);

		command.then(Commands.literal("count_ores")
				.then(Commands.argument("chunk_radius", IntegerArgumentType.integer(1, 25))
						.executes(context -> countOres(context.getSource(), IntegerArgumentType.getInteger(context, "chunk_radius"), 0, 255))
						.then(Commands.argument("min_y", IntegerArgumentType.integer(0, 255))
								.then(Commands.argument("max_y", IntegerArgumentType.integer(0, 255))
										.executes(context -> countOres(context.getSource(), IntegerArgumentType.getInteger(context, "chunk_radius"), IntegerArgumentType.getInteger(context, "min_y"), IntegerArgumentType.getInteger(context, "max_y")))
								)
						)
				)
		);

		LiteralArgumentBuilder<CommandSourceStack> dump = Commands.literal("dump");

		JERCommands.register(command);
		DumpCommands.register(dump);

		if (event.getEnvironment() != Commands.CommandSelection.DEDICATED) {
			FTBJanitor.proxy.registerCommands(command, dump);
		}

		command.then(dump);
		event.getDispatcher().register(command);
	}

	private static int autofly() {
		autofly = !autofly;
		return 1;
	}

	private static int devEnv(CommandSourceStack source, boolean envValue) {
		MinecraftServer s = source.getServer();

		s.getGameRules().getRule(GameRules.RULE_DAYLIGHT).set(envValue, s);
		s.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(envValue, s);
		s.getGameRules().getRule(GameRules.RULE_WEATHER_CYCLE).set(envValue, s);

		if (envValue) {
			ServerLevelData info = (ServerLevelData) source.getLevel().getLevelData();
			info.setDayTime(6000);
			info.setClearWeatherTime(2000000);
			info.setRainTime(0);
			info.setThunderTime(0);
			info.setRaining(false);
			info.setThundering(false);
		}

		return 1;
	}

	private static int heal(ServerPlayer player) {
		player.setHealth((player.getMaxHealth()));
		player.getFoodData().eat(20, 20F);
		return 1;
	}

	private static int clearArea(CommandSourceStack source, int radius, boolean keepStructures, boolean useTag) {
		source.sendSuccess(new TextComponent("Clearing area, expect lag..."), false);

		ServerLevel world = source.getLevel();
		BlockPos pos = new BlockPos(source.getPosition());
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

		List<StructureFeature<?>> structures = new ArrayList<>();

		for (StructureFeature<?> s : Registry.STRUCTURE_FEATURE) {
			structures.add(s);
		}

		for (int x = pos.getX() - radius; x <= pos.getX() + radius; x++) {
			for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; z++) {
				int h = world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);

				for (int y = h - 2; y >= 0; y--) {
					mutablePos.set(x, y, z);

					if (!keepStructures || !isPartOfStructure(structures, world, mutablePos)) {
						Block block = world.getBlockState(mutablePos).getBlock();

						if (block != Blocks.BEDROCK && !(block instanceof AirBlock) && (useTag ? CLEAR_AREA_TAG.contains(block) : (block.getRegistryName() == null || block.getRegistryName().getNamespace().equals("minecraft")))) {
							world.setBlock(mutablePos, Blocks.AIR.defaultBlockState(), 2);
						}
					}
				}
			}
		}

		source.sendSuccess(new TextComponent("Done!"), false);
		return 1;
	}

	private static boolean isPartOfStructure(List<StructureFeature<?>> structures, ServerLevel world, BlockPos pos) {
		if (structures.isEmpty()) {
			return false;
		}

		for (StructureFeature<?> feature : structures) {
			if (world.structureFeatureManager().getStructureAt(pos, true, feature).isValid()) {
				return true;
			}
		}

		return false;
	}

	private static int countOres(CommandSourceStack source, int chunkRadius, int minY, int maxY) {
		int radius = chunkRadius - 1;
		source.sendSuccess(new TextComponent("Loading " + ((radius * 2 + 1) * (radius * 2 + 1)) + " chunks, expect lag..."), false);

		Thread thread = new Thread(() -> {
			BlockPos sourcePos = new BlockPos(source.getPosition());
			int cx = sourcePos.getX() >> 4;
			int cz = sourcePos.getZ() >> 4;
			List<ChunkAccess> chunks = new ArrayList<>();

			for (int x = cx - radius; x <= cx + radius; x++) {
				for (int z = cz - radius; z <= cz + radius; z++) {
					if (!source.getServer().isRunning()) {
						return;
					}

					final ChunkAccess chunk = source.getLevel().getChunk(x, z, ChunkStatus.FULL, true);

					if (chunk != null) {
						chunks.add(chunk);
					}
				}
			}

			source.sendSuccess(new TextComponent("Found " + chunks.size() + " chunks, scanning blocks..."), false);

			BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
			int count = 0;
			int blocks = 0;
			Map<Block, MutableInt> ores = new HashMap<>();

			for (ChunkAccess chunk : chunks) {
				for (int bx = 0; bx < 16; bx++) {
					for (int bz = 0; bz < 16; bz++) {
						int h = Math.min(maxY, chunk.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, bx, bz));

						for (int y = minY; y < h; y++) {
							pos.set(bz, y, bz);
							BlockState block = chunk.getBlockState(pos);

							if (!block.isAir() && Tags.Blocks.ORES.contains(block.getBlock())) {
								count++;
								ores.computeIfAbsent(block.getBlock(), (block1) -> new MutableInt()).increment();
							}

							blocks++;

							if (!source.getServer().isRunning()) {
								return;
							}
						}
					}
				}
			}

			source.sendSuccess(new TextComponent("Found " + count + " ores, " + blocks + " blocks in total. See latest.log for full printout."), false);

			FTBJanitor.LOGGER.info("Ore counts:\nTotal chunks: " + chunks.size() + "\nTotal blocks: " + blocks + "\nTotal ores: " + count + "\n\n" + ores.entrySet().stream().sorted((a, b) -> -a.getValue().compareTo(b.getValue())).map(e -> e.getKey().getRegistryName() + "," + e.getValue()).collect(Collectors.joining("\n")));
		}, "Ore-Count");

		thread.setDaemon(true);
		thread.start();
		return 1;
	}
}