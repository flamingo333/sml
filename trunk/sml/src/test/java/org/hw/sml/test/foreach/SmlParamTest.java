package org.hw.sml.test.foreach;

import java.io.IOException;

import org.hw.sml.core.resolver.JsEngine;
import org.hw.sml.core.resolver.Rst;
import org.hw.sml.core.resolver.SqlResolvers;
import org.hw.sml.model.SMLParams;
import org.hw.sml.support.el.JsEl;
import org.hw.sml.support.time.StopWatch;
import org.hw.sml.tools.DateTools;
import org.hw.sml.tools.IOUtils;

public class SmlParamTest {
	public static void main(String[] args) throws IOException {
		JsEngine.evel("");
		StopWatch sw=new StopWatch("");
		sw.start("1");
		System.out.println(JsEngine.evel("['1',2,3,4].contains(1)"));
		sw.stop();
		sw.start("2");
		System.out.println(DateTools.getFormatTime("201751","yyyyww"));
		String sql=IOUtils.toString(SmlParamTest.class.getResourceAsStream("smlParam.txt"),"gbk");
		sw.stop();
		sw.start("3");
		SqlResolvers srs=new SqlResolvers(new JsEl());
		srs.init();
		sw.stop();
		sw.start("4");
		Rst rst=srs.resolverLinks(sql,new SMLParams().add("discteteTime","201706000000,201707000000,201708000000").add("f", "1").reinit());
		sw.stop();
		sw.start("5");
		System.out.println(rst.getSqlString());
		System.out.println(rst.getParamObjects());
		System.out.println(rst.getPrettySqlString());
		sw.stop();
		
		System.out.println(sw.prettyPrint());
		System.out.println(266&255);
	}
}
