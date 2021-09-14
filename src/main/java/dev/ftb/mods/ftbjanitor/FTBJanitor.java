package dev.ftb.mods.ftbjanitor;

import dev.ftb.mods.ftbjanitor.command.DumpCommands;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * @author LatvianModder
 */
@Mod(FTBJanitor.MOD_ID)
@Mod.EventBusSubscriber(modid = FTBJanitor.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FTBJanitor {
	public static final String MOD_ID = "ftbjanitor";
	public static final String MOD_NAME = "FTB Janitor";
	public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);
	public static FTBJanitorCommon proxy;

	public FTBJanitor() {
		Locale.setDefault(Locale.US);
		ModLoadingContext.get().registerExtensionPoint(ExtensionPoint.DISPLAYTEST, () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
		FTBJanitorConfig.get();
		proxy = DistExecutor.safeRunForDist(() -> FTBJanitorClient::new, () -> FTBJanitorCommon::new);
	}

	@SubscribeEvent
	public static void attach(AttachCapabilitiesEvent<ItemStack> event) {
		if (DumpCommands.dumpItemCapabilityAttachStacks && event.getObject().getItem() == Items.APPLE) {
			event.addCapability(new ResourceLocation("ftbjanitor:example"), new ICapabilityProvider() {
				@NotNull
				@Override
				public <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction arg) {
					return LazyOptional.empty();
				}
			});
		}
	}

	@SubscribeEvent
	public static void soundPlayed(PlaySoundAtEntityEvent event) {
		if (event.getSound() == SoundEvents.ARMOR_EQUIP_GENERIC) {
			new RuntimeException("Dummy exception from entity " + event.getEntity()).printStackTrace();
		}
	}
}