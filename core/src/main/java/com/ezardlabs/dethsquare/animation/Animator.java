package com.ezardlabs.dethsquare.animation;

import com.ezardlabs.dethsquare.Component.RequiredComponents;
import com.ezardlabs.dethsquare.Renderer;
import com.ezardlabs.dethsquare.Script;

import java.util.Iterator;

@RequiredComponents(Renderer.class)
public final class Animator extends Script implements Iterable<Animation> {
	private Animation[] animations;
	private int index = -1;
	private int frame = -2;
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
		if (frame != this.frame) {
			this.frame = frame;
			if (frame != -1) {
				this.nextFrameTime = System.currentTimeMillis() + animations[index].frameData[frame].getDuration();
				setRendererData(animations[index], frame);
//				gameObject.renderer.setSprite(animations[index].frames[frame]);
//				gameObject.renderer.setData(animations[index].frameData[frame]);
			}
		}
	}

	private void setRendererData(Animation animation, int frame) {
		gameObject.renderer.setSprite(animation.frames[frame]);
		gameObject.renderer.setSize(animation.frameData[frame].getWidth(), animation.frameData[frame].getHeight());
		gameObject.renderer.setOffsets(animation.frameData[frame].getOffset(transform.scale.x));
	}

	public void update() {
		if (!shouldUpdate) {
			if (index == -1 || frame == -1) return;
			return;
		}
		int startFrame = frame;
		if (index == -1 || frame == -1) return;
		int tempFrame;
		if (System.currentTimeMillis() >= nextFrameTime) {
			nextFrameTime += animations[index].frameData[frame].getDuration();
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
		} else {
			tempFrame = frame;
		}
		if (tempFrame != startFrame && animations[index].listener != null) {
			animations[index].listener.onFrame(this, tempFrame);
		}
	}

	private void resetAndStart(int i) {
		index = i;
		frame = -2;
		setFrame(0);
		finished = false;
		if (animations[index].listener != null) animations[index].listener.onAnimatedStarted(this);
	}

	public void play(String animationName) {
		play(animationName, false);
	}

	public void play(String animationName, boolean refresh) {
		if (refresh && index > -1) {
			if (animations[index].name.equals(animationName)) {
				if (finished) {
					resetAndStart(index);
					return;
				} else {
					return;
				}
			}
		}

		for (int i = 0; i < animations.length; i++) {
			if (i != index && animations[i].name.equals(animationName)) {
				resetAndStart(i);
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
