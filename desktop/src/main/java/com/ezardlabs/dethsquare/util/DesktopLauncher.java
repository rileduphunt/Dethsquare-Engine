package com.ezardlabs.dethsquare.util;

import com.ezardlabs.dethsquare.util.Dethsquare.Platform;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;

import java.util.HashMap;

import static org.lwjgl.glfw.GLFW.GLFW_DECORATED;
import static org.lwjgl.glfw.GLFW.GLFW_FALSE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_0;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_3;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_4;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_5;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_6;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_7;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_8;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_9;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_C;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_E;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ENTER;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F1;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F10;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F12;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F2;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F3;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F4;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F5;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F6;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F7;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F8;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_F9;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_G;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_H;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_I;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_J;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_K;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_L;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_M;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_N;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_O;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_P;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Q;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_R;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_T;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_U;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Y;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_LEFT;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_MIDDLE;
import static org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_RIGHT;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE;
import static org.lwjgl.glfw.GLFW.glfwCreateWindow;
import static org.lwjgl.glfw.GLFW.glfwGetPrimaryMonitor;
import static org.lwjgl.glfw.GLFW.glfwGetVideoMode;
import static org.lwjgl.glfw.GLFW.glfwGetWindowSize;
import static org.lwjgl.glfw.GLFW.glfwInit;
import static org.lwjgl.glfw.GLFW.glfwMakeContextCurrent;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetErrorCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback;
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
	private final HashMap<Integer, String> keyMap = new HashMap<>();
	private boolean setupCompleted = false;

	public DesktopLauncher() {
		glfwInit();

		glfwSetErrorCallback(
				(error, description) -> System.err.println(error + ": " + description));

		GLFWVidMode glfwVidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

		glfwWindowHint(GLFW_DECORATED, GLFW_FALSE);

		window = glfwCreateWindow(glfwVidMode.width(), glfwVidMode.height(), "Lost Sector", NULL,
				NULL);

		initKeyMap();

		glfwSetKeyCallback(window, (window, key, scanCode, action, mods) -> {
			String keyStr = keyMap.get(key);
			if (keyStr != null) {
				if (action == GLFW_PRESS) {
					keyListeners.forEach(keyListener -> keyListener.onKeyDown(keyStr));
				}
				if (action == GLFW_RELEASE) {
					keyListeners.forEach(keyListener -> keyListener.onKeyUp(keyStr));
				}
			}
		});

		glfwSetCursorPosCallback(window, (window, xPos, yPos) -> {
			mouseListeners.forEach(mouseListener -> mouseListener.onMove((int) xPos, (int) yPos));
		});

		glfwSetMouseButtonCallback(window, (window, button, action, mods) -> {
			int index = getMouseButtonIndex(button);
			if (index != 0) {
				if (action == GLFW_PRESS) {
					mouseListeners.forEach(mouseListener -> mouseListener.onButtonDown(index));
				}
				if (action == GLFW_RELEASE) {
					mouseListeners.forEach(mouseListener -> mouseListener.onButtonUp(index));
				}
			}
		});

		glfwSetWindowSizeCallback(window, (window, width, height) -> resizeListeners
				.forEach(resizeListener -> resizeListener.onResize(width, height)));

		glfwShowWindow(window);

		glfwMakeContextCurrent(window);

		GL.createCapabilities();

		glfwSwapInterval(1);

		glClearColor(0, 0, 0, 1);

		init();

		setupCompleted = true;
	}

	private void initKeyMap() {
		addLetterKeys();
		addNumberKeys();
		addFunctionKeys();
		addOtherKeys();
	}

	private void addLetterKeys() {
		keyMap.put(GLFW_KEY_A, "A");
		keyMap.put(GLFW_KEY_B, "B");
		keyMap.put(GLFW_KEY_C, "C");
		keyMap.put(GLFW_KEY_D, "D");
		keyMap.put(GLFW_KEY_E, "E");
		keyMap.put(GLFW_KEY_F, "F");
		keyMap.put(GLFW_KEY_G, "G");
		keyMap.put(GLFW_KEY_H, "H");
		keyMap.put(GLFW_KEY_I, "I");
		keyMap.put(GLFW_KEY_J, "J");
		keyMap.put(GLFW_KEY_K, "K");
		keyMap.put(GLFW_KEY_L, "L");
		keyMap.put(GLFW_KEY_M, "M");
		keyMap.put(GLFW_KEY_N, "N");
		keyMap.put(GLFW_KEY_O, "O");
		keyMap.put(GLFW_KEY_P, "P");
		keyMap.put(GLFW_KEY_Q, "Q");
		keyMap.put(GLFW_KEY_R, "R");
		keyMap.put(GLFW_KEY_S, "S");
		keyMap.put(GLFW_KEY_T, "T");
		keyMap.put(GLFW_KEY_U, "U");
		keyMap.put(GLFW_KEY_V, "V");
		keyMap.put(GLFW_KEY_W, "W");
		keyMap.put(GLFW_KEY_X, "X");
		keyMap.put(GLFW_KEY_Y, "Y");
		keyMap.put(GLFW_KEY_Z, "Z");
	}

	private void addNumberKeys() {
		keyMap.put(GLFW_KEY_0, "ALPHA_0");
		keyMap.put(GLFW_KEY_1, "ALPHA_1");
		keyMap.put(GLFW_KEY_2, "ALPHA_2");
		keyMap.put(GLFW_KEY_3, "ALPHA_3");
		keyMap.put(GLFW_KEY_4, "ALPHA_4");
		keyMap.put(GLFW_KEY_5, "ALPHA_5");
		keyMap.put(GLFW_KEY_6, "ALPHA_6");
		keyMap.put(GLFW_KEY_7, "ALPHA_7");
		keyMap.put(GLFW_KEY_8, "ALPHA_8");
		keyMap.put(GLFW_KEY_9, "ALPHA_9");
	}

	private void addFunctionKeys() {
		keyMap.put(GLFW_KEY_F1, "F1");
		keyMap.put(GLFW_KEY_F2, "F2");
		keyMap.put(GLFW_KEY_F3, "F3");
		keyMap.put(GLFW_KEY_F4, "F4");
		keyMap.put(GLFW_KEY_F5, "F5");
		keyMap.put(GLFW_KEY_F6, "F6");
		keyMap.put(GLFW_KEY_F7, "F7");
		keyMap.put(GLFW_KEY_F8, "F8");
		keyMap.put(GLFW_KEY_F9, "F9");
		keyMap.put(GLFW_KEY_F10, "F10");
		keyMap.put(GLFW_KEY_F11, "F11");
		keyMap.put(GLFW_KEY_F12, "F12");
	}

	private void addOtherKeys() {
		keyMap.put(GLFW_KEY_SPACE, "SPACE");
		keyMap.put(GLFW_KEY_ENTER, "ENTER");
		keyMap.put(GLFW_KEY_ESCAPE, "ESCAPE");
		keyMap.put(GLFW_KEY_BACKSPACE, "BACKSPACE");
		keyMap.put(GLFW_KEY_DELETE, "DELETE");
	}

	private int getMouseButtonIndex(int button) {
		switch (button) {
			case GLFW_MOUSE_BUTTON_LEFT:
				return 1;
			case GLFW_MOUSE_BUTTON_MIDDLE:
				return 2;
			case GLFW_MOUSE_BUTTON_RIGHT:
				return 3;
			default:
				return 0;
		}
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
		while (!setupCompleted) {}
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
