package org.pan;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;
import java.util.Properties;

/**
 * @author panmingzhi
 * @createTime 2015年3月31日
 * @content 配置文件
 */
public class AppConfigrator {

	public static final String default_charset = "UTF-8";
	
	public static final String key_monitorInterval = "monitorInterval";
	public static final String key_monitorFolder = "monitorFolder";
	public static final String key_targetFolder = "targetFolder";

	public static final String default_monitorInterval = "10";
	public static final String default_monitorFolder = "";
	public static final String default_targetFolder = "";

	private static final String propertiesFilePath = "AppConfigrator.properties";
	private static Properties properties;

	private static void checkProperties() {
		if (properties != null) {
			return;
		}
		properties = new Properties();
		
		Path path = Paths.get(propertiesFilePath);
		if(!Files.exists(path)){	
			try {
				Files.createFile(path);
			} catch (IOException e) {
				e.printStackTrace();
			}
			setProperties("","");
		}
		
		loadProperties();
	}
	
	public static void loadProperties(){
		try (
			FileInputStream fis = new FileInputStream(propertiesFilePath);
			InputStreamReader inputStreamReader = new InputStreamReader(fis,Charset.forName(default_charset))){
			properties.load(inputStreamReader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void setProperties(String key, String value) {
		try (
			FileOutputStream fos = new FileOutputStream(propertiesFilePath, false);
			PrintWriter out = new PrintWriter(fos);
			) {
			out.println("#监控间隔");
			out.println(String.format("%s=%s", key_monitorInterval, key.equals(key_monitorInterval) ? value:getMonitorInterval()));
			out.println("#监控路径");
			out.println(String.format("%s=%s", key_monitorFolder, (key.equals(key_monitorFolder) ?  value:getMonitorFolder()).replaceAll("\\\\", "/")));
			out.println("#目标路径");
			out.println(String.format("%s=%s", key_targetFolder, (key.equals(key_targetFolder) ?  value:getTargetFolder()).replaceAll("\\\\", "/")));
			out.flush();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		loadProperties();
	}

	public static String getMonitorInterval() {
		checkProperties();
		return properties.getProperty(key_monitorInterval, default_monitorInterval);
	}

	public static String getMonitorFolder() {
		checkProperties();
		return properties.getProperty(key_monitorFolder, default_monitorFolder);
	}

	public static String getTargetFolder() {
		checkProperties();
		return properties.getProperty(key_targetFolder, default_targetFolder);
	}

}
