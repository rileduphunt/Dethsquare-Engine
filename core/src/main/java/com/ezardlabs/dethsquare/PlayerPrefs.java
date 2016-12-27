package com.ezardlabs.dethsquare;

import static com.ezardlabs.dethsquare.util.Utils.PREFS;

public final class PlayerPrefs {

	public static void setBoolean(String key, boolean value) {
		PREFS.setBoolean(key, value);
	}

	public static void setInt(String key, int value) {
		PREFS.setInt(key, value);
	}

	public static void setFloat(String key, float value) {
		PREFS.setFloat(key, value);
	}

	public static void setString(String key, String value) {
		PREFS.setString(key, value);
	}

	public static boolean getBoolean(String key, boolean defaultValue) {
		return PREFS.getBoolean(key, defaultValue);
	}

	public static int getInt(String key, int defaultValue) {
		return PREFS.getInt(key, defaultValue);
	}

	public static float getFloat(String key, float defaultValue) {
		return PREFS.getFloat(key, defaultValue);
	}

	public static String getString(String key, String defaultValue) {
		return PREFS.getString(key, defaultValue);
	}
}
