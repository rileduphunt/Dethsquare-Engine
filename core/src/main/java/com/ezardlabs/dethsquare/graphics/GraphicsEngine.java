package com.ezardlabs.dethsquare.graphics;

import com.ezardlabs.dethsquare.Camera;
import com.ezardlabs.dethsquare.QuadTree;
import com.ezardlabs.dethsquare.util.GameListeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static com.ezardlabs.dethsquare.util.Dethsquare.RENDER;

public class GraphicsEngine {
	// Add hook into game loop
	static {
		GameListeners.addRenderListener(GraphicsEngine::render);
	}

	private static final TreeSet<Renderer> renderers = new TreeSet<>();
	private static final QuadTree<Renderer> qt = new QuadTree<>(30);

	private static float[] vertices = new float[0];
	private static short[] indices = new short[0];
	private static float[] uvs = new float[0];
	private static float[] colours = new float[0];

	private static int verticesLength = 0;
	private static int indicesLength = 0;
	private static int uvsLength = 0;
	private static int coloursLength = 0;

	private static HashMap<Integer, ArrayList<Renderer>> map = new HashMap<>();

	private static ArrayList<Renderer> visible = new ArrayList<>();

	public static void register(Renderer renderer) {
		renderers.add(renderer);

		verticesLength += 12;
		indicesLength += 6;
		uvsLength += 8;
		coloursLength += 3;
	}

	public static void deregister(Renderer renderer) {
		renderers.remove(renderer);

		verticesLength -= 12;
		indicesLength -= 6;
		uvsLength -= 8;
		coloursLength -= 3;
	}

	public static void init() {
		qt.build(renderers.parallelStream()
						  .peek(r -> r.getBounds()
									  .set(r.transform.position.x, r.transform.position.y,
											  r.transform.position.x + r.width, r.transform.position.y + r.height))
						  .filter(renderer -> renderer.gameObject.isStatic)
						  .collect(Collectors.toList()));
		/*ArrayList<Renderer> staticRenderers = new ArrayList<>();
		for (Renderer r : renderers.toArray(new Renderer[renderers.size()])) {
			r.getBounds()
			 .set(r.transform.position.x, r.transform.position.y, r.transform.position.x + r.width,
					 r.transform.position.y + r.height);
			if (r.gameObject.isStatic) {
				staticRenderers.add(r);
				renderers.remove(r);
			}
		}
		qt.build(staticRenderers);*/
	}

	public static void clearAll() {
		renderers.clear();
//		guiRenderers.clear();
	}

	public static void destroyAllTextures() {
		RENDER.destroyAllTextures();
	}

	public static void clearQuadTree() {
		qt.reset();
	}

	private static void render() {
		visible.clear();
		qt.retrieve(visible, Camera.main);

		visible.addAll(renderers); // TODO only add renderers that are visible

		render(visible, false);
//		render(guiRenderers, true);
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
		/*vertices[(i * 12)] = vertices[(i * 12) + 3] = r.getXPos();
		vertices[(i * 12) + 1] = vertices[(i * 12) + 10] = r.getYPos() + r.height;
		vertices[(i * 12) + 2] = vertices[(i * 12) + 5] = vertices[(i * 12) + 8] = vertices[(i * 12) + 11] = r.zIndex;
		vertices[(i * 12) + 4] = vertices[(i * 12) + 7] = r.getYPos();
		vertices[(i * 12) + 6] = vertices[(i * 12) + 9] = r.getXPos() + r.width;*/
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
		/*if (r.transform.scale.x < 0 && r.transform.scale.y < 0) {
			uvs[(i * 8) + 6] = r.sprite.u;
			uvs[(i * 8) + 7] = r.sprite.v;
			uvs[(i * 8) + 4] = r.sprite.u;
			uvs[(i * 8) + 5] = r.sprite.v + r.sprite.h;
			uvs[(i * 8) + 2] = r.sprite.u + r.sprite.w;
			uvs[(i * 8) + 3] = r.sprite.v + r.sprite.h;
			uvs[(i * 8)] = r.sprite.u + r.sprite.w;
			uvs[(i * 8) + 1] = r.sprite.v;
		} else if (r.transform.scale.x < 0) {
			uvs[(i * 8) + 4] = r.sprite.u;
			uvs[(i * 8) + 5] = r.sprite.v;
			uvs[(i * 8) + 6] = r.sprite.u;
			uvs[(i * 8) + 7] = r.sprite.v + r.sprite.h;
			uvs[(i * 8)] = r.sprite.u + r.sprite.w;
			uvs[(i * 8) + 1] = r.sprite.v + r.sprite.h;
			uvs[(i * 8) + 2] = r.sprite.u + r.sprite.w;
			uvs[(i * 8) + 3] = r.sprite.v;
		} else if (r.transform.scale.y < 0) {
			uvs[(i * 8)] = r.sprite.u;
			uvs[(i * 8) + 1] = r.sprite.v;
			uvs[(i * 8) + 2] = r.sprite.u;
			uvs[(i * 8) + 3] = r.sprite.v + r.sprite.h;
			uvs[(i * 8) + 4] = r.sprite.u + r.sprite.w;
			uvs[(i * 8) + 5] = r.sprite.v + r.sprite.h;
			uvs[(i * 8) + 6] = r.sprite.u + r.sprite.w;
			uvs[(i * 8) + 7] = r.sprite.v;
		} else {
			uvs[(i * 8) + 2] = r.sprite.u;
			uvs[(i * 8) + 3] = r.sprite.v;
			uvs[(i * 8)] = r.sprite.u;
			uvs[(i * 8) + 1] = r.sprite.v + r.sprite.h;
			uvs[(i * 8) + 6] = r.sprite.u + r.sprite.w;
			uvs[(i * 8) + 7] = r.sprite.v + r.sprite.h;
			uvs[(i * 8) + 4] = r.sprite.u + r.sprite.w;
			uvs[(i * 8) + 5] = r.sprite.v;
		}*/
	}

	private static void setupColours(Renderer r, int i) {
		if (colours.length != coloursLength) {
			colours = new float[coloursLength];
		}
		/*colours[i * 3] = r.tint[0];
		colours[i * 3 + 1] = r.tint[1];
		colours[i * 3 + 2] = r.tint[2];*/
	}
}
