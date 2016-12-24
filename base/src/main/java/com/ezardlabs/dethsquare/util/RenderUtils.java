package com.ezardlabs.dethsquare.util;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;

public interface RenderUtils {
	int[] loadImage(String path);

	void render(int textureName, FloatBuffer vertexBuffer, FloatBuffer uvBuffer, int numIndices, ShortBuffer indexBuffer, float cameraPosX, float cameraPosY, float scale);

	void destroyAllTextures(HashMap<String, int[]> textures);

	class ImageNotFoundError extends Error {

		ImageNotFoundError(String path) {
			super("Image at " + path + " could not be found");
		}
	}
}
