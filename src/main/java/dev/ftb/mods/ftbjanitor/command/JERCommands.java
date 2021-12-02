package dev.ftb.mods.ftbjanitor.command;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbjanitor.JERDimData;
import dev.ftb.mods.ftbjanitor.JERScanner;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.SerializationTags;
import net.minecraft.tags.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author LatvianModder
 */
public class JERCommands {
	public static void register(LiteralArgumentBuilder<CommandSourceStack> command) {
		command.then(Commands.literal("jer_worldgen")
				.then(Commands.literal("start")
						.executes(context -> jerStart(context.getSource(), false))
						.then(Commands.argument("drops", BoolArgumentType.bool())
								.executes(context -> jerStart(context.getSource(), BoolArgumentType.getBool(context, "drops")))
						)
				)
				.then(Commands.literal("stop")
						.executes(context -> jerStop(context.getSource()))
				)
		);
	}

	private static int jerStart(CommandSourceStack source, boolean drops) {
		if (JERScanner.current != null) {
			source.sendSuccess(new TextComponent("JER Scanner is already running!"), false);
			return 0;
		}

		Path config = FMLPaths.CONFIGDIR.get().resolve("jer-world-gen-config.json");

		if (!Files.exists(config)) {
			try {
				JsonObject json = new JsonObject();

				JsonArray blocks = new JsonArray();
				blocks.add("#forge:ores");
				json.add("block_whitelist", blocks);

				JsonObject dimensions = new JsonObject();

				for (ServerLevel world : source.getServer().getAllLevels()) {
					dimensions.addProperty(world.dimension().location().toString(), true);
				}

				json.add("dimensions", dimensions);

				json.addProperty("height", 128);
				json.addProperty("scan_radius", 25);

				Files.write(config, Collections.singleton(new GsonBuilder().setPrettyPrinting().create().toJson(json)));
				source.sendSuccess(new TextComponent("config/jer-world-gen-config.json created! After you've configured it, run this command again!"), false);
				return 0;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			try (Reader reader = Files.newBufferedReader(config)) {
				JsonObject json = new GsonBuilder().setLenient().create().fromJson(reader, JsonObject.class);

				int height = Mth.clamp(json.get("height").getAsInt(), 16, 256);
				int radius = Mth.clamp(json.get("scan_radius").getAsInt(), 1, 200);
				int startX = Mth.floor(source.getPosition().x) >> 4;
				int startZ = Mth.floor(source.getPosition().z) >> 4;

				Set<Block> blocks = new HashSet<>();

				for (JsonElement e : json.get("block_whitelist").getAsJsonArray()) {
					String s = e.getAsString();

					if (s.startsWith("#")) {
						Tag<Block> tag = SerializationTags.getInstance().getBlocks().getTag(new ResourceLocation(s.substring(1)));

						if (tag != null) {
							blocks.addAll(tag.getValues());
						}
					} else {
						blocks.add(Registry.BLOCK.get(new ResourceLocation(s)));
					}
				}

				blocks.remove(Blocks.AIR);

				JERScanner.current = new JERScanner(height, radius, startX, startZ, blocks, drops);

				JsonObject dimensions = json.get("dimensions").getAsJsonObject();

				for (ServerLevel world : source.getServer().getAllLevels()) {
					String id = world.dimension().location().toString();

					if (dimensions.has(id) && dimensions.get(id).getAsBoolean()) {
						JERScanner.current.dimensions.add(new JERDimData(JERScanner.current, world));
					}
				}

				JERScanner.current.stop = false;

				if (source.getEntity() instanceof Player) {
					Player p = (Player) source.getEntity();
					JERScanner.current.callback = text -> p.displayClientMessage(text, true);
				} else {
					JERScanner.current.callback = text -> source.sendSuccess(text, false);
				}

				Util.ioPool().execute(JERScanner.current);
				source.sendSuccess(new TextComponent("JER Scanner started!"), false);
				return 1;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return 0;
	}

	private static int jerStop(CommandSourceStack source) {
		if (JERScanner.current != null) {
			JERScanner.current.stop();
			JERScanner.current = null;
			source.sendSuccess(new TextComponent("JER Scanner stopped!"), false);
			return 1;
		} else {
			source.sendFailure(new TextComponent("JER Scanner isn't running!"));
			return 0;
		}
	}
}
