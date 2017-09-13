package com.ezardlabs.dethsquare.debug;

import com.ezardlabs.dethsquare.Vector2;
import com.ezardlabs.dethsquare.util.Dethsquare;
import com.ezardlabs.dethsquare.util.GameListeners;

import java.util.ArrayList;

public class Debug {
	static {
		GameListeners.addRenderListener(Debug::renderAll);
	}
	private static ArrayList<DebugGraphic> debugGraphics = new ArrayList<>();

	public static void drawCircle(Vector2 position, float radius, float red, float green, float blue) {
		debugGraphics.add(new DebugCircle(position.x, position.y, radius, red, green, blue));
	}

	public static void drawRect(Vector2 position, float width, float height, float red, float green, float blue) {
		debugGraphics.add(new DebugRect(position.x, position.y, width, height, red, green, blue));
	}

	private static void renderAll() {
		Dethsquare.RENDER.render(debugGraphics);
		debugGraphics.clear();
	}
}
