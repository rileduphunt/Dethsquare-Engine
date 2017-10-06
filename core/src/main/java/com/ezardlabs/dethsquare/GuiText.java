package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.TextureAtlas.Sprite;

public class GuiText extends Component implements Bounded {
	private final RectF bounds = new RectF();
	private String text;
	private TextureAtlas font;
	private float fontSize;
	private int depth;
	private float spaceWidth;
	private float letterSpacing = -1;
	private float totalWidth = -1;
	public GameObject[] characters = new GameObject[0];

	private boolean started = false;

	public GuiText(String text, TextureAtlas font, float fontSize) {
		this(text, font, fontSize, -1, 0);
	}

	public GuiText(String text, TextureAtlas font, float fontSize, int depth) {
		this(text, font, fontSize, -1, depth);
	}

	public GuiText(String text, TextureAtlas font, float fontSize, float letterSpacing) {
		this(text, font, fontSize, letterSpacing, 0);
	}

	public GuiText(String text, TextureAtlas font, float fontSize, float letterSpacing, int depth) {
		this.text = text;
		this.font = font;
		this.fontSize = fontSize;
		this.letterSpacing = letterSpacing;
		this.depth = depth;
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

			width += (s.w / s.h) * fontSize + getLetterSpacing();
		}
		width -= getLetterSpacing();
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
				guiRenderer.setDepth(depth);
				characters[i] = GameObject.instantiate(new GameObject(String.valueOf(text.charAt(i)), guiRenderer),
						new Vector2(transform.position.x + xOffset, transform.position.y));
				characters[i].transform.setParent(transform);
			} else {
				//noinspection ConstantConditions
				characters[i].getComponent(GuiRenderer.class).setSprite(s);
			}
			characters[i].setActive(true);

			xOffset += width + getLetterSpacing();
		}
		totalWidth = xOffset - getLetterSpacing();

		bounds.set(transform.position.x, transform.position.y, transform.position.x + totalWidth,
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

	private float getLetterSpacing() {
		if (letterSpacing >= 0) {
			return letterSpacing;
		} else {
			return Screen.scale * 6.25f;
		}
	}

	public boolean hitTest(float x, float y) {
		return bounds.contains(x, y);
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
