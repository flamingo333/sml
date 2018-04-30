package sml;

import java.io.IOException;

import org.hw.sml.core.resolver.JsEngine;
import org.hw.sml.core.resolver.Rst;
import org.hw.sml.core.resolver.SqlResolvers;
import org.hw.sml.support.el.JsEl;
import org.hw.sml.support.time.StopWatch;
import org.hw.sml.tools.DateTools;
import org.hw.sml.tools.IOUtils;

import com.eastcom_sw.inas.core.service.jdbc.SqlParams;

public class SmlParamTest {
	public static void main(String[] args) throws IOException {

		SqlResolvers srs=new SqlResolvers(new JsEl());
		srs.init();
		//duankouIdStr
		//shebeiIdStr
		Rst rst=srs.resolverLinks(
				IOUtils.toString(SmlParamTest.class.getResourceAsStream("smlParam.txt"), "utf8"),
				new SqlParams()
					.add("duankouIdStr","")
					.add("shebeiIdStr", "1,12,23,4")
					.reinit()
		);
		//System.out.println(rst);
		System.out.println(rst.getPrettySqlString());
	}
}
