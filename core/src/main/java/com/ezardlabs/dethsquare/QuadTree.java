package com.ezardlabs.dethsquare;

import java.util.ArrayList;
import java.util.TreeMap;

@SuppressWarnings("unchecked")
final class QuadTree<T extends Bounded> {
	private final int maxObjects;
	private RectF bounds;
	private ArrayList<T> objects = new ArrayList<>();
	private QuadTree[] nodes = new QuadTree[4];
	private TreeMap<Double, Integer> map = new TreeMap<>();

	public QuadTree(int maxObjects) {
		this.maxObjects = maxObjects;
	}

	QuadTree(int maxObjects, RectF bounds) {
		this(maxObjects);
		this.bounds = bounds;
	}

	public final void init(Bounded[] items) {
		float x = 0;
		float y = 0;
		for (Bounded b : items) {
			if (b.getGameObject() == null || b.getGameObject().isStatic && b.getBounds().right > x)
				x = b.getBounds().right;
			if (b.getGameObject() == null || b.getGameObject().isStatic && b.getBounds().bottom > y)
				y = b.getBounds().bottom;
		}
		if (x >= y) {
			bounds = new RectF(0, 0, (int) x, (int) x);
		} else if (y > x) {
			bounds = new RectF(0, 0, (int) y, (int) y);
		}
		for (Bounded b : items) {
			if (b.getGameObject() == null || b.getGameObject().isStatic) {
				insert(b);
			}
		}
		finalise(items);
	}

	static <T extends Bounded> void retrieve(ArrayList<T> returnObjects, QuadTree<T> qt, Bounded b) {
		retrieve(returnObjects, qt, b.getBounds());
	}

	static <T extends Bounded> ArrayList<T> retrieve(ArrayList<T> returnObjects, QuadTree<T> qt, RectF bounds) {
		if (!qt.isLeaf()) {
			for (QuadTree qt2 : qt.nodes) {
				if (qt2.bounds.contains(bounds)) {
					return retrieve(returnObjects, qt2, bounds);
				}
			}
			for (QuadTree qt2 : qt.nodes) {
				if (RectF.intersects(qt2.bounds, bounds)) {
					retrieve(returnObjects, qt2, bounds);
				}
			}
			if (!returnObjects.isEmpty()) return returnObjects;
		}
		returnObjects.addAll(qt.objects);
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

	private void finalise(Bounded[] items) {
		objects.clear();
		if (isLeaf()) {
			for (Bounded b : items) {
				if (bounds.contains(b.getBounds()) || RectF.intersects(bounds, b.getBounds())) {
					objects.add((T) b);
				}
			}
		} else {
			for (QuadTree qt2 : nodes) {
				qt2.finalise(items);
			}
		}
	}

	private void insert(Bounded b) {
		if (isLeaf()) {
			objects.add((T) b);
			if (objects.size() > maxObjects) {
				if (isLeaf()) split();
				outer:
				while (!objects.isEmpty()) {
					for (int j = 0; j < 4; j++) {
						if (nodes[j].bounds.contains(b.getBounds())) {
							nodes[j].insert(objects.remove(0));
							continue outer;
						}
					}
					Bounded b2 = objects.remove(0);
					for (int j = 0; j < 4; j++) {
						if (RectF.intersects(nodes[j].bounds, b2.getBounds())) nodes[j].insert(b2);
					}
				}
			}
		} else {
			for (int i = 0; i < 4; i++) {
				if (nodes[i].bounds.contains(b.getBounds())) {
					nodes[i].insert(b);
					return;
				}
			}
			for (int i = 0; i < 4; i++) {
				if (RectF.intersects(nodes[i].bounds, b.getBounds())) nodes[i].insert(b);
			}
		}
	}

	private void split() {
		float subWidth = bounds.width() / 2f;
		float subHeight = bounds.height() / 2f;
		float x = bounds.left;
		float y = bounds.top;
		nodes[0] = new QuadTree(maxObjects, new RectF(x, y, x + subWidth, y + subHeight)); // top left
		nodes[1] = new QuadTree(maxObjects, new RectF(x + subWidth, y, x + (subWidth * 2), y + subHeight)); // top right
		nodes[2] = new QuadTree(maxObjects,
				new RectF(x, y + subHeight, x + subWidth, y + (subHeight * 2))); // bottom left
		nodes[3] = new QuadTree(maxObjects,
				new RectF(x + subWidth, y + subHeight, x + (subWidth * 2), y + (subHeight * 2))); // bottom right
	}

	public boolean isLeaf() {
		return nodes[0] == null;
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
