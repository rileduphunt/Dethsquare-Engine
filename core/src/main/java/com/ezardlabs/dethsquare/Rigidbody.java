package com.ezardlabs.dethsquare;

public final class Rigidbody extends Script {
	private static final float MAX_MOVEMENT = 50; // TODO move into Collider/Physics
	public Vector2 velocity = new Vector2(0, 0.9375f);
	public float gravity = 1.25f;

	@Override
	public void update() {
		if (Time.frameCount > 5) {
			velocity.y += gravity * Time.fpsScaling60;
			if (velocity.y > 78.125f) velocity.y = 78.125f;

			float x = velocity.x * Time.fpsScaling60;
			float y = velocity.y * Time.fpsScaling60;
			float xAbs = Math.abs(velocity.x * Time.fpsScaling60);
			float yAbs = Math.abs(velocity.y * Time.fpsScaling60);
			if (xAbs > yAbs && xAbs > MAX_MOVEMENT) {
				int iterations = (int) (x / MAX_MOVEMENT);
				float ratio = MAX_MOVEMENT / x;
				for (int i = 0; i < iterations; i++) {
					transform.translate(x * ratio, y * ratio);
				}
				ratio = (x % MAX_MOVEMENT) / x;
				transform.translate(x * ratio, y * ratio);
			} else if (yAbs > xAbs && yAbs > MAX_MOVEMENT) {
				int iterations = (int) (y / MAX_MOVEMENT);
				float ratio = MAX_MOVEMENT / y;
				for (int i = 0; i < iterations; i++) {
					transform.translate(x * ratio, y * ratio);
				}
				ratio = (y % MAX_MOVEMENT) / y;
				transform.translate(x * ratio, y * ratio);
			} else {
				transform.translate(x, y);
			}
		}
	}
}
