package com.ezardlabs.dethsquare;

import static com.ezardlabs.dethsquare.util.Utils.AUDIO;

public final class AudioSource extends Component {
	private final AudioClip initial;
	private int current = -1;
	private boolean loop;
	private int volume;

	public AudioSource() {
		this(null, false, 50);
	}

	public AudioSource(AudioClip audioClip) {
		this(audioClip, false, 50);
	}

	public AudioSource(AudioClip audioClip, boolean loop) {
		this(audioClip, loop, 50);
	}

	public AudioSource(AudioClip audioClip, int volume) {
		this(audioClip, false, volume);
	}

	public AudioSource(AudioClip audioClip, boolean loop, int volume) {
		initial = audioClip;
		this.loop = loop;
		this.volume = volume;
	}

	@Override
	public void start() {
		if (initial != null) {
			current = initial.id;
			play();
			setLoop(loop);
			setVolume(volume);
		}
	}

	public void play() {
		AUDIO.play(current);
	}

	public void pause() {
		AUDIO.pause(current);
	}

	public void stop() {
		AUDIO.stop(current);
	}

	public void setLoop(boolean loop) {
		this.loop = loop;
		AUDIO.setLoop(current, loop);
	}

	public void setVolume(int volume) {
		this.volume = volume;
		AUDIO.setVolume(current, volume);
	}

	public void setAudioClip(AudioClip audioClip) {
		current = audioClip.id;
	}

	public static final class AudioClip {
		private static int idCount = 0;
		private int id;

		public AudioClip(String path) {
			id = idCount++;
			AUDIO.create(id, path);
		}

		@Override
		public boolean equals(Object obj) {
			return (obj instanceof AudioClip) && ((AudioClip) obj).id == id;
		}
	}
}
