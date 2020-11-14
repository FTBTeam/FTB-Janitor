package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.chunk.storage.ChunkSerializer;
import net.minecraft.world.gen.feature.structure.Structure;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Map;

/**
 * @author LatvianModder
 */
@Mixin(ChunkSerializer.class)
public class ChunkSerializerMixin
{
	@Redirect(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/chunk/IChunk;setStructureReferences(Ljava/util/Map;)V"))
	private static void setStructureReferencesFTBJ(IChunk chunk, Map<Structure<?>, LongSet> structureReferences)
	{
		if (structureReferences.remove(null) != null)
		{
			chunk.setModified(true);
		}

		chunk.setStructureReferences(structureReferences);
	}
}
