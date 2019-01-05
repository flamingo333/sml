package org.hw.sml.core.build;

import org.hw.sml.core.RebuildParam;

public interface ValueHandler {
	public Object handler(Object value,RebuildParam rebuildParam);
}
