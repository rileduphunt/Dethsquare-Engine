package com.ezardlabs.dethsquare;

import com.ezardlabs.dethsquare.Component.RequiredComponents;

import java.util.Iterator;

@RequiredComponents(Renderer.class)
public final class Animator extends Script implements Iterable<Animation> {
	private Animation[] animations;
	private int index = -1;
	private int frame = 0;
	private long nextFrameTime = 0;
	private boolean finished = false;
	public boolean shouldUpdate = true;

	public Animator(Animation... animations) {
		this.animations = animations;
	}

	public void setAnimations(Animation... animations) {
		this.animations = animations;
	}

	public void addAnimations(Animation... animations) {
		Animation[] newAnimations = new Animation[this.animations.length + animations.length];
		System.arraycopy(this.animations, 0, newAnimations, 0, this.animations.length);
		System.arraycopy(animations, 0, newAnimations, this.animations.length, animations.length);
		this.animations = newAnimations;
	}

	private void setFrame(int frame) {
		this.frame = frame;
		this.nextFrameTime = System.currentTimeMillis() + animations[index].frameData[frame].duration;
		gameObject.renderer.setData(animations[index].frameData[frame]);
	}

	public void update() {
		if (!shouldUpdate) {
			if (index == -1 || frame == -1) return;
			gameObject.renderer.sprite = animations[index].frames[frame];
			return;
		}
		int startFrame = frame;
		if (index == -1 || frame == -1) return;
		int tempFrame;
		if (System.currentTimeMillis() >= nextFrameTime) {
			nextFrameTime += animations[index].frameData[frame].duration;
			tempFrame = animations[index].type.update(frame, animations[index].frames.length);
			if (tempFrame == -1) {
				if (!finished) {
					if (animations[index].listener != null) {
						animations[index].listener.onAnimationFinished(this);
					}
					finished = true;
				}
				setFrame(tempFrame);
				return;
			} else {
				finished = false;
			}
			setFrame(tempFrame);
			try {
				gameObject.renderer.sprite = animations[index].frames[frame];
			} catch (ArrayIndexOutOfBoundsException ignored) {
			}
		} else {
			tempFrame = frame;
		}
		if (tempFrame != startFrame && animations[index].listener != null) {
			animations[index].listener.onFrame(this, tempFrame);
		}
	}

	public void play(String animationName) {
		if (index != -1 && animations[index].name.equals(animationName)) return;
		for (int i = 0; i < animations.length; i++) {
			if (i != index && animations[i].name.equals(animationName)) {
				index = i;
				setFrame(0);
				finished = false;
				gameObject.renderer.sprite = animations[index].frames[frame];
				if (animations[index].listener != null) animations[index].listener.onAnimatedStarted(this);
				break;
			}
		}
	}

	public Animation getCurrentAnimation() {
		if (index == -1) return null;
		else return animations[index];
	}

	public int getCurrentAnimationId() {
		return index;
	}

	public void setCurrentAnimationId(int animationId) {
		index = animationId;
	}

	public int getCurrentAnimationFrame() {
		return frame == -1 ? 0 : frame;
	}

	public void setCurrentAnimationFrame(int frame) {
		setFrame(frame);
	}

	public boolean isFinished() {
		return finished;
	}

	public Animation getAnimation(String name) {
		for (Animation animation : animations) {
			if (animation.name.equals(name)) {
				return animation;
			}
		}
		return null;
	}

	@Override
	public Iterator<Animation> iterator() {
		return new ObjectIterator<>(animations);
	}
}
