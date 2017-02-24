package com.ezardlabs.dethsquare.util;

import org.lwjgl.BufferUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;

import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_RGB;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDeleteTextures;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;
import static org.lwjgl.opengl.GL14.glBlendFuncSeparate;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetAttribLocation;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class DesktopRenderUtils implements RenderUtils {
	private final HashMap<String, int[]> textures = new HashMap<>();

	private int program;
	private int positionLoc;
	private int transformLoc;
	private int texCoordsLoc;
	private int vertexArray;
	private int vertexBuffer;
	private int indexBuffer;
	private int texCoordBuffer;

	private final float[] projection = new float[16];
	private final float[] view = new float[16];
	private final float[] projectionAndView = new float[16];

	private float cameraX = 0;
	private float cameraY = 0;
	private float scale = 1;
	private int screenWidth = 1920;
	private int screenHeight = 1080;

	private boolean initialised = false;

	public int[] loadImage(String path) {
		if (textures.containsKey(path)) {
			return textures.get(path);
		}

		int[] returnVals = new int[3];

		String normalisedPath;
		try {
			normalisedPath = new URI(path).normalize().getPath();
		} catch (URISyntaxException e) {
			e.printStackTrace();
			normalisedPath = path;
		}
		InputStream in = ClassLoader.getSystemResourceAsStream(normalisedPath);
		if (in == null) {
			throw new ImageNotFoundError(normalisedPath);
		}

		int texture = glGenTextures();

		try {
			byte[] bytes = org.apache.commons.io.IOUtils
					.toByteArray(ClassLoader.getSystemResourceAsStream(path));
			ByteBuffer nativeData = ByteBuffer.allocateDirect(bytes.length);
			nativeData.put(bytes);
			nativeData.flip();

			IntBuffer width = BufferUtils.createIntBuffer(1);
			IntBuffer height = BufferUtils.createIntBuffer(1);
			IntBuffer components = BufferUtils.createIntBuffer(1);

			ByteBuffer imageBuffer = stbi_load_from_memory(nativeData, width, height, components,
					4);

			glBindTexture(GL_TEXTURE_2D, texture);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0,
					components.get() == 4 ? GL_RGBA : GL_RGB, GL_UNSIGNED_BYTE, imageBuffer);

			glActiveTexture(GL_TEXTURE0);

			glUniform1i(glGetAttribLocation(program, "texUnit"), 0);

			stbi_image_free(imageBuffer);

			returnVals[0] = texture;
			returnVals[1] = width.get(0);
			returnVals[2] = height.get(0);
			textures.put(path, returnVals);

			return returnVals;
		} catch (IOException e) {
			e.printStackTrace();
			return new int[3];
		}
	}

	public void render(int textureName, float[] vertices, float[] uvs, short[] indices, int num) {
		if (!initialised) {
			glEnable(GL_BLEND);
			glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);

			String[] lines = Dethsquare.IO.getFileLines("shaders/texture/vert.glsl");
			String shader = "";
			for (String s : lines) {
				shader += s + "\n";
			}

			int vs = glCreateShader(GL_VERTEX_SHADER);
			glShaderSource(vs, shader);
			glCompileShader(vs);

			lines = Dethsquare.IO.getFileLines("shaders/texture/frag.glsl");
			shader = "";
			for (String s : lines) {
				shader += s + "\n";
			}

			int fs = glCreateShader(GL_FRAGMENT_SHADER);
			glShaderSource(fs, shader);
			glCompileShader(fs);

			program = glCreateProgram();
			glAttachShader(program, vs);
			glAttachShader(program, fs);
			glLinkProgram(program);

			glUseProgram(program);

			positionLoc = glGetAttribLocation(program, "position");
			transformLoc = glGetUniformLocation(program, "transform");
			texCoordsLoc = glGetAttribLocation(program, "texCoords");

			vertexArray = glGenVertexArrays();
			vertexBuffer = glGenBuffers();
			texCoordBuffer = glGenBuffers();
			glBindVertexArray(vertexArray);

			indexBuffer = glGenBuffers();

			initialised = true;
		}

		glBindTexture(GL_TEXTURE_2D, textureName);

		glBindBuffer(GL_ARRAY_BUFFER, vertexBuffer);
		glBufferData(GL_ARRAY_BUFFER, vertices, GL_DYNAMIC_DRAW);
		glVertexAttribPointer(positionLoc, 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(positionLoc);

		glBindBuffer(GL_ARRAY_BUFFER, texCoordBuffer);
		glBufferData(GL_ARRAY_BUFFER, uvs, GL_DYNAMIC_DRAW);
		glVertexAttribPointer(texCoordsLoc, 2, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(texCoordsLoc);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_DYNAMIC_DRAW);

		glDrawElements(GL_TRIANGLES, num * 6, GL_UNSIGNED_SHORT, 0);
	}

	public void destroyAllTextures() {
		for (int[] data : textures.values()) {
			glDeleteTextures(data[0]);
		}
		textures.clear();
	}

	@Override
	public void setCameraPosition(float x, float y) {
		this.cameraX = x;
		this.cameraY = y;

		calculateMatrices();
	}

	@Override
	public void setScale(float scale) {
		this.scale = scale;

		calculateMatrices();
	}

	private void calculateMatrices() {
		Matrix.clear(projection);
		Matrix.clear(view);
		Matrix.clear(projectionAndView);

		Matrix.orthoM(projection, 0, screenWidth, screenHeight, 0, 0, 50);

		Matrix.setLookAtM(view, 0, 0, 1, 0, 0, 0, 0, 1, 0);
		Matrix.translateM(view, -cameraX, -cameraY, 0);

		Matrix.multiplyMM(projectionAndView, projection, view);

		glUniformMatrix4fv(transformLoc, false, projectionAndView);
	}

	@Override
	public void setScreenSize(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;

		glViewport(0, 0, width, height);

		calculateMatrices();
	}
}
