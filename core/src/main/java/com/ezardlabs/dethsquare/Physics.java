package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.QuadTree.RayCollision;

import java.util.HashSet;
import java.util.stream.Stream;

public class Physics {

	public static RaycastHit raycast(Vector2 origin, Vector2 direction, float distance, int layerMask) {
		direction.normalise();
		Vector2 end = new Vector2(origin.x + direction.x * distance, origin.y + direction.y * distance);
		RaycastHit raycastHit = null;
		HashSet<Collider> temp = new HashSet<>(
				Collider.staticColliders.size() + Collider.normalColliders.size() + Collider.triggerColliders.size());
		temp.addAll(Collider.staticColliders);
		temp.addAll(Collider.normalColliders);
		temp.addAll(Collider.triggerColliders);
		for (Collider c : temp) {
			if ((c.gameObject.getLayerMask() & layerMask) == c.gameObject.getLayerMask()) {
				Vector2 hit = c.getBounds().intersect(origin, end);
				if (hit != null) {
					if (raycastHit == null) {
						raycastHit = new RaycastHit(hit, Vector2.distance(origin, hit), c.transform, c);
					} else if (Vector2.distance(origin, hit) < raycastHit.distance) {
						raycastHit.point = hit;
						raycastHit.distance = Vector2.distance(origin, hit);
						raycastHit.transform = c.transform;
						raycastHit.collider = c;
					}
				}
			}
		}
		return raycastHit;
	}

	public static RaycastHit raycastOptimised(Vector2 origin, Vector2 direction, float distance, int layerMask) {
		direction.normalise();
		Vector2 end = new Vector2(origin.x + direction.x * distance, origin.y + direction.y * distance);

		final RayCollision<Collider> collision = QuadTree.getRayCollision(Collider.qt, origin, end, layerMask);
		System.out.println("Halfway: " + collision.point);
		Stream.concat(Collider.normalColliders.stream(), Collider.triggerColliders.stream())
			  .parallel()
			  .filter(collider -> (collider.gameObject.getLayerMask() & layerMask) == collider.gameObject.getLayerMask())
			  .forEach(collider -> {
				  Vector2 intersect = collider.getBounds().intersect(origin, end);
				  if (intersect != null) {
					  double dist = Vector2.distance(origin, intersect);
					  if (dist < collision.distance) {
						  System.out.println(collider.gameObject.name);
						  collision.set(collider, intersect, dist);
					  }
				  }
			  });
		if (collision.distance != Double.MAX_VALUE) {
			return new RaycastHit(collision.point, collision.distance, collision.bounded.transform,
					collision.bounded.gameObject.collider);
		} else {
			return null;
		}
	}

	public static class RaycastHit {
		public Collider collider;
		public double distance;
		public Vector2 point;
		public Transform transform;

		RaycastHit(Vector2 point, double distance, Transform transform, Collider collider) {
			this.point = point;
			this.distance = distance;
			this.transform = transform;
			this.collider = collider;
		}
	}
}
