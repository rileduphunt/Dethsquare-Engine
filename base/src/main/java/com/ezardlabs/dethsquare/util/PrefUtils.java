package com.ezardlabs.dethsquare.util;

/**
 * Interface for managing preferences in a platform-specific way
 */
public interface PrefUtils {
	/**
	 * Set a boolean value
	 *
	 * @param key   The name of the preference to modify
	 * @param value The new value for the preference
	 */
	void setBoolean(String key, boolean value);

	/**
	 * Set an int value
	 *
	 * @param key   The name of the preference to modify
	 * @param value The new value for the preference
	 */
	void setInt(String key, int value);

	/**
	 * Set a float value
	 *
	 * @param key   The name of the preference to modify
	 * @param value The new value for the preference
	 */
	void setFloat(String key, float value);

	/**
	 * Set a string value
	 *
	 * @param key   The name of the preference to modify
	 * @param value The new value for the preference
	 */
	void setString(String key, String value);

	/**
	 * Retrieve a boolean value
	 *
	 * @param key          The name of the preference to retrieve
	 * @param defaultValue Value to return if this preference does not exist
	 * @return Returns the preference value if it exists, or defaultValue if
	 * it does not
	 */
	boolean getBoolean(String key, boolean defaultValue);

	/**
	 * Retrieve a boolean value
	 *
	 * @param key          The name of the preference to retrieve
	 * @param defaultValue Value to return if this preference does not exist
	 * @return Returns the preference value if it exists, or defaultValue if
	 * it does not
	 */
	int getInt(String key, int defaultValue);

	/**
	 * Retrieve a boolean value
	 *
	 * @param key          The name of the preference to retrieve
	 * @param defaultValue Value to return if this preference does not exist
	 * @return Returns the preference value if it exists, or defaultValue if
	 * it does not
	 */
	float getFloat(String key, float defaultValue);

	/**
	 * Retrieve a boolean value
	 *
	 * @param key          The name of the preference to retrieve
	 * @param defaultValue Value to return if this preference does not exist
	 * @return Returns the preference value if it exists, or defaultValue if
	 * it does not
	 */
	String getString(String key, String defaultValue);
}