package com.feed_the_beast.mods.ftbjanitor;

import com.feed_the_beast.mods.ftbjanitor.command.FTBJanitorCommands;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.storage.FolderName;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
@Mod.EventBusSubscriber(modid = FTBJanitor.MOD_ID, value = Dist.CLIENT)
public class FTBJanitorClient extends FTBJanitorCommon {
	@Override
	public void registerCommands(LiteralArgumentBuilder<CommandSource> command, LiteralArgumentBuilder<CommandSource> dump) {
		dump.then(Commands.literal("client_resources").executes(context -> dumpClientResources(context.getSource())));
	}

	private int dumpClientResources(CommandSource source) {
		Minecraft.getInstance().execute(() -> {
			List<Pair<ResourceLocation, Long>> list = new ArrayList<>();
			long totalSize = 0L;
			byte[] buffer = new byte[Short.MAX_VALUE];
			source.sendFeedback(new StringTextComponent("Loading client resources..."), false);

			IResourceManager manager = Minecraft.getInstance().getResourceManager();
			LinkedHashSet<ResourceLocation> locations = new LinkedHashSet<>();
			FTBJanitor.ignoreResourceLocationErrors = true;

			try {
				locations.addAll(manager.getAllResourceLocations(".", s -> true));
				locations.addAll(manager.getAllResourceLocations("textures", s -> true));
				locations.addAll(manager.getAllResourceLocations("font", s -> true));
				locations.addAll(manager.getAllResourceLocations("lang", s -> true));
				locations.addAll(manager.getAllResourceLocations("blockstates", s -> true));
				locations.addAll(manager.getAllResourceLocations("models", s -> true));
				locations.addAll(manager.getAllResourceLocations("particles", s -> true));
				locations.addAll(manager.getAllResourceLocations("shaders", s -> true));
				locations.addAll(manager.getAllResourceLocations("texts", s -> true));
				locations.addAll(manager.getAllResourceLocations("sounds", s -> true));
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			FTBJanitor.ignoreResourceLocationErrors = false;

			for (ResourceLocation res : locations) {
				long size = 0L;

				try (InputStream stream = manager.getResource(res).getInputStream()) {
					int i;

					do {
						i = stream.read(buffer);
						size += i;
					}
					while (i > 0);
				} catch (Exception ex) {
					size = 0L;
				}

				list.add(Pair.of(res, size));
				totalSize += size;
				FTBJanitor.LOGGER.info("Found " + res + ": " + size);
			}

			List<String> lines = new ArrayList<>();
			list.stream().sorted((o1, o2) -> Long.compare(o2.getValue(), o1.getValue())).forEach(p -> lines.add(p.getKey() + ": " + p.getValue()));
			lines.add("");
			lines.add("Resource count: " + list.size() + ", total size: " + totalSize + " bytes");
			lines.add("Resource packs: " + manager.getResourcePackStream().map(IResourcePack::getName).sorted().collect(Collectors.joining(", ")));

			try {
				String filename = "client-resource-dump-" + Instant.now().toString().replaceAll("[:T]", "-") + ".txt";
				Files.write(source.getServer().func_240776_a_(FolderName.DOT).resolve(filename), lines);
				source.sendFeedback(new StringTextComponent("Client resource dump saved as " + filename), true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			source.sendFeedback(new StringTextComponent("Resource count: " + list.size() + ", total size: " + totalSize + " bytes"), false);
		});

		return 1;
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event) {
		if (FTBJanitorCommands.autofly && event.phase == TickEvent.Phase.START) {
			Minecraft.getInstance().gameSettings.keyBindForward.setPressed(true);
		}
	}
}
