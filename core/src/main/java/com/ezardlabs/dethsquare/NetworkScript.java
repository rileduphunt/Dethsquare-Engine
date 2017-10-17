package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.networking.Network;

public class NetworkScript extends Script {

	protected final void sendMessage(String command, String message) {
		Network.sendMessage(this, command, message);
	}

	protected void receiveMessage(String command, String message) {
		// only used in subclasses
	}
}
