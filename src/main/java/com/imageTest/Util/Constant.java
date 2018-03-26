package com.imageTest.Util;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.databind.ObjectMapper;

public final class Constant {

	public static String propsFile = "imagedownloader.properties";
	public static Properties properties = null;
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	public static ObjectMapper getObjectMapper() {
		return OBJECT_MAPPER;
	}

	public Constant() {
		loadProperties();
	}

	public void loadProperties() {
		InputStream input = null;

		try {
			input = Thread.currentThread().getContextClassLoader().getResourceAsStream(propsFile);
			if (properties == null)
				properties = new Properties();
				properties.load(input);

		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			if (null != input) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public List<String> getEarthDates(){
    	String[] eDates =  getProperty("earthDates").split(",");
    	SimpleDateFormat sourceDateFormat = new SimpleDateFormat(getProperty("sourceDateFormat"));
    	SimpleDateFormat targetDateFormat = new SimpleDateFormat(getProperty("targetDateFormat"));
    	List<String> frmtDt = new ArrayList<>();
    	try {
    		for (String dt : eDates) {
    			Date date = sourceDateFormat.parse(dt);
    			frmtDt.add(targetDateFormat.format(date));
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
        return frmtDt;
    }
	
	public String getHostUrl(){
        return getProperty("host");
    }
	
	public String getLocalPathToDownload(){
        return getProperty("img_storage_path");
    }

	public String getProperty(String key) {
		return properties.getProperty(key);
	}

}