package com.ezardlabs.dethsquare.networking;

import com.ezardlabs.dethsquare.Script;

public class NetworkScript extends Script {

	protected final void sendMessage(String command, String message) {
		Network.sendMessage(this, command, message);
	}

	protected void receiveMessage(String command, String message) {
		// only used in subclasses
	}
}
