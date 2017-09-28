package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.graphics.GraphicsEngine;

import java.util.HashMap;

public class LevelManager {
	private static HashMap<String, Level> levels = new HashMap<>();
	private static String currentLevelName;
	private static boolean loadingLevel = false;

	public static void registerLevel(String name, Level level) {
		levels.put(name, level);
	}

	public static void loadLevel(String name) {
		if (loadingLevel) {
			throw new IllegalStateException("Cannot load a level whilst another level is being loaded");
		}
		Time.pause();
		loadingLevel = true;
		currentLevelName = name;

		GameObject.destroyAll();

		Level level = levels.get(name);
		level.onLoad();

		GameObject.startAll();
		GraphicsEngine.init();
		Collider.init();
		loadingLevel = false;

		Input.clearAll();
		Time.resume();
	}

	public static Level getCurrentLevel() {
		return levels.get(currentLevelName);
	}

	public static String getCurrentLevelName() {
		return currentLevelName;
	}
}
