package com.ezardlabs.dethsquare.util;

import com.ezardlabs.dethsquare.util.audio.OggMusic;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;

import static com.ezardlabs.dethsquare.util.Utils.IO;
public class DesktopAudioUtils implements AudioUtils {
	private static HashMap<Integer, AudioThread> playingAudio = new HashMap<>();

	private static class AudioThread extends Thread {
		private final String path;
		private boolean loop = false;
		private int volume = 100;
		private byte[] data;
		private OggMusic ogg;

		private AudioThread(String path) {
			this.path = path;
		}

		private void setLoop(boolean loop) {
			this.loop = loop;
		}

		private void setVolume(int volume) {
			this.volume = volume;
			if (ogg != null) ogg.setVolume(volume);
		}

		@Override
		public void run() {
			ogg = new OggMusic(Thread.currentThread());
			ogg.setVolume(volume);
			ogg.setMute(false);
			try {
				data = IOUtils.toByteArray(
						Thread.currentThread().getContextClassLoader().getResourceAsStream(path));
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}
			do {
				ogg.playOgg(new ByteArrayInputStream(data));
			} while (loop);
		}
	}

	public void playAudio(final int id, final String path) {
		AudioThread at = new AudioThread(path);
		playingAudio.put(id, at);
		at.start();
	}

	public void setAudioLoop(int id, boolean loop) {
		playingAudio.get(id).setLoop(loop);
	}

	public void setAudioVolume(int id, int volume) {
		playingAudio.get(id).setVolume(volume);
	}

	public void stopAudio(int id) {
		if (playingAudio.containsKey(id)) {
			playingAudio.remove(id).stop();
		}
	}

	public void stopAllAudio() {
		playingAudio.values().forEach(Thread::stop);
	}

	private static ByteBuffer loadAudio(String path) throws IOException {
		byte[] bytes = IOUtils.toByteArray(IO.getInputStream(path));
		ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
		buffer.put(bytes);
		buffer.flip();
		return buffer;
	}
}
