package com.ezardlabs.dethsquare.animation;

import com.ezardlabs.dethsquare.TextureAtlas.Sprite;
import com.ezardlabs.dethsquare.Vector2;

public final class Animation {
	public final String name;
	final Sprite[] frames;
	final FrameData[] frameData;
	AnimationType type;
	AnimationListener listener;

	public Animation(String name, Sprite[] frames, FrameData[] frameData, AnimationType type) {
		this(name, frames, frameData, type, null);
	}

	public Animation(String name, Sprite[] frames, FrameData[] frameData, AnimationType type,
			AnimationListener listener) {
		this.name = name;
		this.frames = frames;
		this.type = type.clone();
		this.frameData = frameData;
		this.listener = listener;
	}

	public void setAnimationType(AnimationType type) {
		this.type = type;
	}

	public void setAnimationListener(AnimationListener listener) {
		this.listener = listener;
	}

	static class FrameData {
		private final int width;
		private final int height;
		private final Vector2[] offsets;
		private final long duration;

		FrameData(int width, int height, Vector2 offset, long duration) {
			this.width = width;
			this.height = height;
			this.offsets = new Vector2[]{offset};
			this.duration = duration;
		}

		FrameData(int width, int height, Vector2[] offsets, long duration) {
			this.width = width;
			this.height = height;
			this.offsets = offsets;
			this.duration = duration;
		}

		int getWidth() {
			return width;
		}

		int getHeight() {
			return height;
		}

		Vector2 getOffset(float scale) {
			if (offsets.length == 1) return offsets[0];
			if (scale > 0) {
				return offsets[0];
			} else {
				return offsets[1];
			}
		}

		long getDuration() {
			return duration;
		}
	}

	public interface AnimationListener {
		void onAnimatedStarted(Animator animator);

		void onFrame(Animator animator, int frameNum);

		void onAnimationFinished(Animator animator);
	}
}
