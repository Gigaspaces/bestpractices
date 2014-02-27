package com.mycompany.app.common.utils;

public class StringUtils {
	
	private StringUtils() {}
	
	public static boolean validateLength(String original) {
		if(original != null && original.length() > 0)
			return true;
		else
			return false;
	}
	
	public static String trim(String original) {
		if(original != null && original.length() > 0)
			return original.trim();
		else
			return Constants.EMPTY_STRING;
	}
	
	public static Integer stringToInteger(String s) {
		Integer i;
		try {
			i = new Integer(s);
		}catch(Exception e) {
			return new Integer(0);
		}
		return i;
	}
	
}