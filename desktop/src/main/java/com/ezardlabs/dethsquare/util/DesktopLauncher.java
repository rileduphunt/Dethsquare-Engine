package com.ezardlabs.dethsquare.util;

import com.ezardlabs.dethsquare.util.Utils.Platform;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowSizeCallback;
import static org.lwjgl.glfw.GLFW.glfwShowWindow;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwSwapInterval;
import static org.lwjgl.glfw.GLFW.glfwWindowHint;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.system.MemoryUtil.NULL;

public class DesktopLauncher extends Launcher {
	private final long window;

	public DesktopLauncher() {
		glfwInit();

		glfwSetErrorCallback(
				(error, description) -> System.err.println(error + ": " + description));

		GLFWVidMode glfwVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

		glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);

		window = glfwCreateWindow(glfwVidMode.width(), glfwVidMode.height(), "Lost Sector", NULL,
				NULL);

		glfwSetWindowSizeCallback(window, (window, width, height) -> resizeListeners
				.forEach(resizeListener -> resizeListener.onResize(width, height)));

		glfwShowWindow(window);

		glfwMakeContextCurrent(window);

		GL.createCapabilities();

		glfwSwapInterval(1);

		glClearColor(0, 0, 0, 1);

		init();
	}

	@Override
	protected Platform getPlatform() {
		return Platform.DESKTOP;
	}

	@Override
	protected AudioUtils getAudio() {
		return new DesktopAudioUtils();
	}

	@Override
	protected IOUtils getIO() {
		return new DesktopIOUtils();
	}

	@Override
	protected PrefUtils getPrefs() {
		return new DesktopPrefUtils();
	}

	@Override
	protected RenderUtils getRender() {
		return new DesktopRenderUtils();
	}

	@Override
	public void launch(BaseGame game) {
		int[] width = new int[1];
		int[] height = new int[1];
		glfwGetWindowSize(window, width, height);
		onResize(width[0], height[0]);

		game.create();

		while (!glfwWindowShouldClose(window)) {
			glfwPollEvents();
			glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

			update();

			render();

			glfwSwapBuffers(window);
		}
	}
}
