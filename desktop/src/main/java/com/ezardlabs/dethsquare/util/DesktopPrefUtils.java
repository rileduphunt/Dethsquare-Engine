package com.ezardlabs.dethsquare.util;

import java.nio.charset.Charset;
import java.util.prefs.Preferences;

public class DesktopPrefUtils implements PrefUtils {
	private static final Preferences prefs = Preferences.userRoot().node("8-Bit Warframe/Lost Sector");

	@Override
	public void setBoolean(String key, boolean value) {
		prefs.putBoolean(key, value);
	}

	@Override
	public void setInt(String key, int value) {
		prefs.putInt(key, value);
	}

	@Override
	public void setFloat(String key, float value) {
		prefs.putFloat(key, value);
	}

	@Override
	public void setString(String key, String value) {
		prefs.putByteArray(key, value.getBytes(Charset.forName("UTF-8")));
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
		return new String(prefs.getByteArray(key, defaultValue.getBytes(Charset.forName("UTF-8"))), Charset.forName("UTF-8"));
	}
}
