package dev.ftb.mods.ftbjanitor;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import dev.ftb.mods.ftbjanitor.command.FTBJanitorCommands;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.storage.LevelResource;
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
	public void registerCommands(LiteralArgumentBuilder<CommandSourceStack> command, LiteralArgumentBuilder<CommandSourceStack> dump) {
		dump.then(Commands.literal("client_resources").executes(context -> dumpClientResources(context.getSource())));
	}

	private int dumpClientResources(CommandSourceStack source) {
		Minecraft.getInstance().execute(() -> {
			List<Pair<ResourceLocation, Long>> list = new ArrayList<>();
			long totalSize = 0L;
			byte[] buffer = new byte[Short.MAX_VALUE];
			source.sendSuccess(new TextComponent("Loading client resources..."), false);

			ResourceManager manager = Minecraft.getInstance().getResourceManager();
			LinkedHashSet<ResourceLocation> locations = new LinkedHashSet<>();
			FTBJanitor.ignoreResourceLocationErrors = true;

			try {
				locations.addAll(manager.listResources(".", s -> true));
				locations.addAll(manager.listResources("textures", s -> true));
				locations.addAll(manager.listResources("font", s -> true));
				locations.addAll(manager.listResources("lang", s -> true));
				locations.addAll(manager.listResources("blockstates", s -> true));
				locations.addAll(manager.listResources("models", s -> true));
				locations.addAll(manager.listResources("particles", s -> true));
				locations.addAll(manager.listResources("shaders", s -> true));
				locations.addAll(manager.listResources("texts", s -> true));
				locations.addAll(manager.listResources("sounds", s -> true));
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
			lines.add("Resource packs: " + manager.listPacks().map(PackResources::getName).sorted().collect(Collectors.joining(", ")));

			try {
				String filename = "client-resource-dump-" + Instant.now().toString().replaceAll("[:T]", "-") + ".txt";
				Files.write(source.getServer().getWorldPath(LevelResource.ROOT).resolve(filename), lines);
				source.sendSuccess(new TextComponent("Client resource dump saved as " + filename), true);
			} catch (Exception ex) {
				ex.printStackTrace();
			}

			source.sendSuccess(new TextComponent("Resource count: " + list.size() + ", total size: " + totalSize + " bytes"), false);
		});

		return 1;
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event) {
		if (FTBJanitorCommands.autofly && event.phase == TickEvent.Phase.START) {
			Minecraft.getInstance().options.keyUp.setDown(true);
		}
	}
}
