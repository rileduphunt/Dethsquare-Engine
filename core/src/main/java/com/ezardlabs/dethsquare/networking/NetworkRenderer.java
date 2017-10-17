package com.ezardlabs.dethsquare.networking;

import com.ezardlabs.dethsquare.Component;
import com.ezardlabs.dethsquare.Component.RequiredComponents;
import com.ezardlabs.dethsquare.graphics.Renderer;
import com.ezardlabs.dethsquare.networking.markers.Networked;

import java.nio.ByteBuffer;

@RequiredComponents(Renderer.class)
public class NetworkRenderer extends Component implements Networked {
	private final ByteBuffer data = ByteBuffer.allocate(getSize());

	@Override
	public ByteBuffer onSend() {
		data.position(0);
		data.putFloat(0, gameObject.renderer.width); // 0 - 3
		data.putFloat(4, gameObject.renderer.height); // 4 - 7
		data.putFloat(8, gameObject.renderer.xOffset); // 8 - 11
		data.putFloat(12, gameObject.renderer.yOffset); // 12 - 15
		return data;
	}

	@Override
	public void onReceive(ByteBuffer data, int index) {
		gameObject.renderer.setSize(data.getFloat(index), data.getFloat(index + 4));
		gameObject.renderer.setOffsets(data.getFloat(index + 8), data.getFloat(index + 12));
	}

	@Override
	public short getSize() {
		return 16;
	}
}
