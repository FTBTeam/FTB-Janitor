package com.feed_the_beast.mods.ftbjanitor;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.resources.IResourceManager;
import net.minecraft.resources.IResourcePack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.storage.FolderName;
import org.apache.commons.lang3.tuple.Pair;

import java.io.InputStream;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author LatvianModder
 */
public class FTBJanitorClient extends FTBJanitorCommon
{
	@Override
	public void registerCommands(LiteralArgumentBuilder<CommandSource> command)
	{
		command.then(Commands.literal("dump_all_client_resources")
				.executes(context -> dumpAllResources(context.getSource()))
		);
	}

	private int dumpAllResources(CommandSource source)
	{
		Minecraft.getInstance().execute(() -> {
			List<Pair<ResourceLocation, Long>> list = new ArrayList<>();
			long totalSize = 0L;
			byte[] buffer = new byte[Short.MAX_VALUE];
			source.sendFeedback(new StringTextComponent("Loading client resources..."), false);

			IResourceManager manager = Minecraft.getInstance().getResourceManager();

			for (ResourceLocation res : manager.getAllResourceLocations(".", s -> true))
			{
				long size = 0L;

				try (InputStream stream = manager.getResource(res).getInputStream())
				{
					int i;

					do
					{
						i = stream.read(buffer);
						size += i;
					}
					while (i > 0);
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}

				list.add(Pair.of(res, size));
				totalSize += size;
				FTBJanitor.LOGGER.info("Found " + res + ": " + size);
			}

			List<String> lines = new ArrayList<>();
			list.stream().sorted((o1, o2) -> Long.compare(o2.getValue(), o1.getValue())).forEach(p -> lines.add(p.getKey() + ": " + p.getValue()));
			lines.add("");
			lines.add("Resource count: " + list.size() + ", total size: " + totalSize + " bytes");
			lines.add("Resource packs: " + manager.getResourcePackStream().map(IResourcePack::getName).collect(Collectors.joining(", ")));

			try
			{
				String filename = "client-resource-dump-" + Instant.now().toString().replaceAll("[:T]", "-") + ".txt";
				Files.write(source.getServer().func_240776_a_(FolderName.DOT).resolve(filename), lines);
				source.sendFeedback(new StringTextComponent("Client resource dump saved as " + filename), true);
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}

			source.sendFeedback(new StringTextComponent("Resource count: " + list.size() + ", total size: " + totalSize + " bytes"), false);
		});

		return 1;
	}
}
