package com.feed_the_beast.mods.ftbjanitor.command;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ExperienceOrbEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public enum KillAllType {
	ALL("all", entity -> true),
	DEFAULT("default", entity -> entity instanceof ItemEntity || entity instanceof ExperienceOrbEntity || !(entity instanceof PlayerEntity)),
	LIVING("living", entity -> entity instanceof PlayerEntity),
	NON_LIVING("non_living", entity -> entity instanceof PlayerEntity),
	ITEMS("items", entity -> entity instanceof PlayerEntity),
	XP_ORBS("xp_orbs", entity -> entity instanceof PlayerEntity),

	;

	public static final KillAllType[] VALUES = values();

	public final String name;
	public final Predicate<Entity> filter;

	KillAllType(String n, Predicate<Entity> f) {
		name = n;
		filter = f;
	}
}
