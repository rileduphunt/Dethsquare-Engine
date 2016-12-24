package com.ezardlabs.dethsquare.util;

import com.ezardlabs.dethsquare.util.Utils.Platform;

public abstract class Launcher implements GameListeners {

	protected Launcher() {
		init();
	}

	protected void init() {
		Utils.init(getPlatform(), getAudio(), getIO(), getPrefs(), getRender());
	}

	protected abstract Platform getPlatform();

	protected abstract AudioUtils getAudio();

	protected abstract IOUtils getIO();

	protected abstract PrefUtils getPrefs();

	protected abstract RenderUtils getRender();

	public abstract void launch(BaseGame game);

	protected final void update() {
		updateListeners.forEach(UpdateListener::onUpdate);
	}

	protected final void render() {
		renderListeners.forEach(RenderListener::onRender);
	}

	protected final void onResize(int width, int height) {
		screenSize.width = width;
		screenSize.height = height;
		for (ResizeListener resizeListener : resizeListeners) {
			resizeListener.onResize(width, height);
		}
	}
}
