package com.ezardlabs.dethsquare.debug;

abstract class DebugCircleBase extends DebugGraphic {
	protected float x;
	protected float y;
	protected float radius;

	DebugCircleBase(float x, float y, float radius, float red, float green, float blue) {
		super(red, green, blue);
		this.x = x;
		this.y =y ;
		this.radius = radius;
	}
}
