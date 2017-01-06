package com.ezardlabs.dethsquare.multiplayer;

import com.ezardlabs.dethsquare.Component.RequiredComponents;
import com.ezardlabs.dethsquare.Renderer;

import java.nio.ByteBuffer;

@RequiredComponents(Renderer.class)
public class NetworkRenderer extends NetworkBehaviour {

	@Override
	protected ByteBuffer onSend() {
		data.position(0);
		data.putFloat(0, gameObject.renderer.width); // 0 - 3
		data.putFloat(4, gameObject.renderer.height); // 4 - 7
		data.putFloat(8, gameObject.renderer.xOffset); // 8 - 11
		data.putFloat(12, gameObject.renderer.yOffset); // 12 - 15
		return data;
	}

	@Override
	protected void onReceive(ByteBuffer data, int index) {
		gameObject.renderer.setSize(data.getFloat(index), data.getFloat(index + 4));
		gameObject.renderer.setOffsets(data.getFloat(index + 8), data.getFloat(index + 12));
	}

	@Override
	public short getSize() {
		return 16;
	}
}
