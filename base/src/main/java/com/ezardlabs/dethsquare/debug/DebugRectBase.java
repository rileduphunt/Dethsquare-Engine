package com.ezardlabs.dethsquare.debug;

abstract class DebugRectBase extends DebugGraphic {
	protected final float x;
	protected final float y;
	protected final float width;
	protected final float height;

	DebugRectBase(float x, float y, float width, float height, float red, float green, float blue) {
		super(red, green, blue);
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
}
