package com.feed_the_beast.mods.ftbjanitor.core.mixin;

import net.minecraft.client.renderer.texture.AtlasTexture;
import org.spongepowered.asm.mixin.Mixin;

/**
 * @author LatvianModder
 */
@Mixin(AtlasTexture.class)
public class AtlasTextureMixin {
	/*
	@Inject(method = "loadSprite", at = @At(value = "NEW", target = "net/minecraft/client/renderer/texture/TextureAtlasSprite", shift = At.Shift.BEFORE), locals = LocalCapture.PRINT)
	private void loadSpriteFTBJ(IResourceManager resourceManagerIn, TextureAtlasSprite.Info spriteInfoIn, int widthIn, int heightIn, int mipmapLevelIn, int originX, int originY, CallbackInfoReturnable<TextureAtlasSprite> cir, ResourceLocation resourceLocation, IResource resource, NativeImage image)
	{
		int w = image.getWidth();
		int h = image.getHeight();
		
		if (mipmapLevelIn == 0 && w >= 32 && ((AtlasTexture) (Object) this).getTextureLocation().equals(AtlasTexture.LOCATION_BLOCKS_TEXTURE))
		{
			StartupMessageManager.mcLoaderConsumer().ifPresent(c -> c.accept("Large atlas texture loaded: " + spriteInfoIn.getSpriteLocation() + " (" + w + "x" + h + ")"));
			FTBJanitor.LOGGER.info("Large atlas texture loaded: " + spriteInfoIn.getSpriteLocation() + " (" + w + "x" + h + ")");
		}
	}
	*/
}
