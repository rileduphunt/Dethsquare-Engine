package com.ezardlabs.dethsquare.util;

public interface PrefUtils {
	void setBoolean(String key, boolean value);

	void setInt(String key, int value);

	void setFloat(String key, float value);

	void setString(String key, String value);

	boolean getBoolean(String key, boolean defaultValue);

	int getInt(String key, int defaultValue);

	float getFloat(String key, float defaultValue);

	String getString(String key, String defaultValue);
}