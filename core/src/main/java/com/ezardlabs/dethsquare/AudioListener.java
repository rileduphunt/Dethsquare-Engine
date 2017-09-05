package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.util.Dethsquare;

public class AudioListener extends Script {

	@Override
	public void update() {
		Dethsquare.AUDIO.setListenerPosition(transform.position.x, transform.position.y);
	}
}
