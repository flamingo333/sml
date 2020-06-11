package org.hw.sml.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

public class Resources {
	private String name;
	
	public Resources(){
		
	}
	public Resources(String name){
		this.name=name;
	}
	public Properties parse(){
		Properties properties=new Properties();
		Enumeration<URL> ds;
		try {
			ds = ClassUtil.getClassLoader().getResources("");
			List<File> files=MapUtils.newArrayList();
			while(ds.hasMoreElements()){
				File fileDir=new File(ds.nextElement().getFile());
				addFiles(files, fileDir, name);
			}
			for(File file:files){
				String fileType=getSuffix(file.getName());
				if(fileType.equalsIgnoreCase(".properties")){
					loadPropFile(properties, file);
				}else{
					loadFile(properties,file);
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		return properties;
	}
	private void loadFile(Properties properties, File file) {
		BufferedReader bw=null;
		try {
			 bw=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
			 String temp=null;
			 while((temp=bw.readLine())!=null){
				 if(temp.startsWith("#")){
					 continue;
				 }
				 String[] ls=temp.split("=",2);
				 if(ls.length==2){
					 properties.put(ls[0],ls[1]);
				 }
			 }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			IOUtils.safeClose(bw);
		}
	}
	private void loadPropFile(Properties properties,File file){
		Properties props=new Properties();
		FileInputStream fis=null;
		try {
			fis = new FileInputStream(file);
			props.load(fis);
			properties.putAll(props);
		} catch (FileNotFoundException e) {
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			IOUtils.safeClose(fis);
		}
		
	}
	private String getSuffix(String filename){
		return filename.substring(filename.lastIndexOf("."));
	}
	private void addFiles(final List<File> files,File fileDir,final String regex){
	   fileDir.listFiles(new FileFilter() {
			public boolean accept(File file) {
				if(file.isDirectory()){
					addFiles(files,file,regex);
					return false;
				}else{
					boolean flag=false;
					try{
						flag=file.getName().matches(regex);
					}catch(Exception e){}
					if(flag){
						files.add(file);
					}
					return flag;
				}
			}
		});
	}
}
