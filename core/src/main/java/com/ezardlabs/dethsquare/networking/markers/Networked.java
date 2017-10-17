package com.ezardlabs.dethsquare.networking.markers;

import java.nio.ByteBuffer;

public interface Networked {
	ByteBuffer onSend();

	void onReceive(ByteBuffer data, int index);

	short getSize();
}
