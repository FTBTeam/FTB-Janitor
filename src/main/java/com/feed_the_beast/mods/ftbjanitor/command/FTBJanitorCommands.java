package com.feed_the_beast.mods.ftbjanitor.command;

import com.feed_the_beast.mods.ftbjanitor.FTBJanitor;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ITag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.IServerWorldInfo;
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
	public static final ITag<Block> CLEAR_AREA_TAG = BlockTags.makeWrapperTag("ftbjanitor:clear_area");
	public static boolean autofly = false;

	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSource> command = Commands.literal("ftbjanitor").requires(source -> source.getServer().isSinglePlayer() || source.hasPermissionLevel(2));

		command.then(Commands.literal("autofly").executes(context -> autofly()));

		command.then(Commands.literal("dev")
				.executes(context -> devEnv(context.getSource(), true))
				.then(Commands.argument("enabled", BoolArgumentType.bool())
						.executes(context -> devEnv(context.getSource(), BoolArgumentType.getBool(context, "enabled")))
				)
		);

		command.then(Commands.literal("heal").executes(context -> heal(context.getSource().asPlayer())));
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

		LiteralArgumentBuilder<CommandSource> dump = Commands.literal("dump");

		JERCommands.register(command);
		DumpCommands.register(dump);

		if (event.getEnvironment() != Commands.EnvironmentType.DEDICATED) {
			FTBJanitor.proxy.registerCommands(command, dump);
		}

		command.then(dump);
		event.getDispatcher().register(command);
	}

	private static int autofly() {
		autofly = !autofly;
		return 1;
	}

	private static int devEnv(CommandSource source, boolean envValue) {
		MinecraftServer s = source.getServer();

		s.getGameRules().get(GameRules.DO_DAYLIGHT_CYCLE).set(envValue, s);
		s.getGameRules().get(GameRules.DO_MOB_SPAWNING).set(envValue, s);
		s.getGameRules().get(GameRules.DO_WEATHER_CYCLE).set(envValue, s);

		if (envValue) {
			IServerWorldInfo info = (IServerWorldInfo) source.getWorld().getWorldInfo();
			info.setDayTime(6000);
			info.setClearWeatherTime(2000000);
			info.setRainTime(0);
			info.setThunderTime(0);
			info.setRaining(false);
			info.setThundering(false);
		}

		return 1;
	}

	private static int heal(ServerPlayerEntity player) {
		player.setHealth((player.getMaxHealth()));
		player.getFoodStats().addStats(20, 20);
		return 1;
	}

	private static int clearArea(CommandSource source, int radius, boolean keepStructures, boolean useTag) {
		source.sendFeedback(new StringTextComponent("Clearing area, expect lag..."), false);

		ServerWorld world = source.getWorld();
		BlockPos pos = new BlockPos(source.getPos());
		BlockPos.Mutable mutablePos = new BlockPos.Mutable();

		List<Structure<?>> structures = new ArrayList<>();

		for (Structure<?> s : Registry.STRUCTURE_FEATURE) {
			structures.add(s);
		}

		for (int x = pos.getX() - radius; x <= pos.getX() + radius; x++) {
			for (int z = pos.getZ() - radius; z <= pos.getZ() + radius; z++) {
				int h = world.getHeight(Heightmap.Type.MOTION_BLOCKING, x, z);

				for (int y = 0; y < h; y++) {
					mutablePos.setPos(x, y, z);

					if (!keepStructures || !isPartOfStructure(structures, world, mutablePos)) {
						Block block = world.getBlockState(mutablePos).getBlock();

						if (block != Blocks.BEDROCK && !(block instanceof AirBlock) && (useTag ? CLEAR_AREA_TAG.contains(block) : (block.getRegistryName() == null || block.getRegistryName().getNamespace().equals("minecraft")))) {
							world.setBlockState(mutablePos, Blocks.AIR.getDefaultState(), 2);
						}
					}
				}
			}
		}

		source.sendFeedback(new StringTextComponent("Done!"), false);
		return 1;
	}

	private static boolean isPartOfStructure(List<Structure<?>> structures, World world, BlockPos pos) {
		if (structures.isEmpty()) {
			return false;
		}

		// currently no way to do this
		return false;
	}
}
