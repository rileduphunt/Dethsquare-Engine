package com.ezardlabs.dethsquare.animation;

import com.ezardlabs.dethsquare.Animation;
import com.ezardlabs.dethsquare.Animation.FrameData;
import com.ezardlabs.dethsquare.AnimationType;
import com.ezardlabs.dethsquare.TextureAtlas;
import com.ezardlabs.dethsquare.TextureAtlas.Sprite;
import com.ezardlabs.dethsquare.Vector2;
import com.ezardlabs.dethsquare.util.Dethsquare;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashSet;

public class Animations {

	public static Animation[] load(String path, TextureAtlas ta) {
		return load(path, ta, null);
	}

	public static Animation[] load(String path, TextureAtlas ta, Validator validator) {
		AnimationData data = AnimationData.parse(path);
		Animation[] animations = data.getAnimations(ta);

		if (validator != null) {
			if (validator.validate(animations)) {
				return animations;
			} else {
				throw new Error("Animation validation failed for " + path);
			}
		}
		return animations;
	}

	private static class AnimationData {
		private final HashSet<DataAnimation> animations = new HashSet<>();
		private Defaults defaults;

		private AnimationData() {
		}

		private Animation[] getAnimations(TextureAtlas ta) {
			Animation[] animations = new Animation[this.animations.size()];
			int count = 0;
			for (DataAnimation da : this.animations) {
				animations[count++] = da.toAnimation(ta);
			}
			return animations;
		}

		private static AnimationData parse(String path) {
			JSONObject json = new JSONObject(
					String.join(" ", Dethsquare.IO.getFileLines("animations/" + path + "/anim.json")));

			AnimationData data;
			if (json.has("extends")) {
				data = parse(json.getString("extends"));
			} else {
				data = new AnimationData();
			}

			if (json.has("defaults")) {
				data.defaults = Defaults.parse(json.getJSONObject("defaults"));
			}

			data.animations.addAll(DataAnimation.parseArray(json.getJSONArray("animations"), data.defaults));

			return data;
		}

		private static class Defaults {
			private final int width;
			private final int height;
			private final Vector2 offset;
			private final long duration;

			private Defaults(int width, int height, Vector2 offset, long duration) {
				this.width = width;
				this.height = height;
				this.offset = offset;
				this.duration = duration;
			}

			private static Defaults parse(JSONObject json) {
				JSONObject offset = json.getJSONObject("offset");
				return new Defaults(json.getInt("width"), json.getInt("height"),
						new Vector2(offset.getInt("x"), offset.getInt("y")), json.getLong("duration"));
			}
		}

		private static class DataAnimation {
			private final String name;
			private final ArrayList<DataSprite> sprites;
			private final String type;

			private DataAnimation(String name, ArrayList<DataSprite> sprites, String type) {
				this.name = name;
				this.sprites = sprites;
				this.type = type;
			}

			private Animation toAnimation(TextureAtlas ta) {
				Sprite[] sprites = new Sprite[this.sprites.size()];
				FrameData[] frameData = new FrameData[this.sprites.size()];

				int count = 0;
				for (DataSprite ds : this.sprites) {
					sprites[count] = ds.toSprite(ta);
					frameData[count] = ds.toFrameData();
				}

				AnimationType animationType;
				switch (type) {
					case "ONE_SHOT":
						animationType = AnimationType.ONE_SHOT;
						break;
					case "LOOP":
						animationType = AnimationType.LOOP;
						break;
					case "OSCILLATE":
						animationType = AnimationType.OSCILLATE;
						break;
					default:
						throw new Error("No animation type was specified for '" + name + "'");
				}
				return new Animation(name, sprites, frameData, animationType);
			}

			public boolean equals(Object o) {
				return (o instanceof DataAnimation) && (((DataAnimation) o).name.equals(name));
			}

			public int hashCode() {
				return name.hashCode();
			}

			private static ArrayList<DataAnimation> parseArray(JSONArray array, Defaults defaults) {
				ArrayList<DataAnimation> animations = new ArrayList<>();

				for (int i = 0; i < array.length(); i++) {
					animations.add(parse(array.getJSONObject(i), defaults));
				}

				return animations;
			}

			private static DataAnimation parse(JSONObject json, Defaults defaults) {
				ArrayList<DataSprite> sprites = new ArrayList<>();
				JSONArray array = json.getJSONArray("sprites");

				for (int i = 0; i < array.length(); i++) {
					sprites.add(DataSprite.parse(array.getJSONObject(i), defaults));
				}

				return new DataAnimation(json.getString("name"), sprites, json.getString("type"));
			}

			private static class DataSprite {
				private final String name;
				private final int width;
				private final int height;
				private final Vector2 offset;
				private final long duration;

				private DataSprite(String name, int width, int height, Vector2 offset, long duration) {
					this.name = name;
					this.width = width;
					this.height = height;
					this.offset = offset;
					this.duration = duration;
				}

				private Sprite toSprite(TextureAtlas ta) {
					return ta.getSprite(name);
				}

				private FrameData toFrameData() {
					return new FrameData(width, height, offset, duration);
				}

				private static DataSprite parse(JSONObject json, Defaults defaults) {
					return new DataSprite(json.getString("name"),
							json.has("width") ? json.getInt("width") : defaults.width,
							json.has("height") ? json.getInt("height") : defaults.height,
							json.has("offset") ? new Vector2(json.getJSONObject("offset").getInt("x"),
									json.getJSONObject("offset").getInt("y")) : defaults.offset,
							json.has("duration") ? json.getLong("duration") : defaults.duration);
				}
			}
		}
	}

	public static class Validator {
		private final String[] requiredAnimations;

		public Validator(String... requiredAnimations) {
			this.requiredAnimations = requiredAnimations;
		}

		private boolean validate(Animation[] animations) {
			for (String s : requiredAnimations) {
				boolean found = false;
				for (Animation a : animations) {
					if (a.name.equals(s)) {
						found = true;
						break;
					}
				}
				if (!found) return false;
			}
			return true;
		}
	}
}
