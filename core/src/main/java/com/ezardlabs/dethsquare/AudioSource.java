package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.AudioManager.AudioGroup;

import static com.ezardlabs.dethsquare.AudioManager.AudioGroup.NONE;
import static com.ezardlabs.dethsquare.util.Utils.AUDIO;

public final class AudioSource extends Component {
	private final AudioClip initial;
	private int current = -1;
	private boolean loop;
	private float volume;
	private AudioGroup audioGroup;

	public AudioSource() {
		this(null, false, 50, NONE);
	}

	public AudioSource(AudioClip audioClip) {
		this(audioClip, false, 50, NONE);
	}

	public AudioSource(AudioClip audioClip, boolean loop) {
		this(audioClip, loop, 50, NONE);
	}

	public AudioSource(AudioClip audioClip, int volume) {
		this(audioClip, false, volume, NONE);
	}

	public AudioSource(AudioClip audioClip, AudioGroup audioGroup) {
		this(audioClip, false, 50, audioGroup);
	}

	public AudioSource(AudioClip audioClip, boolean loop, AudioGroup audioGroup) {
		this(audioClip, loop, 50, audioGroup);
	}

	public AudioSource(AudioClip audioClip, int volume, AudioGroup audioGroup) {
		this(audioClip, false, volume, audioGroup);
	}

	public AudioSource(AudioClip audioClip, boolean loop, int volume) {
		this(audioClip, loop, volume, NONE);
	}

	public AudioSource(AudioClip audioClip, boolean loop, int volume, AudioGroup audioGroup) {
		initial = audioClip;
		this.loop = loop;
		this.volume = volume;
		this.audioGroup = audioGroup;
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

	public void setVolume(float volume) {
		volumeRangeCheck(volume);
		this.volume = volume;
		AUDIO.setVolume(current, AudioManager.getVolume(volume, audioGroup));
	}

	public void setAudioClip(AudioClip audioClip) {
		current = audioClip.id;
	}

	private static void volumeRangeCheck(float volume) {
		if (volume < 0 || volume > 1) {
			throw new IllegalArgumentException("Volume must be between 0 and 1 inclusive");
		}
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
