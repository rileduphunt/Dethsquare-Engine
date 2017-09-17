package com.ezardlabs.dethsquare.util;

import com.ezardlabs.dethsquare.debug.DebugGraphic;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
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
import static org.lwjgl.opengl.GL20.glUniform4f;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class DesktopRenderUtils implements RenderUtils {
	private final HashMap<String, int[]> textures = new HashMap<>();
	private final HashMap<String, Integer> programCache = new HashMap<>();
	private final HashMap<String, Integer> shaderCache = new HashMap<>();

	private int program = -1;
	private int positionLoc;
	private int transformLoc;
	private int texCoordsLoc;
	private int colourLoc;
	private int vertexArray;
	private int vertexBuffer;
	private int indexBuffer;
	private int texCoordBuffer;
	private int colourBuffer;

	private int program2 = -1;
	private int colour2Loc;

	private final float[] projection = new float[16];
	private final float[] view = new float[16];
	private final float[] projectionAndView = new float[16];

	private final float[] guiView = new float[16];
	private final float[] guiProjectionAndView = new float[16];

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

			if (program == -1) {
				program = loadShaderProgram("shaders/texture");
			}

			glBindTexture(GL_TEXTURE_2D, texture);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
			glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
			glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width.get(), height.get(), 0,
					components.get() == 4 ? GL_RGBA : GL_RGB, GL_UNSIGNED_BYTE, imageBuffer);

			glActiveTexture(GL_TEXTURE0);

			glUseProgram(program);

			glUniform1i(glGetUniformLocation(program, "texUnit"), 0);

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

	@Override
	public int loadShaderProgram(String vertexPath, String fragmentPath) {
		String key = vertexPath + fragmentPath;
		int program;
		if (programCache.containsKey(key)) {
			program =  programCache.get(key);
		} else {
			int vert = loadShader(vertexPath, GL_VERTEX_SHADER);
			int frag = loadShader(fragmentPath, GL_FRAGMENT_SHADER);

			program = glCreateProgram();
			glAttachShader(program, vert);
			glAttachShader(program, frag);
			glLinkProgram(program);

			programCache.put(key, program);
		}
		glUseProgram(program);
		return program;
	}

	@Override
	public int loadShaderProgram(String path) {
		return loadShaderProgram(path + File.separator + "vert.glsl", path + File.separator + "frag.glsl");
	}

	private int loadShader(String path, int type) {
		String key = path + "||" + type;
		if (shaderCache.containsKey(key)) {
			return shaderCache.get(key);
		} else {
			String[] lines = Dethsquare.IO.getFileLines(path);
			StringBuilder sb = new StringBuilder();
			for (String s : lines) {
				sb.append(s).append("\n");
			}

			int shader = glCreateShader(type);
			glShaderSource(shader, sb.toString());
			glCompileShader(shader);

			shaderCache.put(key, shader);

			return shader;
		}
	}

	public void render(int textureName, float[] vertices, float[] uvs, short[] indices, float[] colours, int num) {
		if (!initialised) {
			glEnable(GL_BLEND);
			glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE);

			program = loadShaderProgram("shaders/texture");
			glUseProgram(program);

			positionLoc = glGetAttribLocation(program, "position");
			transformLoc = glGetUniformLocation(program, "transform");
			texCoordsLoc = glGetAttribLocation(program, "texCoords");
			colourLoc = glGetAttribLocation(program, "colour");

			vertexArray = glGenVertexArrays();
			vertexBuffer = glGenBuffers();
			texCoordBuffer = glGenBuffers();
			colourBuffer = glGenBuffers();
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

		glBindBuffer(GL_ARRAY_BUFFER, colourBuffer);
		glBufferData(GL_ARRAY_BUFFER, colours, GL_DYNAMIC_DRAW);
		glVertexAttribPointer(colourLoc, 3, GL_FLOAT, false, 0, 0);
		glEnableVertexAttribArray(colourLoc);

		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, indexBuffer);
		glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_DYNAMIC_DRAW);

		glDrawElements(GL_TRIANGLES, num * 6, GL_UNSIGNED_SHORT, 0);
	}

	public void render(ArrayList<DebugGraphic> debugGraphics) {
		if (program2 == -1) {
			program2 = glCreateProgram();
			String[] lines = Dethsquare.IO.getFileLines("shaders/plain/vert.glsl");
			StringBuilder shader = new StringBuilder();
			for (String s : lines) {
				shader.append(s).append("\n");
			}

			int vs = glCreateShader(GL_VERTEX_SHADER);
			glShaderSource(vs, shader.toString());
			glCompileShader(vs);

			lines = Dethsquare.IO.getFileLines("shaders/plain/frag.glsl");
			shader = new StringBuilder();
			for (String s : lines) {
				shader.append(s).append("\n");
			}

			int fs = glCreateShader(GL_FRAGMENT_SHADER);
			glShaderSource(fs, shader.toString());
			glCompileShader(fs);

			glAttachShader(program2, vs);
			glAttachShader(program2, fs);
			glLinkProgram(program2);

			colour2Loc = glGetUniformLocation(program2, "colour");
		}
		glUseProgram(program2);
		GL11.glPushMatrix();
		for (DebugGraphic dg : debugGraphics) {
			glUniform4f(colour2Loc, dg.red, dg.green, dg.blue, 1);
			dg.draw(cameraX, cameraY, scale);
		}
		GL11.glPopMatrix();
		glUseProgram(program);
	}

	public void destroyAllTextures() {
		for (int[] data : textures.values()) {
			glDeleteTextures(data[0]);
		}
		textures.clear();
	}

	@Override
	public void setCameraPosition(float x, float y) {
		this.cameraX = Math.round(x);
		this.cameraY = Math.round(y);

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
		System.arraycopy(view, 0, guiView, 0, view.length);
		Matrix.translateM(view, -cameraX, -cameraY, 0);

		Matrix.multiplyMM(projectionAndView, projection, view);
		Matrix.multiplyMM(guiProjectionAndView, projection, guiView);

		glUniformMatrix4fv(transformLoc, false, projectionAndView);
	}

	@Override
	public void setScreenSize(int width, int height) {
		this.screenWidth = width;
		this.screenHeight = height;

		glViewport(0, 0, width, height);

		calculateMatrices();
	}

	@Override
	public void setGuiRenderMode(boolean guiRenderMode) {
		glUniformMatrix4fv(transformLoc, false, guiRenderMode ? guiProjectionAndView : projectionAndView);
	}
}
