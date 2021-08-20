package dev.ftb.mods.ftbjanitor.core.mixin;

import dev.ftb.mods.ftbjanitor.FTBJanitor;
import dev.ftb.mods.ftbjanitor.FTBJanitorConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.chunk.storage.ChunkSerializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChunkSerializer.class)
public abstract class ChunkSerializerMixin {
	@Redirect(method = "read", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;getIntArray(Ljava/lang/String;)[I"))
	private static int[] readBiomesFTBJ(CompoundTag tag, String key) {
		int current = FTBJanitorConfig.get().refreshBiomes.get();

		if (current > 0 && current > tag.getInt("BiomesVersionFTBJ")) {
			tag.putBoolean("shouldSave", true);
			FTBJanitor.LOGGER.info("Resetting chunk biomes @ " + tag.getInt("xPos") + ":" + tag.getInt("zPos") + "!");
			return null;
		}

		return tag.getIntArray(key);
	}

	@Redirect(method = "write", at = @At(value = "INVOKE", target = "Lnet/minecraft/nbt/CompoundTag;putIntArray(Ljava/lang/String;[I)V"))
	private static void writeBiomesFTBJ(CompoundTag tag, String key, int[] array) {
		tag.putInt("BiomesVersionFTBJ", FTBJanitorConfig.get().refreshBiomes.get());
		tag.putIntArray(key, array);
	}
}
