package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.util.Dethsquare;

public final class Camera extends Script implements Bounded {
	public static Camera main;
	private final RectF bounds = new RectF();
	private boolean isMain = false;

	public Camera(boolean main) {
		isMain = main;
	}

	@Override
	public void start() {
		if (isMain) main = this;
	}

	public void update() {
		bounds.left = transform.position.x;
		bounds.top = transform.position.y;
		bounds.right = bounds.left + Screen.width;
		bounds.bottom = bounds.top + Screen.height;

		if (main == this) {
			Dethsquare.RENDER.setCameraPosition(transform.position.x, transform.position.y);
		}
	}

	@Override
	public RectF getBounds() {
		return bounds;
	}

	@Override
	public GameObject getGameObject() {
		return gameObject;
	}
}
