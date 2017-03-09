package com.ezardlabs.dethsquare.multiplayer;

import com.ezardlabs.dethsquare.Component.RequiredComponents;
import com.ezardlabs.dethsquare.Transform;

import java.nio.ByteBuffer;

@RequiredComponents(Transform.class)
public class NetworkTransform extends NetworkBehaviour {

	@Override
	protected ByteBuffer onSend() {
		data.position(0);
		data.putFloat(0, transform.position.x); // 0 - 3
		data.putFloat(4, transform.position.y); // 4 - 7
		data.putFloat(8, transform.scale.x); // 8 - 11
		data.putFloat(12, transform.scale.y); // 12 - 15
		return data;
	}

	@Override
	protected void onReceive(ByteBuffer data, int index) {
		transform.position.set(data.getFloat(index), data.getFloat(index + 4));
		if (gameObject.collider != null) {
			gameObject.collider.recalculateBounds();
			gameObject.collider.triggerCheck();
		}
		transform.scale.set(data.getFloat(index + 8), data.getFloat(index + 12));
	}

	@Override
	public short getSize() {
		return 16;
	}
}
