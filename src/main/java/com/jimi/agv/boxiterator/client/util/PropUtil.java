package com.jimi.agv.boxiterator.client.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropUtil {

	public static String getString(String filePath, String key) {
		Properties properties = getPropertiesFile(filePath);
		return properties.getProperty(key);
	}

	
	public static Integer getInt(String filePath, String key) {
		Properties properties = getPropertiesFile(filePath);
		return Integer.parseInt(properties.getProperty(key));
	}
	
	
	public static Boolean getBoolean(String filePath, String key) {
		Properties properties = getPropertiesFile(filePath);
		return Boolean.parseBoolean(properties.getProperty(key));
	}

	
	private static Properties getPropertiesFile(String filePath) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(new File(filePath)));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return properties;
	}
	
}
