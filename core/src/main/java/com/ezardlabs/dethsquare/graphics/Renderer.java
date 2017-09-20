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
	private static final int DEPTH_OFFSET = 100000;
	private static final int MIN_DEPTH = -DEPTH_OFFSET;
	private static final int MAX_DEPTH = DEPTH_OFFSET;
	/**
	 * <table cellspacing="10">
	 *     <tr>
	 *         <td>0 (1 bit):</td>
	 *         <td>ignored</td>
	 *     </tr>
	 *     <tr>
	 *         <td>1 - 2 (2 bits):</td>
	 *         <td>layer:</td>
	 *     </tr>
	 *     <tr>
	 *         <td></td>
	 *         <td>
	 *             <ul style="list-style:none">
	 *                 <li>game = 0</li>
	 *                 <li>game fullscreen effect = 1</li>
	 *                 <li>HUD = 2</li>
	 *                 <li>total fullscreen effect = 3</li>
	 *             </ul>
	 *         </td>
	 *     </tr>
	 *     <tr>
	 *         <td>3 - 5 (3 bits):</td>
	 *         <td>viewport</td>
	 *     </tr>
	 *     <tr>
	 *         <td>6 - 7 (2 bits):</td>
	 *         <td>translucency type (opaque, translucent (space for normal/additive/subtractive subtypes)):</td>
	 *     </tr>
	 *     <tr>
	 *         <td></td>
	 *         <td>
	 *             <ul>
	 *                 <li>opaque = 0</li>
	 *                 <li>translucent = 1</li>
	 *             </ul>
	 *         </td>
	 *     </tr>
	 *     <tr>
	 *         <td>8 - 39 (32 bits):</td>
	 *         <td>depth</td>
	 *     </tr>
	 *     <tr>
	 *         <td>40 - 63 (24 bits):</td>
	 *         <td>texture ID</td>
	 *     </tr>
	 * </table>
	 * <br/><br/>
	 * <table cellspacing="10">
	 *     <tr>
	 *         <td>0</td>
	 *         <td>00</td>
	 *         <td>000</td>
	 *         <td>00</td>
	 *         <td>00000000000000000000000000000000</td>
	 *         <td>000000000000000000000000</td>
	 *     </tr>
	 *     <tr>
	 *         <td>ignored</td>
	 *         <td>layer</td>
	 *         <td>viewport</td>
	 *         <td>translucency</td>
	 *         <td>depth</td>
	 *         <td>texture ID</td>
	 *     </tr>
	 * </table>
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
		setKeyLayer();
	}

	public Renderer(TextureAtlas textureAtlas, Sprite sprite, float width, float height) {
		setTextureAtlas(textureAtlas, width, height);
		this.sprite = sprite;
		setKeyLayer();
	}

	public Renderer(TextureAtlas textureAtlas) {
		setTextureAtlas(textureAtlas);
		setKeyLayer();
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
		if (depth < MIN_DEPTH || depth > MAX_DEPTH) {
			throw new IllegalArgumentException("Depth must be between " + MIN_DEPTH + " and " + MAX_DEPTH);
		}
		this.depth = depth + DEPTH_OFFSET;
		// clear depth section of key
		key = key &~ 72057594021150720L;
		// set depth section
		key |= Integer.toUnsignedLong(this.depth) << 24;
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

	private void setKeyLayer() {
		byte layer = getLayer();
		if (layer < 0 || layer > 3) {
			throw new IllegalArgumentException("Layer must be between 0 and 3 inclusive");
		} else {
			// clear layer section of key
			key = key &~ 6917529027641081856L;
			// set layer section of key
			//noinspection NumericOverflow
			key |= (long) layer << 61;
		}
	}

	protected byte getLayer() {
		return 0;
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