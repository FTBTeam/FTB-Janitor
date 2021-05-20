package com.feed_the_beast.mods.ftbjanitor.command;

import com.feed_the_beast.mods.ftbjanitor.FTBJanitor;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AirBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

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

				for (int y = 0; y < h; y++) {
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

	private static boolean isPartOfStructure(List<StructureFeature<?>> structures, Level world, BlockPos pos) {
		if (structures.isEmpty()) {
			return false;
		}

		// currently no way to do this
		return false;
	}
}
