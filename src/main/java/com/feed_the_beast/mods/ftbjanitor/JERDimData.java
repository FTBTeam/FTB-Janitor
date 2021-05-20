package com.feed_the_beast.mods.ftbjanitor;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.HashMap;

/**
 * @author LatvianModder
 */
public class JERDimData {
	public final JERScanner scanner;
	public final ServerLevel dimension;
	public HashMap<Block, MutableLong>[] distribution;

	public JERDimData(JERScanner s, ServerLevel w) {
		scanner = s;
		dimension = w;
	}
}
