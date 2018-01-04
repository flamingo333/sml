package org.hw.sml.support.jmx;

import org.hw.sml.support.SmlAppContextUtils;

public class SmlAgent implements SmlAgentMBean {
	public int clear(String key) {
		if(key==null||key.equals("all")){
			key="";
		}
		return SmlAppContextUtils.getSmlContextUtils().clear(key);
	}
}
