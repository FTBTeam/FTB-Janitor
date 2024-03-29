package dev.ftb.mods.ftbjanitor.core;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import dev.ftb.mods.ftbjanitor.FTBJanitor;

import java.util.concurrent.Executor;

/**
 * @author astei
 */
public class LazyDataFixerBuilder extends DataFixerBuilder {
	private static final Executor NO_OP_EXECUTOR = command -> {
	};

	public LazyDataFixerBuilder(int dataVersion) {
		super(dataVersion);
		FTBJanitor.LOGGER.info("Lazy DFU Loaded");
	}

	@Override
	public DataFixer build(Executor executor) {
		return super.build(NO_OP_EXECUTOR);
	}
}