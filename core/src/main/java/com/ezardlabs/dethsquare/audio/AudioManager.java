package com.ezardlabs.dethsquare.audio;

import java.util.ArrayList;

public class AudioManager {
	private static float masterVolume = 0.5f;
	private static float musicVolume = 0.5f;
	private static float sfxVolume = 0.5f;
	private static ArrayList<AudioSource> audioSources = new ArrayList<>();

	public enum AudioGroup {
		NONE,
		MUSIC,
		SFX
	}

	public static void setMasterVolume(float volume) {
		rangeCheck(volume);
		masterVolume = volume;
		updateVolumes(null);
	}

	public static void setMusicVolume(float volume) {
		rangeCheck(volume);
		musicVolume = volume;
		updateVolumes(AudioGroup.MUSIC);
	}

	public static void setSfxVolume(float volume) {
		rangeCheck(volume);
		sfxVolume = volume;
		updateVolumes(AudioGroup.SFX);
	}

	private static void updateVolumes(AudioGroup audioGroup) {
		for (AudioSource audioSource : audioSources) {
			if (audioGroup == null || audioSource.getAudioGroup() == audioGroup) {
				audioSource.setVolume(audioSource.getVolume());
			}
		}
	}

	static void addAudioSource(AudioSource audioSource) {
		audioSources.add(audioSource);
	}

	static void removeAudioSource(AudioSource audioSource) {
		audioSources.remove(audioSource);
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

	public static void clearAll() {
		audioSources.clear();
	}
}
