package com.ezardlabs.dethsquare.graphics;

import com.ezardlabs.dethsquare.util.Dethsquare;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;

public class Material {
	private static HashMap<String, Material> cache = new HashMap<>();
	private static int idCount = 0;

	private final int id;
	private final int shader;
	private final HashMap<String, Integer> uniformMapping;
	private final HashMap<String, Integer> attributeMapping;

	private Material(int shader, HashMap<String, Integer> uniformMapping, HashMap<String, Integer> attributeMapping) {
		this.id = idCount++;
		this.shader = shader;
		this.uniformMapping = uniformMapping;
		this.attributeMapping = attributeMapping;
	}

	public static Material load(String path) {
		if (cache.containsKey(path)) {
			return cache.get(path).copy();
		} else {
			JSONObject json = new JSONObject(String.join(" ", Dethsquare.IO.getFileLines(path)));

			int shader = parseShader(json);
			HashMap<String, Integer> uniformMapping = parseVariables(shader, json, true);
			HashMap<String, Integer> attributeMapping = parseVariables(shader, json, false);

			Material material = new Material(shader, uniformMapping, attributeMapping);
			cache.put(path, material);
			return material;
		}
	}

	private static int parseShader(JSONObject json) {
		JSONObject shaderObject = json.getJSONObject("shader");
		return Dethsquare.RENDER.loadShaderProgram(shaderObject.getString("vertex"),
				shaderObject.getString("fragment"));
	}

	private static HashMap<String, Integer> parseVariables(int shader, JSONObject json, boolean global) {
		HashMap<String, Integer> map = new HashMap<>();
		JSONArray properties = json.getJSONArray("properties");
		for (int i = 0; i < properties.length(); i++) {
			JSONObject property = properties.getJSONObject(i);
			if (global) {
				if (property.has("global") && property.getBoolean("global")) {
					String name = property.getString("name");
					map.put(name, Dethsquare.RENDER.getUniformLocation(shader, name));
				}
			} else {
				if (!property.has("global") || (property.has("global") && !property.getBoolean("global"))) {
					String name = property.getString("name");
					map.put(name, Dethsquare.RENDER.getAttributeLocation(shader, name));
				}
			}
		}
		return map;
	}

	int getId() {
		return id;
	}

	private Material copy() {
		return new Material(shader, new HashMap<>(uniformMapping), new HashMap<>(attributeMapping));
	}
}
