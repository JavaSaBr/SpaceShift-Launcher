package com.ss.launcher.ui;

import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Pos.CENTER_RIGHT;
import static javafx.scene.paint.Color.TRANSPARENT;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import rlib.ui.hanlder.WindowDragHandler;
import rlib.ui.page.UIPage;
import rlib.ui.util.FXUtils;
import rlib.ui.window.UIWindow;
import rlib.ui.window.impl.UndecoratedUIWindow;
import rlib.util.array.Array;

/**
 * Реализация окна лаунчера.
 * 
 * @author Ronn
 */
public class LauncherUIWindow extends UndecoratedUIWindow {

	public static final String CSS_FILE_BASE = "/ui/fx/css/base.css";
	public static final String CSS_FILE_EXTERNAL = "/ui/fx/css/external.css";
	public static final String CSS_FILE_CUSTOM_IDS = "/ui/fx/css/custom_ids.css";
	public static final String CSS_FILE_CUSTOM_CLASSES = "/ui/fx/css/custom_classes.css";

	private static final Insets PROP_CLOSE_BUTTON_OFFSET = new Insets(4, 40, 0, 0);
	private static final Insets PROP_TITLE_OFFSET = new Insets(10, 0, 0, 0);

	public static final int WINDOW_WIDTH = 1000;
	public static final int WINDOW_HEIGHT = 500;

	public LauncherUIWindow(Stage stage, Array<Class<? extends UIPage>> availablePages) {
		super(stage, availablePages);
	}

	@Override
	protected Stage configureStage(Stage stage) {
		stage.initStyle(StageStyle.TRANSPARENT);
		stage.setResizable(false);
		stage.setMaxHeight(WINDOW_HEIGHT);
		stage.setMinHeight(WINDOW_HEIGHT);
		stage.setMaxWidth(WINDOW_WIDTH);
		stage.setMinWidth(WINDOW_WIDTH);
		return stage;
	}

	/**
	 * Создание и определение сцены {@link UIWindow}.
	 */
	@Override
	protected Scene createdScene() {

		final Scene scene = new Scene(getRootNode(), WINDOW_WIDTH, WINDOW_HEIGHT, Color.TRANSPARENT);
		scene.setFill(TRANSPARENT);

		final ObservableList<String> stylesheets = scene.getStylesheets();
		stylesheets.add(CSS_FILE_BASE);
		stylesheets.add(CSS_FILE_EXTERNAL);
		stylesheets.add(CSS_FILE_CUSTOM_IDS);
		stylesheets.add(CSS_FILE_CUSTOM_CLASSES);

		return scene;
	}

	@Override
	protected Pane createRoot() {

		final Pane root = super.createRoot();
		root.setId("BlackBlueGradientBackground");

		return root;
	}

	@Override
	protected Button createCloseButton() {

		final Button button = new Button("X");
		button.setId("GameDraggablePanelButtonClose");
		button.setOnAction(event -> close());

		return button;
	}

	@Override
	protected void applyMarginToCloseButton(Button closeButton) {
		StackPane.setMargin(closeButton, PROP_CLOSE_BUTTON_OFFSET);
	}

	@Override
	protected Pane createHeader() {

		final StackPane header = new StackPane();
		header.setAlignment(CENTER_RIGHT);

		Label titleLabel = new Label("SpaceShift Launcher");
		titleLabel.setTextAlignment(TextAlignment.CENTER);
		titleLabel.setAlignment(CENTER);

		FXUtils.bindFixedWidth(titleLabel, header.widthProperty());

		FXUtils.addToPane(titleLabel, header);
		FXUtils.addClassTo(titleLabel, "arial-label-20-bold-with-shadow");

		StackPane.setMargin(titleLabel, PROP_TITLE_OFFSET);

		WindowDragHandler.install(header);

		return header;
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
