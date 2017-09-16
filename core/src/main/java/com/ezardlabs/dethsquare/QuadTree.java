package com.ezardlabs.dethsquare;

import java.util.ArrayList;
import java.util.TreeMap;

@SuppressWarnings("unchecked")
public final class QuadTree<T extends Bounded> {
	private final RectF bounds;
	private final int maxObjects;
	private final QuadTree[] nodes = new QuadTree[4];
	private final ArrayList<T> objects;
	private final TreeMap<Double, Integer> map = new TreeMap<>();

	public QuadTree(int maxObjects) {
		this(maxObjects, new RectF());
	}

	private QuadTree(int maxObjects, RectF bounds) {
		this.bounds = bounds;
		this.maxObjects = maxObjects;
		this.objects = new ArrayList<>(maxObjects);
	}

	public void build(ArrayList<T> objects) {
		float left = 0;
		float top = 0;
		float right = 0;
		float bottom = 0;
		for (Bounded b : objects) {
			if (b.getBounds().left < left) {
				left = b.getBounds().left;
			}
			if (b.getBounds().left > right) {
				right = b.getBounds().right;
			}
			if (b.getBounds().top < top) {
				top = b.getBounds().top;
			}
			if (b.getBounds().left > right) {
				bottom = b.getBounds().bottom;
			}
		}
		float width = right - left;
		float height = bottom - top;
		if (width > height) {
			bounds.set(left, top, left + width, top + width);
		} else if (top > left) {
			bounds.set(left, top, left + height, top + height);
		}
		for (T b : objects) {
			if (b.getGameObject() == null || b.getGameObject().isStatic) {
				insert(b);
			}
		}
	}

	private void insert(T object) {
		if (isLeaf()) {
			objects.add(object);
			if (objects.size() > maxObjects) {
				split();
				loop:
				while (!objects.isEmpty()) {
					T item = objects.remove(0);
					for (QuadTree node : nodes) {
						if (node.bounds.contains(item.getBounds())) {
							node.insert(item);
							continue loop;
						}
					}
					for (QuadTree node : nodes) {
						if (node.bounds.intersects(item.getBounds())) {
							node.insert(item);
						}
					}
				}
			}
		} else {
			for (QuadTree node : nodes) {
				if (node.bounds.contains(object.getBounds())) {
					node.insert(object);
					return;
				}
			}
			for (QuadTree node : nodes) {
				if (node.bounds.intersects(object.getBounds())) {
					node.insert(object);
				}
			}
		}
	}

	private void split() {
		float subWidth = bounds.width() / 2f;
		float subHeight = bounds.height() / 2f;
		float x = bounds.left;
		float y = bounds.top;
		// top left
		nodes[0] = new QuadTree(maxObjects, new RectF(x, y, x + subWidth, y + subHeight));
		// top right
		nodes[1] = new QuadTree(maxObjects, new RectF(x + subWidth, y, x + (subWidth * 2), y + subHeight));
		// bottom left
		nodes[2] = new QuadTree(maxObjects, new RectF(x, y + subHeight, x + subWidth, y + (subHeight * 2)));
		// bottom right
		nodes[3] = new QuadTree(maxObjects,
				new RectF(x + subWidth, y + subHeight, x + (subWidth * 2), y + (subHeight * 2)));
	}

	private boolean isLeaf() {
		return nodes[0] == null;
	}

	public void retrieve(ArrayList<T> returnObjects, Bounded b) {
		retrieve(returnObjects, b.getBounds());
	}

	ArrayList<T> retrieve(ArrayList<T> returnObjects, RectF bounds) {
		if (!isLeaf()) {
			for (QuadTree qt : nodes) {
				if (qt.bounds.contains(bounds)) {
					return qt.retrieve(returnObjects, bounds);
				}
			}
			for (QuadTree qt : nodes) {
				if (RectF.intersects(qt.bounds, bounds)) {
					qt.retrieve(returnObjects, bounds);
				}
			}
			if (!returnObjects.isEmpty()) return returnObjects;
		}
		returnObjects.addAll(objects);
		return returnObjects;
	}

	static <T extends Bounded> RayCollision<T> getRayCollision(QuadTree<T> qt, Vector2 start, Vector2 end,
			int layerMask) {
		return getRayCollision(qt, start, end, layerMask, new RayCollision<>());
	}

	private static <T extends Bounded> RayCollision<T> getRayCollision(QuadTree<T> qt, Vector2 start, Vector2 end,
			int layerMask, RayCollision<T> collision) {
		if (qt.bounds.intersect(start, end) != null) {
			if (qt.isLeaf()) {
				Vector2 temp;
				double dist;
				for (T object : qt.objects) {
					temp = object.getBounds().intersect(start, end);
					if (temp != null) {
						dist = Vector2.distance(start, temp);
						if (dist < collision.distance && (object.getGameObject().getLayerMask() & layerMask) ==
								object.getGameObject().getLayerMask()) {
							collision.set(object, temp, dist);
						}
					}
				}
				return collision;
			} else {
				qt.map.clear();
				Vector2 intersect;
				for (int i = 0; i < qt.nodes.length; i++) {
					intersect = qt.nodes[i].bounds.intersect(start, end);
					qt.map.put(intersect == null ? Double.MAX_VALUE : Vector2.distance(start, intersect), i);
				}
				for (Double d : qt.map.keySet()) {
					if (d < collision.distance) {
						RayCollision<T> temp = getRayCollision(qt.nodes[qt.map.get(d)], start, end, layerMask,
								collision);
						collision.set(temp);
					}
				}
				return collision;
			}
		} else {
			return collision;
		}
	}

	static class RayCollision<T extends Bounded> {
		T bounded = null;
		Vector2 point = null;
		double distance = Double.MAX_VALUE;

		void set(RayCollision<T> collision) {
			bounded = collision.bounded;
			point = collision.point;
			distance = collision.distance;
		}

		void set(T bounded, Vector2 point, double distance) {
			this.bounded = bounded;
			this.point = point;
			this.distance = distance;
		}
	}
}
