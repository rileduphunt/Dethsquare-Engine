package com.ezardlabs.dethsquare.networking;

import com.ezardlabs.dethsquare.Component;
import com.ezardlabs.dethsquare.animation.Animator;
import com.ezardlabs.dethsquare.Component.RequiredComponents;
import com.ezardlabs.dethsquare.networking.markers.Networked;

import java.nio.ByteBuffer;

@RequiredComponents(Animator.class)
public class NetworkAnimator extends Component implements Networked {
	private final ByteBuffer data = ByteBuffer.allocate(getSize());

	@Override
	public void start() {
		super.start();
		gameObject.animator.enabled = gameObject.playerId == Network.getPlayerId();
	}

	@Override
	public ByteBuffer onSend() {
		data.position(0);
		data.putShort(0, (short) gameObject.animator.getCurrentAnimationId()); // 0 - 1
		data.putShort(2, (short) gameObject.animator.getCurrentAnimationFrame()); // 2 - 3
		return data;
	}

	@Override
	public void onReceive(ByteBuffer data, int index) {
		gameObject.animator.setCurrentAnimationId(data.getShort(index));
		gameObject.animator.setCurrentAnimationFrame(data.getShort(index + 2));
	}

	@Override
	public short getSize() {
		return 4;
	}
}
