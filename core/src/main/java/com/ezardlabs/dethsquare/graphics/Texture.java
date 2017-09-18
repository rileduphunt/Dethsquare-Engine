package com.ezardlabs.dethsquare.graphics;

import com.ezardlabs.dethsquare.TextureAtlas;
import com.ezardlabs.dethsquare.TextureAtlas.Sprite;
import com.ezardlabs.dethsquare.util.Dethsquare;

public class Texture {
	private int textureName;
	private TextureAtlas textureAtlas;
	private Sprite sprite;

	public Texture(TextureAtlas textureAtlas) {
		this(textureAtlas, new Sprite(0, 0, 1, 1));
	}

	public Texture(TextureAtlas textureAtlas, String spriteName) {
		this(textureAtlas, textureAtlas.getSprite(spriteName));
	}

	public Texture(TextureAtlas textureAtlas, Sprite sprite) {
		textureName = textureAtlas.textureName;
	}

	public Texture(String imagePath) {
		textureName = Dethsquare.RENDER.loadImage(imagePath)[1];
		textureAtlas = null;
	}

	public void setSprite(Sprite sprite) {
		this.sprite = sprite;
	}

	int getTextureName() {
		return textureName;
	}
}
