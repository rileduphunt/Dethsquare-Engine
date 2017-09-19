package com.ezardlabs.dethsquare.graphics;

import com.ezardlabs.dethsquare.Bounded;
import com.ezardlabs.dethsquare.Component;
import com.ezardlabs.dethsquare.GameObject;
import com.ezardlabs.dethsquare.RectF;
import com.ezardlabs.dethsquare.TextureAtlas;
import com.ezardlabs.dethsquare.TextureAtlas.Sprite;
import com.ezardlabs.dethsquare.Vector2;

import static com.ezardlabs.dethsquare.util.Dethsquare.RENDER;

public class Renderer extends Component implements Bounded, Comparable<Renderer> {
	/**
	 * 0 - 1 (2 bits):		layer:
	 * 							game = 0
	 * 							game fullscreen effect = 1
	 * 							HUD = 2
	 * 							total fullscreen effect = 3
	 * 2 - 5 (4 bits):		viewport
	 * 6 - 7 (2 bits): 		translucency type (opaque, translucent (space for normal/additive/subtractive subtypes)):
	 * 							opaque = 0
	 * 							translucent = 1
	 * 8 - 39 (32 bits):	depth
	 * 40 - 63 (24 bits):	texture ID
	 *
	 * 00   	0000		00				00000000000000000000000000000000	000000000000000000000000
	 * layer	viewport	translucency	depth								texture ID
	 */
	private long key = 0;

	public Sprite sprite = new Sprite(0, 0, 0, 0);
	public float width;
	public float height;
	private int depth = 0;
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

	public long getKey() {
		return key;
	}

	private void setTextureName(int textureName) {
		this.textureName = textureName;
		// clear material section id key
		key = key &~ 16777215L;
		// set material section of key
		key |= textureName;
	}

	public void setImage(String imagePath, float width, float height) {
		int[] data = RENDER.loadImage(imagePath);
		setTextureName(data[0]);
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

	public void setOffsets(Vector2 offsets) {
		xOffset = offsets.x;
		yOffset = offsets.y;
	}

	public void setTextureAtlas(TextureAtlas textureAtlas) {
		setTextureName(textureAtlas.textureName);
	}

	public void setTextureAtlas(TextureAtlas textureAtlas, float spriteWidth, float spriteHeight) {
		setTextureName(textureAtlas.textureName);
		width = spriteWidth;
		height = spriteHeight;
	}

	public void setTint(float red, float green, float blue) {
		tint[0] = red;
		tint[1] = green;
		tint[2] = blue;
	}

	public void setDepth(int depth) {
		this.depth = depth;
		// clear depth section of key
		key = key &~ 72057594021150720L;
		// set depth section
		key |= ((long) depth) << 24;
	}

	public int getDepth() {
		return depth;
	}

	public float[] getTint() {
		return tint;
	}

	protected float getXPos() {
		return transform.position.x + xOffset;
	}

	protected float getYPos() {
		return transform.position.y + yOffset;
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

	@Override
	public int compareTo(Renderer r) {
		return Long.compare(key, r.key);
	}
}