package com.ezardlabs.dethsquare.util;

import com.ezardlabs.dethsquare.debug.DebugGraphic;

import java.util.ArrayList;

public interface RenderUtils {
	int[] loadImage(String path);

	void render(int textureName, float[] vertices, float[] uvs, short[] indices, float[] colours, int num);

	void render(ArrayList<DebugGraphic> debugGraphics);

	void destroyAllTextures();

	void setCameraPosition(float x, float y);

	void setScale(float scale);

	void setScreenSize(int width, int height);

	void setGuiRenderMode(boolean guiRenderMode);

	class ImageNotFoundError extends Error {

		ImageNotFoundError(String path) {
			super("Image at " + path + " could not be found");
		}
	}
}
