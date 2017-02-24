package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.util.GameListeners;
import com.ezardlabs.dethsquare.util.Dethsquare;

public final class Screen {
	/**
	 * The current width of the screen
	 */
	public static int width = 0;
	/**
	 * The current height of the screen
	 */
	public static int height = 0;
	/**
	 * The current scaling to apply to objects
	 */
	public static float scale = 1;

	// Listen for resize events
	static {
		GameListeners.addResizeListener((width, height) -> {
			Screen.scale = (float) width / 1920f;
			Screen.width = width;
			Screen.height = height;
			Dethsquare.RENDER.setScreenSize(Screen.width, Screen.height);
			Dethsquare.RENDER.setScale(Screen.scale);
		});
		Screen.width = GameListeners.screenSize.width;
		Screen.height = GameListeners.screenSize.height;
		Screen.scale = (float) Screen.width / 1920f;
		Dethsquare.RENDER.setScreenSize(Screen.width, Screen.height);
		Dethsquare.RENDER.setScale(Screen.scale);
	}
}
