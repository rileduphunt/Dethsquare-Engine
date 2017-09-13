package com.ezardlabs.dethsquare.debug;

import org.lwjgl.opengl.GL11;

public class DebugRect extends DebugRectBase {

	DebugRect(float x, float y, float width, float height, float red, float green, float blue) {
		super(x, y, width, height, red, green, blue);
	}

	@Override
	public void draw(float cameraX, float cameraY, float scale) {
		GL11.glLineWidth(3);
		GL11.glBegin(GL11.GL_LINE_LOOP);
		GL11.glColor3f(red, green, blue);
		GL11.glVertex3f((x - cameraX) * scale, (y - cameraY) * scale, 9999999);
		GL11.glVertex3f((x + width - cameraX) * scale, (y - cameraY) * scale, 9999999);
		GL11.glVertex3f((x + width - cameraX) * scale, (y + height - cameraY) * scale, 9999999);
		GL11.glVertex3f((x - cameraX) * scale, (y + height - cameraY) * scale, 9999999);
		GL11.glEnd();
	}
}
