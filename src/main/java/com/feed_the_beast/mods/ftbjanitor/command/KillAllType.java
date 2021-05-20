package com.feed_the_beast.mods.ftbjanitor.command;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

import java.util.function.Predicate;

/**
 * @author LatvianModder
 */
public enum KillAllType {
	ALL("all", entity -> true),
	DEFAULT("default", entity -> entity instanceof LivingEntity && !(entity instanceof Player)),
	LIVING("living", entity -> entity instanceof LivingEntity),
	NON_LIVING("non_living", entity -> entity instanceof Player),
	ITEMS("items", entity -> entity instanceof ItemEntity),
	XP_ORBS("xp_orbs", entity -> entity instanceof ExperienceOrb),

	;

	public static final KillAllType[] VALUES = values();

	public final String name;
	public final Predicate<Entity> filter;

	KillAllType(String n, Predicate<Entity> f) {
		name = n;
		filter = f;
	}
}
