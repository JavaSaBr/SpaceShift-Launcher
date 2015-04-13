package com.ss.launcher.ui;

import static com.ss.client.fx.ui.css.CSSIds.BLACK_BLUE_GRADIENT_BACKGROUND;
import static com.ss.client.fx.ui.css.CSSIds.GAME_DRAGGABLE_PANEL_BUTTON_CLOSE;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import rlib.ui.page.UIPage;
import rlib.ui.window.impl.UndecoratedUIWindow;
import rlib.util.array.Array;

/**
 * Реализация окна лаунчера.
 * 
 * @author Ronn
 */
public class LauncherUIWindow extends UndecoratedUIWindow {

	public LauncherUIWindow(Stage stage, Array<Class<? extends UIPage>> availablePages) {
		super(stage, availablePages);
	}

	@Override
	protected Pane createRoot() {
		final Pane root = super.createRoot();
		root.setId(BLACK_BLUE_GRADIENT_BACKGROUND);
		return root;
	}

	@Override
	protected Button createCloseButton() {

		final Button button = new Button();
		button.setId(GAME_DRAGGABLE_PANEL_BUTTON_CLOSE);
		button.setOnAction(event -> close());

		return button;
	}

	@Override
	protected Button createMiniminizeButton() {
		return null;
	}

	@Override
	public void close() {
		super.close();
		System.exit(0);
	}
}
