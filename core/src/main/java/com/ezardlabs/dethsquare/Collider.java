package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.util.GameListeners;
import com.ezardlabs.dethsquare.util.GameListeners.UpdateListener;

import java.util.ArrayList;

public final class Collider extends Component implements Bounded {
	static {
		GameListeners.addUpdateListener(new UpdateListener() {
			@Override
			public void onUpdate() {
				if (rebuildQuadTree) {
					qt.build(staticColliders);
					rebuildQuadTree = false;
				}
			}
		});
	}

	public static final ArrayList<Collider> normalColliders = new ArrayList<>();
	public static final ArrayList<Collider> staticColliders = new ArrayList<>();
	public static final ArrayList<Collider> triggerColliders = new ArrayList<>();
	public static QuadTree<Collider> qt = new QuadTree<>(20);
	private static boolean inited = false;
	private static boolean rebuildQuadTree = false;
	public final RectF lastBounds = new RectF();
	private final float height;
	private final float width;
	ArrayList<Collider> possible = new ArrayList<>();
	private Collider[] triggers = new Collider[0];
	private boolean isTrigger = false;
	private final Vector2 offset = new Vector2();

	private final RectF bounds = new RectF();

	public enum CollisionLocation {
		TOP,
		RIGHT,
		BOTTOM,
		LEFT
	}

	public class Collision {
		/**
		 * The {@link Collider} that was hit
		 */
		public final Collider collider;
		/**
		 * The {@link GameObject} of the {@link Collider} that was hit
		 */
		public final GameObject gameObject;
		/**
		 * The side of the {@link Collider} that was involved in the collision
		 */
		public final CollisionLocation location;
		/**
		 * The speed at which the collision occurred
		 */
		public final float speed;
		/**
		 * The {@link Transform} of the {@link Collider} that was hit
		 */
		public final Transform transform;
		/**
		 * The {@link Rigidbody} of the {@link Collider} that was hit
		 */
		public final Rigidbody rigidbody;

		Collision(Collider collider, GameObject gameObject, Transform transform, Rigidbody rigidbody,
				CollisionLocation location, float speed) {
			this.collider = collider;
			this.gameObject = gameObject;
			this.transform = transform;
			this.rigidbody = rigidbody;
			this.location = location;
			this.speed = speed;
		}
	}

	public Collider(float width, float height) {
		this(width, height, 0, 0, false);
	}

	public Collider(float width, float height, boolean isTrigger) {
		this(width, height, 0, 0, isTrigger);
	}

	public Collider(float width, float height, float offsetX, float offsetY) {
		this(width, height, offsetX, offsetY, false);
	}

	public Collider(float width, float height, float offsetX, float offsetY, boolean isTrigger) {
		this.width = width;
		this.height = height;
		this.offset.set(offsetX, offsetY);
		this.isTrigger = isTrigger;
	}

	public static void init() {
		qt.build(staticColliders);
		inited = true;
	}

	static void clearAll() {
		normalColliders.clear();
		staticColliders.clear();
		triggerColliders.clear();
		qt = new QuadTree<>(20);
		inited = false;
	}

	public void start() {
		if (gameObject.isStatic) {
			staticColliders.add(this);
			if (inited) {
				rebuildQuadTree = true;
			}
		}
		if (isTrigger) addTrigger();
		if (!gameObject.isStatic && !isTrigger) normalColliders.add(this);
		recalculateBounds();
	}

	private void addTrigger() {
		triggerColliders.add(this);
		triggers = new Collider[triggerColliders.size()];
	}

	private void removeTrigger() {
		triggerColliders.remove(this);
		triggers = new Collider[triggerColliders.size()];
	}

	public void setIsTrigger(boolean isTrigger) {
		if (isTrigger == this.isTrigger) return;
		this.isTrigger = isTrigger;
		if (isTrigger) {
			addTrigger();
		} else {
			removeTrigger();
		}
	}

	public void destroy() {
		if (gameObject.isStatic) staticColliders.remove(this);
		if (isTrigger) removeTrigger();
		if (!gameObject.isStatic && !isTrigger) normalColliders.remove(this);
	}

	void move(float x, float y, float xSpeed, float ySpeed) {
		if (isTrigger) {
			transform.position.x += x;
			transform.position.y += y;
			recalculateBounds();
			triggerCheck();
			return;
		}
		possible.clear();

		lastBounds.left = bounds.left;
		lastBounds.top = bounds.top;
		lastBounds.right = bounds.right;
		lastBounds.bottom = bounds.bottom;
		if (x > 0) bounds.right += x;
		if (x < 0) bounds.left += x;
		if (y > 0) bounds.bottom += y;
		if (y < 0) bounds.top += y;

		qt.retrieve(possible, this);

		bounds.left = lastBounds.left;
		bounds.top = lastBounds.top;
		bounds.right = lastBounds.right;
		bounds.bottom = lastBounds.bottom;

		if (possible.isEmpty()) {
			transform.position.x += x;
			transform.position.y += y;
		} else {
			transform.position.y += y;
			recalculateBounds();
			Collider c;
			for (int i = 0; i < possible.size(); i++) {
				c = possible.get(i);
				if (c != this && c != null && !c.isTrigger && RectF.intersects(bounds, c.bounds)) {
					if (y > 0 && bounds.bottom > c.bounds.top) {
						transform.position.y = c.bounds.top - bounds.height() - offset.y;
						gameObject.onCollision(new Collision(c, c.gameObject, c.transform, c.gameObject.rigidbody,
								CollisionLocation.BOTTOM, ySpeed / Time.fpsScaling60));
					} else if (y < 0 && bounds.top < c.bounds.bottom) {
						transform.position.y = c.bounds.bottom - offset.y;
						if (transform.position.y != lastBounds.top) {
							gameObject.onCollision(new Collision(c, c.gameObject, c.transform, c.gameObject.rigidbody,
									CollisionLocation.TOP, ySpeed / Time.fpsScaling60));
						}
					}
					if (gameObject.rigidbody != null) {
						gameObject.rigidbody.velocity.y = 0;
					}
					recalculateBounds();
				}
			}
			transform.position.x += x;
			recalculateBounds();
			for (int i = 0; i < possible.size(); i++) {
				c = possible.get(i);
				if (c != this && c != null && !c.isTrigger && RectF.intersects(bounds, c.bounds)) {
					if (x > 0 && bounds.right > c.bounds.left) {
						transform.position.x = c.bounds.left - bounds.width() - offset.x;
						if (transform.position.x != lastBounds.left) {
							gameObject.onCollision(new Collision(c, c.gameObject, c.transform, c.gameObject.rigidbody,
									CollisionLocation.RIGHT, xSpeed / Time.fpsScaling60));
						}
					} else if (x < 0 && bounds.left < c.bounds.right) {
						transform.position.x = c.bounds.right - offset.x;
						if (transform.position.x != lastBounds.left) {
							gameObject.onCollision(new Collision(c, c.gameObject, c.transform, c.gameObject.rigidbody,
									CollisionLocation.LEFT, xSpeed / Time.fpsScaling60));
						}
					}
					if (gameObject.rigidbody != null) {
						gameObject.rigidbody.velocity.x = 0;
					}
					recalculateBounds();
				}
			}
		}
		recalculateBounds();
		triggerCheck();
	}

	public void recalculateBounds() {
		bounds.left = transform.position.x + offset.x;
		bounds.top = transform.position.y + offset.y;
		bounds.right = transform.position.x + offset.x + width;
		bounds.bottom = transform.position.y + offset.y + height;
	}

	public void triggerCheck() {
		if (isTrigger) {
			possible.clear();
			qt.retrieve(possible, this);
			for (Collider c : possible) {
				if (RectF.intersects(bounds, c.bounds)) {
					gameObject.onTriggerEnter(c);
				}
			}
			for (Collider c : normalColliders) {
				if (c != this && RectF.intersects(bounds, c.bounds)) {
					gameObject.onTriggerEnter(c);
				}
			}
			for (Collider c : triggerColliders.toArray(triggers)) {
				if (c != this && c != null && RectF.intersects(bounds, c.bounds)) {
					c.gameObject.onTriggerEnter(this);
					gameObject.onTriggerEnter(c);
				}
			}
		} else {
			for (Collider c : triggerColliders) {
				if (c != this && RectF.intersects(bounds, c.bounds)) {
					c.gameObject.onTriggerEnter(this);
				}
			}
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
