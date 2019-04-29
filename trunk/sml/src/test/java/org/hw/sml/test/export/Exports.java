package org.hw.sml.test.export;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;

import org.hw.sml.tools.Https;
import org.hw.sml.tools.MapUtils;
import org.hw.sml.tools.Maps;

import com.alibaba.fastjson.JSON;

public class Exports {
	public static void main1(String[] args) throws FileNotFoundException, IOException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		//导出 postform
		Https https=Https.newPostFormHttps("http://localhost:8080/SMP/sml/export/if-test-page");
		//params 指定接口id-查询条件，exportType  json字符串
		https.getParamer().add("params","{\"ifId\":\"if-test-page\",\"exportType\":\"xlsx\"}");
		https.getParamer().add("FileTitle", "学生信息");
		//一级分隔符  ,#,;#;   二级分隔符   ,#,
		https.getParamer().add("HeaderTitle","学号,#,id,#,;#;姓名,#,name");
		https.getParamer().add("sidx","id");
		https.getParamer().add("sord","asc");//排序字段
		https.bos(new FileOutputStream("d:/temp/学生信息2.xlsx")).execute();
	}
	public static void main(String[] args) throws Exception{
		Map<String,Object> params=MapUtils.newHashMap();
		params.put("title","学生信息");
		params.put("propertys",Arrays.asList("id","name"));
		params.put("heads",Arrays.asList("学号","姓名"));
		params.put("type","csv");
		params.put("datas",Arrays.asList(new String[]{"1","hw"},new String[]{"2","wh"}));
		String formParams=JSON.toJSON(params).toString();
		Https https=Https.newPostFormHttps("http://localhost:8080/core-war/sml/export/exportOriginal");
		https.param("params",formParams);
		https.bos(new FileOutputStream("d:/temp/学生信息-1.xls")).execute();
		System.out.println(formParams);
	}
}
