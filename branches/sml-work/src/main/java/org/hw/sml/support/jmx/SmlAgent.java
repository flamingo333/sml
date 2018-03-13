package org.hw.sml.support.jmx;

import java.util.Date;

import org.hw.sml.FrameworkConstant;
import org.hw.sml.support.ManagedThread;
import org.hw.sml.support.SmlAppContextUtils;
import org.hw.sml.support.security.CyptoUtils;
import org.hw.sml.tools.DateTools;

public class SmlAgent implements SmlAgentMBean {
	@Override
	public int clear(String key) {
		if(key==null||key.equals("all")){
			key="";
		}
		return SmlAppContextUtils.getSmlContextUtils().clear(key);
	}

}
