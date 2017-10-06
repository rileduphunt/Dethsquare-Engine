package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.graphics.Renderer;
import com.ezardlabs.dethsquare.util.GameListeners;

public class GuiRenderer extends Renderer {
	private static int referenceScreenWidth = 1920;
	private static int referenceScreenHeight = 1080;
	private static ScaleMode referenceScreenScaleMode = ScaleMode.WIDTH;
	static float referenceScreenScale = 1;

	static {
		GameListeners.addResizeListener((width, height) -> updateReferenceScreenScale());
	}

	public enum ScaleMode {
		WIDTH,
		HEIGHT
	}

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
	public RectF getBounds() {
		bounds.set(getXPos() * referenceScreenScale, getYPos() * referenceScreenScale,
				(getXPos() + width) * referenceScreenScale, (getYPos() + height) * referenceScreenScale);
		return bounds;
	}

	@Override
	protected byte getLayer() {
		return 2;
	}

	public static void setReferenceScreenSize(int width, int height, ScaleMode scaleMode) {
		GuiRenderer.referenceScreenWidth = width;
		GuiRenderer.referenceScreenHeight = height;
		GuiRenderer.referenceScreenScaleMode = scaleMode;
		updateReferenceScreenScale();
	}

	private static void updateReferenceScreenScale() {
		switch (referenceScreenScaleMode) {
			case WIDTH:
				referenceScreenScale = (float) Screen.width / (float) referenceScreenWidth;
				break;
			case HEIGHT:
				referenceScreenScale = (float) Screen.height / (float) referenceScreenHeight;
				break;
			default:
				break;
		}
	}
}
