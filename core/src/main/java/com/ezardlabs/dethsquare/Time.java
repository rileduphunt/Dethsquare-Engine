package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.util.GameListeners;
import com.ezardlabs.dethsquare.util.GameListeners.UpdateListener;

public final class Time {
	static {
		// Add hook into game loop
		GameListeners.addUpdateListener(new UpdateListener() {
			long last = System.currentTimeMillis();

			@Override
			public void onUpdate() {
				long now = System.currentTimeMillis();
				deltaTime = now - last - pauseTime;
				pauseTime = 0;
				fpsScaling60 = deltaTime / frameTime60fps;
				last = now;
				frameCount++;
			}
		});
	}

	/**
	 * Amount of time that a single frame of a game running at 60fps should take
	 */
	private static final float frameTime60fps = 1000f / 60f;
	/**
	 * Whether or not the game is paused
	 */
	private static boolean paused = false;
	/**
	 * The time at which the game was paused
	 */
	private static long pauseStartTime = 0;
	/**
	 * The amount of time that the game has been paused for
	 */
	private static long pauseTime = 0;
	/**
	 * The time in seconds it took to complete the last frame
	 */
	public static float deltaTime = frameTime60fps;
	/**
	 * Multiply movement quantities by this amount so that it scales with a variable frame rate
	 */
	public static float fpsScaling60 = deltaTime / frameTime60fps;
	/**
	 * The total number of frames that have passed
	 */
	public static long frameCount;
	/**
	 * The time at the beginning of this frame. This is the time in seconds since the start of the game
	 */
	public static float time;

	/**
	 * Pause the game
	 */
	public static void pause() {
		paused = true;
		pauseStartTime = System.currentTimeMillis();
	}

	/**
	 * Resume the game
	 */
	public static void resume() {
		paused = false;
		pauseTime = System.currentTimeMillis() - pauseStartTime;
	}

	/**
	 * Returns whether or not the game is currently paused
	 *
	 * @return whether or not the game is currently paused
	 */
	public static boolean isPaused() {
		return paused;
	}
}
