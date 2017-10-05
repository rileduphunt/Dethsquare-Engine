package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.Vector2.Vector2ChangeListener;

import java.util.ArrayList;

/**
 * {@link Component} that contains the position of a {@link GameObject}
 */
public final class Transform extends Component {
	public Vector2 position = new Vector2();
	public Vector2 scale = new Vector2(1, 1);

	Transform parent;
	ArrayList<Transform> children = new ArrayList<>();

	Transform(GameObject gameObject) {
		this.gameObject = gameObject;
		position.setVector2ChangeListener(new Vector2ChangeListener() {
			@Override
			public void onVector2Changed(float xDiff, float yDiff) {
				for (int i = 0; i < children.size(); i++) {
					Transform child = children.get(i);
					child.position.set(child.position.x + xDiff, child.position.y + yDiff);
				}
			}
		});
	}

	public void setParent(Transform parent) {
		setParent(parent, true);
	}

	private void setParent(Transform parent, boolean addChild) {
		this.parent = parent;
		if (addChild) parent.addChild(this, false);
	}

	public Transform getParent() {
		return parent == null || parent.gameObject == GameObject.rootObject ? null : parent;
	}

	public void addChild(Transform child) {
		addChild(this, true);
	}

	private void addChild(Transform child, boolean setParent) {
		children.add(child);
		if (setParent) child.setParent(this, false);
	}

	public Transform getChild(int index) {
		return children.get(index);
	}

	/**
	 * Move the {@link GameObject} that this {@link Transform} is attached to. If the {@link GameObject} has a {@link Collider} attached, then the physics
	 *
	 * @param x
	 * @param y
	 */
	public void translate(float x, float y) {
		translate(x, y, x, y);
	}

	void translate(float x, float y, float xSpeed, float ySpeed) {
		if (gameObject.isStatic) throw new StaticObjectMovedError(gameObject);
		if (x == 0 && y == 0) return;
		float startX = position.x;
		float startY = position.y;
		if (gameObject.collider == null) {
			position.x += x;
			position.y += y;
		} else {
			gameObject.collider.move(x, y, xSpeed, ySpeed);
		}
		for (int i = 0; i < children.size(); i++) {
			children.get(i).translate(position.x - startX, position.y - startY);
		}
	}

	private static class StaticObjectMovedError extends Error {

		private StaticObjectMovedError(GameObject staticGameObject) {
			super("Tried to move GameObject '" + staticGameObject.name + "', but it is " +
					"marked as static and static objects cannot be moved");
		}
	}
}
