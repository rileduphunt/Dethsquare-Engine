package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.QuadTree.RayCollision;

import java.util.Arrays;
import java.util.HashSet;
import java.util.stream.Stream;

public class Physics {

	public static RaycastHit raycast(Vector2 origin, Vector2 direction, float distance, String... targetTags) {
		HashSet<String> tags = new HashSet<>();
		tags.addAll(Arrays.asList(targetTags));
		direction.normalise();
		Vector2 end = new Vector2(origin.x + direction.x * distance, origin.y + direction.y * distance);

		RayCollision<Collider> collision = QuadTree.getRayCollision(Collider.qt, origin, end, targetTags);
		Stream.concat(Collider.normalColliders.stream(), Collider.triggerColliders.stream())
			  .parallel()
			  .filter(collider -> tags.size() == 0 || tags.contains(collider.gameObject.getTag()))
			  .forEach(collider -> {
				  Vector2 intersect = collider.bounds.intersect(origin, end);
				  if (intersect != null) {
					  double dist = Vector2.distance(origin, intersect);
					  if (dist < collision.distance) {
						  collision.set(collider, intersect, dist);
					  }
				  }
			  });
		if (collision.distance != Double.MAX_VALUE) {
			return new RaycastHit(collision.point, collision.distance, collision.boundedComponent.transform,
					collision.boundedComponent.gameObject.collider);
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
