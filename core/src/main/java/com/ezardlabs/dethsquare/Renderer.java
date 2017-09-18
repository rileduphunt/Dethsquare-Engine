package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.TextureAtlas.Sprite;
import com.ezardlabs.dethsquare.graphics.GraphicsEngine;

import static com.ezardlabs.dethsquare.util.Dethsquare.RENDER;

public class Renderer extends Component implements Bounded {
	public Sprite sprite = new Sprite(0, 0, 0, 0);
	public float width;
	public float height;
	private int zIndex = 0;
	private final float[] tint = new float[3];
	private final RectF bounds = new RectF();

	public int textureName = -1;
	public float xOffset;
	public float yOffset;

	public Renderer() {
	}

	public Renderer(String imagePath, float width, float height) {
		setImage(imagePath, width, height);
	}

	public Renderer(TextureAtlas textureAtlas, Sprite sprite, float width, float height) {
		setTextureAtlas(textureAtlas, width, height);
		this.sprite = sprite;
	}

	public Renderer(TextureAtlas textureAtlas) {
		setTextureAtlas(textureAtlas);
	}

	public void setImage(String imagePath, float width, float height) {
		int[] data = RENDER.loadImage(imagePath);
		textureName = data[0];
		this.width = width;
		this.height = height;
		sprite.u = sprite.v = 0;
		sprite.w = sprite.h = 1;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	public void setSize(float width, float height) {
		this.width = width;
		this.height = height;
	}

	public void setOffsets(float xOffset, float yOffset) {
		this.xOffset = xOffset;
		this.yOffset = yOffset;
	}

	public void setTextureAtlas(TextureAtlas textureAtlas) {
		textureName = textureAtlas.textureName;
	}

	public void setTextureAtlas(TextureAtlas textureAtlas, float spriteWidth, float spriteHeight) {
		textureName = textureAtlas.textureName;
		width = spriteWidth;
		height = spriteHeight;
	}

	public void setTint(float red, float green, float blue) {
		tint[0] = red;
		tint[1] = green;
		tint[2] = blue;
	}

	public Renderer setzIndex(int zIndex) {
		this.zIndex = zIndex;
		return this;
	}

	protected float getXPos() {
		return transform.position.x + xOffset;
	}

	protected float getYPos() {
		return transform.position.y + yOffset;
	}

	protected int getZIndex() {
		return zIndex;
	}

	@Override
	public void start() {
		GraphicsEngine.register(this);
	}

	@Override
	protected void destroy() {
		GraphicsEngine.deregister(this);
	}

	@Override
	public RectF getBounds() {
		return bounds;
	}

	@Override
	public GameObject getGameObject() {
		return gameObject;
	}
}