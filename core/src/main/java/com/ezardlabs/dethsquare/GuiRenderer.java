package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.graphics.Renderer;

public class GuiRenderer extends Renderer {

	public GuiRenderer(String imagePath, float width, float height) {
		super(imagePath, width, height);
	}

	public GuiRenderer(TextureAtlas textureAtlas, TextureAtlas.Sprite sprite, float width, float height) {
		super(textureAtlas, sprite, width, height);
	}

	public boolean hitTest(float x, float y) {
		return getBounds().contains(x, y);
	}

	public boolean hitTest(Vector2 position) {
		return hitTest(position.x, position.y);
	}

	@Override
	protected byte getLayer() {
		return 2;
	}
}
