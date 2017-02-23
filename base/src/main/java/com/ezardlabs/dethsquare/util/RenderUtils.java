package com.ezardlabs.dethsquare.util;

import java.util.HashMap;

public interface RenderUtils {
	int[] loadImage(String path);

	void render(int textureName, float[] vertices, float[] uvs, short[] indices, int num);

	void destroyAllTextures(HashMap<String, int[]> textures);

	void setCameraPosition(float x, float y);

	void setScale(float scale);

	void setScreenSize(int width, int height);

	class ImageNotFoundError extends Error {

		ImageNotFoundError(String path) {
			super("Image at " + path + " could not be found");
		}
	}
}
