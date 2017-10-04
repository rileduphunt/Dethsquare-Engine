package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.util.Dethsquare;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Layers {
	private static final String[] LAYERS = {
			"Default",	// 0
			null,		// 1
			null,		// 2
			null,		// 3
			null,		// 4
			null,		// 5
			null,		// 6
			null,		// 7
			null,		// 8
			null,		// 9
			null,		// 10
			null,		// 11
			null,		// 12
			null,		// 13
			null,		// 14
			null,		// 15
			null,		// 16
			null,		// 17
			null,		// 18
			null,		// 19
			null,		// 20
			null,		// 21
			null,		// 22
			null,		// 23
			null,		// 24
			null,		// 25
			null,		// 26
			null,		// 27
			null,		// 28
			null,		// 29
			null,		// 30
			null		// 31
	};
	static {
		parseLayersConfig();
	}

	private static void parseLayersConfig() {
		InputStream is = Dethsquare.IO.getInputStream("config/layers.json");
		if (is != null) {
			try(BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
				StringBuilder data = new StringBuilder();
				String temp;
				while ((temp = reader.readLine()) != null) {
					data.append(temp).append(" ");
				}
				JSONArray json = new JSONArray(data.toString());
				for (int i = 0; i < json.length(); i++) {
					parseLayer(json.getJSONObject(i));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private static void parseLayer(JSONObject layerObject) {
		try {
			int layer = layerObject.getInt("layer");
			String name = layerObject.getString("name");
			if (layer > 7 && layer < 32) {
				LAYERS[layer] = name;
			} else {
				throw new MalformedLayerException();
			}
		} catch(JSONException e) {
			throw new MalformedLayerException();
		}
	}

	public static int getLayer(String name) {
		if (name == null) return -1;
		for (int i = 0; i < LAYERS.length; i++) {
			if (name.equalsIgnoreCase(LAYERS[i])) {
				return i;
			}
		}
		return -1;
	}

	public static int getLayerMask(String... layerNames) {
		int mask = 0;
		for (String name : layerNames) {
			int layer = getLayer(name);
			if (layer > -1) {
				mask |= 1 << layer;
			}
		}
		return mask;
	}

	private static class MalformedLayerException extends RuntimeException {

		private MalformedLayerException() {
			super("Layer config file is malformed - please validate it against the layer schema");
		}
	}
}
