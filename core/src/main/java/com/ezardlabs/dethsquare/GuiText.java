package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.TextureAtlas.Sprite;

public class GuiText extends Component implements Bounded {
	private final RectF bounds = new RectF();
	private String text;
	private TextureAtlas font;
	private float fontSize;
	private int zIndex;
	private float spaceWidth;
	private float totalWidth = -1;
	public GameObject[] characters = new GameObject[0];

	private boolean started = false;

	public GuiText(String text, TextureAtlas font, float fontSize) {
		this(text, font, fontSize, 0);
	}

	public GuiText(String text, TextureAtlas font, float fontSize, int zIndex) {
		this.text = text;
		this.font = font;
		this.fontSize = fontSize;
		this.zIndex = zIndex;
	}

	@Override
	public void start() {
		started = true;
		calculateSpaceWidth();
		generateRenderers();
	}

	@Override
	protected void destroy() {
		for (int i = 0; i < characters.length; i++) {
			GameObject.destroy(characters[i], 1);
		}
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
		if (started) generateRenderers();
	}

	public TextureAtlas getFont() {
		return font;
	}

	public void setFont(TextureAtlas font) {
		this.font = font;
		calculateSpaceWidth();
		if (started) generateRenderers();
	}

	public float getFontSize() {
		return fontSize;
	}

	public void setFontSize(float fontSize) {
		this.fontSize = fontSize;
		calculateSpaceWidth();
		if (started) generateRenderers();
	}

	public float getWidth() {
		if (totalWidth != -1) return totalWidth;
		float width = 0;
		for (int i = 0; i < text.length(); i++) {
			Sprite s = null;
			boolean shouldContinue = false;
			switch (text.charAt(i)) {
				case ' ':
					width += spaceWidth;
					shouldContinue = true;
					break;
				case '!':
					s = font.getSprite("exclamation-mark");
					break;
				case '?':
					s = font.getSprite("question-mark");
					break;
				case '.':
					s = font.getSprite("period");
					break;
				case ':':
					s = font.getSprite("colon");
					break;
				default:
					s = font.getSprite(String.valueOf(text.charAt(i)));
					break;
			}
			if (shouldContinue || s == null) continue;

			width += (s.w / s.h) * fontSize + Screen.scale * 6.25f;
		}
		width -= Screen.scale * 6.25f;
		totalWidth = width;
		return width;
	}

	private void generateRenderers() {
		if (text == null) text = "";

		if (text.length() < characters.length) {
			for (int i = text.length(); i < characters.length; i++) {
				characters[i].setActive(false);
			}
		} else if (text.length() > characters.length) {
			GameObject[] temp = new GameObject[text.length()];
			System.arraycopy(characters, 0, temp, 0, characters.length);
			characters = temp;
		}

		/*for (GameObject go : characters) {
			if (go != null) {
				GameObject.destroy(go);
			}
		}*/

//		characters = new GameObject[text.length()];

		Sprite s;
		float xOffset = 0;
		for (int i = 0; i < text.length(); i++) {
			switch (text.charAt(i)) {
				case ' ':
					xOffset += spaceWidth;
					continue;
				case '!':
					s = font.getSprite("exclamation-mark");
					break;
				case '?':
					s = font.getSprite("question-mark");
					break;
				case '.':
					s = font.getSprite("period");
					break;
				case ':':
					s = font.getSprite("colon");
					break;
				default:
					s = font.getSprite(String.valueOf(text.charAt(i)));
					break;
			}
			if (s == null) continue;

			float width = (s.w / s.h) * fontSize;

			if (characters[i] == null) {
				GuiRenderer guiRenderer = new GuiRenderer(font, s, width, fontSize);
				guiRenderer.setDepth(zIndex);
				characters[i] = GameObject.instantiate(new GameObject(String.valueOf(text.charAt(i)), guiRenderer),
						new Vector2(transform.position.x + xOffset, transform.position.y));
				characters[i].transform.setParent(transform);
			} else {
				//noinspection ConstantConditions
				characters[i].getComponent(GuiRenderer.class).setSprite(s);
			}
			characters[i].setActive(true);

			xOffset += width + Screen.scale * 6.25f;
		}
		totalWidth = xOffset - Screen.scale * 6.25f;

		bounds.set(transform.position.x, transform.position.y, transform.position.x + xOffset,
				transform.position.y + fontSize);
	}

	private void calculateSpaceWidth() {
		Sprite[] chars = font.getSprites();
		float total = 0;
		for (Sprite s : chars) {
			total += (s.w / s.h) * fontSize;
		}
		spaceWidth = total / chars.length;
	}

	public boolean hitTest(float x, float y) {
		return x > transform.position.x * Screen.scale && x < (transform.position.x + totalWidth) * Screen.scale &&
				y > transform.position.y * Screen.scale && y < (transform.position.y + fontSize) * Screen.scale;
	}

	public boolean hitTest(Vector2 position) {
		return hitTest(position.x, position.y);
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
