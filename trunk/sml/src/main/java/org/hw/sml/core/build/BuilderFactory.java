package org.hw.sml.core.build;

import org.hw.sml.core.build.lmaps.AbstractDataBuilder;

public interface BuilderFactory {
	public AbstractDataBuilder getBuilder(String name);
}
