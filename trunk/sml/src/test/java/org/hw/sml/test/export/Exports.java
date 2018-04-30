package org.hw.sml.test.export;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;

import org.hw.sml.tools.Https;

public class Exports {
	public static void main(String[] args) throws FileNotFoundException, IOException, IllegalArgumentException, SecurityException, IllegalAccessException, NoSuchFieldException {
		//导出 postform
		Https https=Https.newPostFormHttps("http://localhost:8080/if-web/sml/export/export");
		//params 指定接口id-查询条件，exportType  json字符串
		https.getParamer().add("params","{\"ifId\":\"if-test-page\",\"exportType\":\"xlsx\"}");
		https.getParamer().add("FileTitle", "学生信息");
		//一级分隔符  ,#,;#;   二级分隔符   ,#,
		https.getParamer().add("HeaderTitle","学号,#,id,#,;#;姓名,#,name");
		https.getParamer().add("sidx","id");
		https.getParamer().add("sord","asc");//排序字段
		https.bos(new FileOutputStream("d:/temp/学生信息2.xlsx")).execute();
		
		
		
		
		
		System.out.println(https.buildUrl(https.getParamer().builder("utf-8")));
		Field field=https.getParamer().getClass().getDeclaredField("queryParamStr");
		field.setAccessible(true);
		System.out.println(field.get(https.getParamer()));
	}
}
