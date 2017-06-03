package com.ezardlabs.dethsquare.debug;

public abstract class DebugGraphic {
	public final float red;
	public final float green;
	public final float blue;

	DebugGraphic(float red, float green, float blue) {
		this.red = red;
		this.green = green;
		this.blue = blue;
	}

	public abstract void draw(float cameraX, float cameraY, float scale);
}
