package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.graphics.GraphicsEngine;
import com.ezardlabs.dethsquare.util.GameListeners;

import java.util.HashMap;

public class LevelManager {
	static {
		GameListeners.addPostUpdateListener(LevelManager::onPostUpdate);
	}

	private static HashMap<String, Level> levels = new HashMap<>();
	private static String currentLevelName;
	private static String nextLevelName;

	public static void registerLevel(String name, Level level) {
		levels.put(name, level);
	}

	public static void loadLevel(String name) {
		if (nextLevelName != null) {
			throw new IllegalStateException("Cannot load a level whilst another level is being loaded");
		}
		nextLevelName = name;
	}

	private static void onPostUpdate() {
		if (nextLevelName != null) {
			Time.pause();
			GameObject.destroyAll();

			currentLevelName = nextLevelName;
			Level level = levels.get(nextLevelName);
			level.onLoad();

			GameObject.startAll();
			GraphicsEngine.init();
			Collider.init();

			Input.clearAll();

			nextLevelName = null;

			Time.resume();
		}
	}

	public static Level getCurrentLevel() {
		return levels.get(currentLevelName);
	}

	public static String getCurrentLevelName() {
		return currentLevelName;
	}
}
