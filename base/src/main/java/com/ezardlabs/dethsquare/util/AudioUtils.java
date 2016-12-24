package com.ezardlabs.dethsquare.util;

public interface AudioUtils {
	void playAudio(final int id, final String path);

	void setAudioLoop(int id, boolean loop);

	void setAudioVolume(int id, int volume);

	void stopAudio(int id);

	void stopAllAudio();
}
