package org.hw.sml.test.foreach;

import java.io.IOException;

import org.apache.commons.io.IOUtils;
import org.hw.sml.core.resolver.JsEngine;
import org.hw.sml.core.resolver.Rst;
import org.hw.sml.core.resolver.SqlResolvers;
import org.hw.sml.model.SMLParams;
import org.hw.sml.support.el.JsEl;

public class ForeachTest {
 
	public static void main(String[] args) throws IOException {
		JsEngine.evel("");
		String sql=IOUtils.toString(ForeachTest.class.getResourceAsStream("smlParam.txt"));
		long start=System.currentTimeMillis();
		for(int i=0;i<1;i++){
			Rst rst=new SqlResolvers(new JsEl()).init().resolverLinks(sql,new SMLParams().add("a","v1").add("cars",new String[]{"a","b","c"}).add("c","vvv").add("f","1").add("discteteTime","20190112").reinit());
			//rst=new SqlResolvers(new JsEl()).init().resolverLinks(sql,new SMLParams().add("a","v1").add("b",new String[]{"v2","v3","v4"}).add("c","vvv").add("d","1,2,3,4").reinit());
			System.out.println(rst.getSqlString());
			System.out.println(rst.getParamObjects());
		}
		long end=System.currentTimeMillis();
		System.out.println(end-start);
	}
}
