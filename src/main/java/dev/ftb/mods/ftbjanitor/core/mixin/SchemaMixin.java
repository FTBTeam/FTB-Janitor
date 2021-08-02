package dev.ftb.mods.ftbjanitor.core.mixin;

import com.mojang.datafixers.DataFixerBuilder;
import dev.ftb.mods.ftbjanitor.FTBJanitorConfig;
import dev.ftb.mods.ftbjanitor.core.LazyDataFixerBuilder;
import net.minecraft.util.datafix.DataFixers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * @author astei
 */
@Mixin(DataFixers.class)
public class SchemaMixin {
	@Redirect(method = "createFixerUpper", at = @At(value = "NEW", target = "com/mojang/datafixers/DataFixerBuilder"))
	private static DataFixerBuilder create$replaceBuilder(int dataVersion) {
		return FTBJanitorConfig.get().lazyDFU.get() ? new LazyDataFixerBuilder(dataVersion) : new DataFixerBuilder(dataVersion);
	}
}
