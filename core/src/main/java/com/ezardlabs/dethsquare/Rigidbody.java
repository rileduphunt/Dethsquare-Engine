package com.ezardlabs.dethsquare;

public final class Rigidbody extends Script {
	public Vector2 velocity = new Vector2(0, 0.9375f);
	public float gravity = 1.25f;

	@Override
	public void update() {
		if (Time.frameCount > 5) {
			velocity.y += gravity * Time.fpsScaling60;
			if (velocity.y > 78.125f) velocity.y = 78.125f;

			transform.translate(velocity.x * Time.fpsScaling60, velocity.y * Time.fpsScaling60);
		}
	}
}
