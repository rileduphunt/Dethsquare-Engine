package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.TextureAtlas.Sprite;

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
		this.type = (AnimationType) type.clone();
		this.frameData = frameData;
		this.listener = listener;
	}

	public void setAnimationType(AnimationType type) {
		this.type = type;
	}

	public void setAnimationListener(AnimationListener listener) {
		this.listener = listener;
	}

	public static class FrameData {
		final int width;
		final int height;
		final Vector2 offset;
		final long duration;

		public FrameData(int width, int height, Vector2 offset, long duration) {
			this.width = width;
			this.height = height;
			this.offset = offset;
			this.duration = duration;
		}
	}

	public interface AnimationListener {
		void onAnimatedStarted(Animator animator);

		void onFrame(Animator animator, int frameNum);

		void onAnimationFinished(Animator animator);
	}
}
