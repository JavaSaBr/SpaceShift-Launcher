package com.ss.launcher.ui.page;

import static javafx.geometry.Pos.TOP_CENTER;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import rlib.ui.page.impl.AbstractUIPage;
import rlib.ui.window.UIWindow;

/**
 * Реализация основной страницы лаунчера.
 * 
 * @author Ronn
 */
public class MainUIPage extends AbstractUIPage {

	private VBox root;

	@Override
	public void postPageShow(UIWindow window) {
		super.postPageShow(window);

		window.setSize(1000, 600);
		window.setRezisable(true);
	}

	@Override
	protected Pane createRoot() {

		root = new VBox();
		root.setAlignment(TOP_CENTER);

		return root;
	}
}
