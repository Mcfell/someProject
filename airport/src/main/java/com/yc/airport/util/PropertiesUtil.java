package com.yc.airport.util;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public class PropertiesUtil {
	private Properties pro;
	/**
	 * 默认config.properties
	 */
	public PropertiesUtil() {
		pro = new Properties();
		InputStream path = this.getClass().getClassLoader().getResourceAsStream("config.properties");
		try {
			pro.load(path);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * 加载指定路径配置文件
	 * @param path
	 */
	public PropertiesUtil(String path) {
		pro = new Properties();
		InputStream paths = this.getClass().getClassLoader().getResourceAsStream(path);
		try {
			pro.load(paths);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * 读取单条配置信息
	 * @param propertie
	 * @return
	 */
	public String readPropertie(String propertie){
		return pro.getProperty(propertie);
	}
	
}
