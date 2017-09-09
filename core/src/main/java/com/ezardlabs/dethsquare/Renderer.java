package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.Animation.FrameData;
import com.ezardlabs.dethsquare.TextureAtlas.Sprite;
import com.ezardlabs.dethsquare.util.GameListeners;

import java.util.ArrayList;
import java.util.HashMap;

import static com.ezardlabs.dethsquare.util.Dethsquare.RENDER;

public class Renderer extends Component implements Bounded {
	// Add hook into game loop
	static {
		GameListeners.addRenderListener(Renderer::renderAll);
	}

	private static QuadTree<Renderer> qt = new QuadTree<>(30);
	private static ArrayList<Renderer> renderers = new ArrayList<>();
	private static ArrayList<Renderer> guiRenderers = new ArrayList<>();

	private static HashMap<Integer, ArrayList<Renderer>> map = new HashMap<>();

	private static ArrayList<Renderer> visible = new ArrayList<>();

	private static float[] vertices = new float[0];
	private static short[] indices = new short[0];
	private static float[] uvs = new float[0];
	private static float[] colours = new float[0];

	private static int verticesLength = 0;
	private static int indicesLength = 0;
	private static int uvsLength = 0;
	private static int coloursLength = 0;

	Sprite sprite;
	public float width;
	public float height;
	private int zIndex = 0;
	private final float[] tint = new float[3];
	private final RectF bounds = new RectF();

	public int textureName = -1;
	public Mode mode = Mode.NONE;
	public float xOffset;
	public float yOffset;

	private enum Mode {
		NONE,
		IMAGE,
		SPRITE
	}

	public Renderer() {
	}

	public Renderer(String imagePath, float width, float height) {
		setImage(imagePath, width, height);
	}

	public Renderer(TextureAtlas textureAtlas, Sprite sprite, float width, float height) {
		setTextureAtlas(textureAtlas, width, height);
		this.sprite = sprite;
	}

	public void setImage(String imagePath, float width, float height) {
		mode = Mode.IMAGE;
		int[] data = RENDER.loadImage(imagePath);
		textureName = data[0];
		this.width = width;
		this.height = height;
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

	public void setData(FrameData data) {
		setSize(data.width, data.height);
		setOffsets(data.getOffset(transform.scale.x).x, data.getOffset(transform.scale.x).y);
	}

	public void setTextureAtlas(TextureAtlas textureAtlas, float spriteWidth, float spriteHeight) {
		textureName = textureAtlas.textureName;
		mode = Mode.SPRITE;
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
		if (this instanceof GuiRenderer) {
			guiRenderers.add(this);
		} else {
			renderers.add(this);
		}
		verticesLength += 12;
		indicesLength += 6;
		uvsLength += 8;
		coloursLength += 3;
	}

	@Override
	protected void destroy() {
		if (this instanceof GuiRenderer) {
			guiRenderers.remove(this);
		} else {
			renderers.remove(this);
		}
		vertices = new float[vertices.length - 12];
		indices = new short[indices.length - 6];
		uvs = new float[uvs.length - 8];
		colours = new float[colours.length - 3];
	}

	public static void init() {
		ArrayList<Renderer> staticRenderers = new ArrayList<>();
		for (Renderer r : renderers.toArray(new Renderer[renderers.size()])) {
			r.bounds.set(r.transform.position.x, r.transform.position.y, r.transform.position.x + r.width,
					r.transform.position.y + r.height);
			if (r.gameObject.isStatic) {
				staticRenderers.add(r);
				renderers.remove(r);
			}
		}
		qt.init(staticRenderers.toArray(new Renderer[staticRenderers.size()]));
	}

	static void clearAll() {
		renderers.clear();
		guiRenderers.clear();
	}

	static void destroyAllTextures() {
		RENDER.destroyAllTextures();
	}

	static void clearQuadTree() {
		qt = new QuadTree<>(30);
	}

	private static void renderAll() {
		visible.clear();
		QuadTree.retrieve(visible, qt, Camera.main.bounds);

		visible.addAll(renderers); // TODO only add renderers that are visible

		render(visible, false);
		render(guiRenderers, true);
	}

	private static void render(ArrayList<Renderer> renderers, boolean gui) {
		RENDER.setGuiRenderMode(gui);

		map.clear();
		for (int i = 0; i < renderers.size(); i++) {
			if (map.size() == 0 || !map.containsKey(renderers.get(i).textureName)) {
				map.put(renderers.get(i).textureName, new ArrayList<>());
			}
			map.get(renderers.get(i).textureName).add(renderers.get(i));
		}
		for (int i : map.keySet()) {
			setupRenderData(map.get(i));
			RENDER.render(i, vertices, uvs, indices, colours, map.get(i).size());
		}
	}

	private static void setupRenderData(ArrayList<Renderer> renderers) {
		int i = 0;
		int last = 0;
		Renderer r;
		for (int j = 0; j < renderers.size(); j++) {
			r = renderers.get(j);

			setupVertices(r, i);

			setupIndices(i, last);
			last = last + 4;

			setupUVs(r, i);

			setupColours(r, i);

			i++;
		}
	}

	private static void setupVertices(Renderer r, int i) {
		if (vertices.length != verticesLength) {
			vertices = new float[verticesLength];
		}
		vertices[(i * 12)] = vertices[(i * 12) + 3] = r.getXPos();
		vertices[(i * 12) + 1] = vertices[(i * 12) + 10] = r.getYPos() + r.height;
		vertices[(i * 12) + 2] = vertices[(i * 12) + 5] = vertices[(i * 12) + 8] = vertices[(i * 12) + 11] = r.zIndex;
		vertices[(i * 12) + 4] = vertices[(i * 12) + 7] = r.getYPos();
		vertices[(i * 12) + 6] = vertices[(i * 12) + 9] = r.getXPos() + r.width;
	}

	private static void setupIndices(int i, int last) {
		if (indices.length != indicesLength) {
			indices = new short[indicesLength];
		}
		indices[(i * 6)] = indices[(i * 6) + 3] = (short) (last);
		indices[(i * 6) + 1] = (short) (last + 1);
		indices[(i * 6) + 2] = indices[(i * 6) + 4] = (short) (last + 2);
		indices[(i * 6) + 5] = (short) (last + 3);
	}

	private static void setupUVs(Renderer r, int i) {
		if (uvs.length != uvsLength) {
			uvs = new float[uvsLength];
		}
		float u;
		float v;
		float w;
		float h;
		switch (r.mode) {
			case IMAGE:
				u = 0;
				v = 0;
				w = 1;
				h = 1;
				break;
			case SPRITE:
				u = r.sprite.u;
				v = r.sprite.v;
				w = r.sprite.w;
				h = r.sprite.h;
				break;
			case NONE:
			default:
				u = v = w = h = 0;
				break;
		}
		if (r.transform.scale.x < 0 && r.transform.scale.y < 0) {
			uvs[(i * 8) + 6] = u;
			uvs[(i * 8) + 7] = v;
			uvs[(i * 8) + 4] = u;
			uvs[(i * 8) + 5] = v + h;
			uvs[(i * 8) + 2] = u + w;
			uvs[(i * 8) + 3] = v + h;
			uvs[(i * 8)] = u + w;
			uvs[(i * 8) + 1] = v;
		} else if (r.transform.scale.x < 0) {
			uvs[(i * 8) + 4] = u;
			uvs[(i * 8) + 5] = v;
			uvs[(i * 8) + 6] = u;
			uvs[(i * 8) + 7] = v + h;
			uvs[(i * 8)] = u + w;
			uvs[(i * 8) + 1] = v + h;
			uvs[(i * 8) + 2] = u + w;
			uvs[(i * 8) + 3] = v;
		} else if (r.transform.scale.y < 0) {
			uvs[(i * 8)] = u;
			uvs[(i * 8) + 1] = v;
			uvs[(i * 8) + 2] = u;
			uvs[(i * 8) + 3] = v + h;
			uvs[(i * 8) + 4] = u + w;
			uvs[(i * 8) + 5] = v + h;
			uvs[(i * 8) + 6] = u + w;
			uvs[(i * 8) + 7] = v;
		} else {
			uvs[(i * 8) + 2] = u;
			uvs[(i * 8) + 3] = v;
			uvs[(i * 8)] = u;
			uvs[(i * 8) + 1] = v + h;
			uvs[(i * 8) + 6] = u + w;
			uvs[(i * 8) + 7] = v + h;
			uvs[(i * 8) + 4] = u + w;
			uvs[(i * 8) + 5] = v;
		}
	}

	private static void setupColours(Renderer r, int i) {
		if (colours.length != coloursLength) {
			colours = new float[coloursLength];
		}
		colours[i * 3] = r.tint[0];
		colours[i * 3 + 1] = r.tint[1];
		colours[i * 3 + 2] = r.tint[2];
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