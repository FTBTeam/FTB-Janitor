package com.feed_the_beast.mods.ftbjanitor;

import net.minecraft.block.Block;
import net.minecraft.world.server.ServerWorld;
import org.apache.commons.lang3.mutable.MutableLong;

import java.util.HashMap;

/**
 * @author LatvianModder
 */
public class JERDimData
{
	public final JERScanner scanner;
	public final ServerWorld dimension;
	public HashMap<Block, MutableLong>[] distribution;

	public JERDimData(JERScanner s, ServerWorld w)
	{
		scanner = s;
		dimension = w;
	}
}
