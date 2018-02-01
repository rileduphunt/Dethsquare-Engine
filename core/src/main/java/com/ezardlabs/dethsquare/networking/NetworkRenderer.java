package com.ezardlabs.dethsquare.networking;

import com.ezardlabs.dethsquare.Component.RequiredComponents;
import com.ezardlabs.dethsquare.graphics.Renderer;

import java.nio.ByteBuffer;

@RequiredComponents(Renderer.class)
public class NetworkRenderer extends NetworkScript {
	private final ByteBuffer data = ByteBuffer.allocate(getSize());

	@Override
	public ByteBuffer onSend() {
		data.position(0);
		data.putFloat(0, gameObject.renderer.width); // 0 - 3
		data.putFloat(4, gameObject.renderer.height); // 4 - 7
		data.putFloat(8, gameObject.renderer.xOffset); // 8 - 11
		data.putFloat(12, gameObject.renderer.yOffset); // 12 - 15
		data.putFloat(16, gameObject.renderer.getTint()[0]); // 16 - 19
		data.putFloat(20, gameObject.renderer.getTint()[1]); // 20 - 23
		data.putFloat(24, gameObject.renderer.getTint()[2]); // 14 - 27
		data.putFloat(28, gameObject.renderer.getTint()[3]); // 28 - 31
		return data;
	}

	@Override
	public void onReceive(ByteBuffer data, int index) {
		gameObject.renderer.setSize(data.getFloat(index), data.getFloat(index + 4));
		gameObject.renderer.setOffsets(data.getFloat(index + 8), data.getFloat(index + 12));
		gameObject.renderer.setTint(data.getFloat(index + 16), data.getFloat(index + 20), data.getFloat(index + 24),
				data.getFloat(index + 28));
	}

	@Override
	public short getSize() {
		return 32;
	}
}
