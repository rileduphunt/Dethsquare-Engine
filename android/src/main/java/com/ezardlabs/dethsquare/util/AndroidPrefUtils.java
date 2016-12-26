package com.ezardlabs.dethsquare.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class AndroidPrefUtils implements PrefUtils {
	private final SharedPreferences prefs;

	public AndroidPrefUtils(Context context) {
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@Override
	public void setBoolean(String key, boolean value) {
		prefs.edit().putBoolean(key, value).commit();
	}

	@Override
	public void setInt(String key, int value) {
		prefs.edit().putInt(key, value).commit();
	}

	@Override
	public void setFloat(String key, float value) {
		prefs.edit().putFloat(key, value).commit();
	}

	@Override
	public void setString(String key, String value) {
		prefs.edit().putString(key, value).commit();
	}

	@Override
	public boolean getBoolean(String key, boolean defaultValue) {
		return prefs.getBoolean(key, defaultValue);
	}

	@Override
	public int getInt(String key, int defaultValue) {
		return prefs.getInt(key, defaultValue);
	}

	@Override
	public float getFloat(String key, float defaultValue) {
		return prefs.getFloat(key, defaultValue);
	}

	@Override
	public String getString(String key, String defaultValue) {
		return prefs.getString(key, defaultValue);
	}
}
