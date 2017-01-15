package com.ezardlabs.dethsquare;

public class AudioManager {
	private static float masterVolume = 0.5f;
	private static float musicVolume = 0.5f;
	private static float sfxVolume = 0.5f;

	public enum AudioGroup {
		NONE,
		MUSIC,
		SFX
	}

	public static void setMasterVolume(float volume) {
		rangeCheck(volume);
		masterVolume = volume;
	}

	public static void setMusicVolume(float volume) {
		rangeCheck(volume);
		musicVolume = volume;
	}

	public static void setSfxVolume(float volume) {
		rangeCheck(volume);
		sfxVolume = volume;
	}

	public static float getVolume(float volume, AudioGroup audioGroup) {
		rangeCheck(volume);
		switch (audioGroup) {
			case NONE:
				return volume * masterVolume;
			case MUSIC:
				return volume * musicVolume * masterVolume;
			case SFX:
				return volume * sfxVolume * masterVolume;
			default:
				return volume * masterVolume;
		}
	}

	private static void rangeCheck(float volume) {
		if (volume < 0 || volume > 1) {
			throw new IllegalArgumentException("Volume must be between 0 and 1 inclusive");
		}
	}
}
