package com.ezardlabs.dethsquare.util;

import com.ezardlabs.dethsquare.util.Dethsquare.Platform;

public abstract class Launcher implements GameListeners {

	protected void init() {
		Dethsquare.init(getPlatform(), getAudio(), getIO(), getPrefs(), getRender());
	}

	protected abstract Platform getPlatform();

	protected abstract AudioUtils getAudio();

	protected abstract IOUtils getIO();

	protected abstract PrefUtils getPrefs();

	protected abstract RenderUtils getRender();

	public abstract void launch(BaseGame game);

	protected final void update() {
		if (!updateListeners.isEmpty()) {
			for (Object o : updateListeners.toArray()) {
				((UpdateListener) o).onUpdate();
			}
		}
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
