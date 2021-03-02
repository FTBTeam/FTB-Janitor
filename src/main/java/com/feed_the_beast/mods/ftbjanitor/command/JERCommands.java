package com.feed_the_beast.mods.ftbjanitor.command;

import com.feed_the_beast.mods.ftbjanitor.JERDimData;
import com.feed_the_beast.mods.ftbjanitor.JERScanner;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tags.ITag;
import net.minecraft.tags.TagCollectionManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;
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
	public static void register(LiteralArgumentBuilder<CommandSource> command) {
		command.then(Commands.literal("jer_worldgen")
				.then(Commands.literal("start")
						.executes(context -> jerStart(context.getSource()))
				)
				.then(Commands.literal("stop")
						.executes(context -> jerStop(context.getSource()))
				)
		);
	}

	private static int jerStart(CommandSource source) {
		if (JERScanner.current != null) {
			source.sendFeedback(new StringTextComponent("JER Scanner is already running!"), false);
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

				for (ServerWorld world : source.getServer().getWorlds()) {
					dimensions.addProperty(world.getDimensionKey().getLocation().toString(), true);
				}

				json.add("dimensions", dimensions);

				json.addProperty("height", 128);
				json.addProperty("scan_radius", 25);

				Files.write(config, Collections.singleton(new GsonBuilder().setPrettyPrinting().create().toJson(json)));
				source.sendFeedback(new StringTextComponent("config/jer-world-gen-config.json created! After you've configured it, run this command again!"), false);
				return 0;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		} else {
			try (Reader reader = Files.newBufferedReader(config)) {
				JsonObject json = new GsonBuilder().setLenient().create().fromJson(reader, JsonObject.class);

				int height = MathHelper.clamp(json.get("height").getAsInt(), 16, 256);
				int radius = MathHelper.clamp(json.get("scan_radius").getAsInt(), 1, 200);
				int startX = MathHelper.floor(source.getPos().x) >> 4;
				int startZ = MathHelper.floor(source.getPos().z) >> 4;

				Set<Block> blocks = new HashSet<>();

				for (JsonElement e : json.get("block_whitelist").getAsJsonArray()) {
					String s = e.getAsString();

					if (s.startsWith("#")) {
						ITag<Block> tag = TagCollectionManager.getManager().getBlockTags().get(new ResourceLocation(s.substring(1)));

						if (tag != null) {
							blocks.addAll(tag.getAllElements());
						}
					} else {
						blocks.add(Registry.BLOCK.getOrDefault(new ResourceLocation(s)));
					}
				}

				blocks.remove(Blocks.AIR);

				JERScanner.current = new JERScanner(height, radius, startX, startZ, blocks);

				JsonObject dimensions = json.get("dimensions").getAsJsonObject();

				for (ServerWorld world : source.getServer().getWorlds()) {
					String id = world.getDimensionKey().getLocation().toString();

					if (dimensions.has(id) && dimensions.get(id).getAsBoolean()) {
						JERScanner.current.dimensions.add(new JERDimData(JERScanner.current, world));
					}
				}

				JERScanner.current.stop = false;

				if (source.getEntity() instanceof PlayerEntity) {
					PlayerEntity p = (PlayerEntity) source.getEntity();
					JERScanner.current.callback = text -> p.sendStatusMessage(text, true);
				} else {
					JERScanner.current.callback = text -> source.sendFeedback(text, false);
				}

				Util.getRenderingService().execute(JERScanner.current);
				source.sendFeedback(new StringTextComponent("JER Scanner started!"), false);
				return 1;
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}

		return 0;
	}

	private static int jerStop(CommandSource source) {
		if (JERScanner.current != null) {
			JERScanner.current.stop();
			JERScanner.current = null;
			source.sendFeedback(new StringTextComponent("JER Scanner stopped!"), false);
			return 1;
		} else {
			source.sendErrorMessage(new StringTextComponent("JER Scanner isn't running!"));
			return 0;
		}
	}
}
