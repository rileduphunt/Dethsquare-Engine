package com.ezardlabs.dethsquare.debug;

import org.lwjgl.opengl.GL11;

class DebugCircle extends DebugCircleBase {

	DebugCircle(float x, float y, float radius, float red, float green, float blue) {
		super(x, y, radius, red, green, blue);
	}

	@Override
	public void draw(float cameraX, float cameraY, float scale) {
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glLineWidth(3);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glColor3f(red, green, blue);
		for (int i = 0; i < 20; i++) {
			float theta = 2f * 3.1415926f * (float) i / 20f;
			float x = (float) (radius * Math.cos(theta) * scale);
			float y = (float) (radius * Math.sin(theta) * scale);
			GL11.glVertex3f(this.x + x - cameraX, this.y + y - cameraY, 9999999);
		}
		GL11.glEnd();
	}
}
