package com.ezardlabs.dethsquare;

/**
 * Extra functions to supplement {@link java.lang.Math java.lang.Mathf}
 */
public class Mathf {

	public static float clamp(float value, float min, float max) {
		if (value < min) {
			return min;
		} else if (value > max) {
			return max;
		} else {
			return value;
		}
	}
}
