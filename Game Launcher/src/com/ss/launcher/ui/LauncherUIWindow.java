package com.ss.launcher.ui;

import static javafx.geometry.Pos.BOTTOM_LEFT;
import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.geometry.Pos.CENTER_RIGHT;
import static javafx.scene.paint.Color.TRANSPARENT;

import java.awt.Point;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
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

	private static final String CSS_CLASS_TITLE_VERSION_TEXT = "title-version-text";
	private static final String CSS_CLASS_WINDOW_BACKGROUND = "window-background";
	private static final String CSS_CLASS_TITLE_TEXT = "title-text";

	private static final String CSS_ID_CLOSE_BUTTON = "CloseButton";

	private static final Point PROP_TITLE_CONTAINER_SIZE = new Point(780, 52);

	private static final Insets PROP_VERSION_LABEL_OFFSET = new Insets(0, 0, 0, 4);
	private static final Insets PROP_CLOSE_BUTTON_OFFSET = new Insets(0, 5, 0, 0);

	public static final int WINDOW_WIDTH = 800;
	public static final int WINDOW_HEIGHT = 544;

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

		return scene;
	}

	@Override
	protected Pane createRoot() {
		final Pane root = super.createRoot();
		FXUtils.addClassTo(root, CSS_CLASS_WINDOW_BACKGROUND);
		return root;
	}

	@Override
	protected Button createCloseButton() {

		final Button button = new Button("X");
		button.setId(CSS_ID_CLOSE_BUTTON);
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

		HBox titleContainer = new HBox();
		titleContainer.setAlignment(CENTER_LEFT);

		Label titleLabel = new Label("SPACESHIFT");
		titleLabel.setTextAlignment(TextAlignment.LEFT);
		titleLabel.setAlignment(BOTTOM_LEFT);

		Label versionLabel = new Label("ALPHA");
		versionLabel.setTextAlignment(TextAlignment.LEFT);
		versionLabel.setAlignment(BOTTOM_LEFT);

		FXUtils.setFixedSize(titleContainer, PROP_TITLE_CONTAINER_SIZE);
		FXUtils.addClassTo(titleLabel, CSS_CLASS_TITLE_TEXT);
		FXUtils.addClassTo(versionLabel, CSS_CLASS_TITLE_VERSION_TEXT);

		FXUtils.bindFixedHeight(versionLabel, titleLabel.heightProperty().subtract(4));

		FXUtils.addToPane(titleLabel, titleContainer);
		FXUtils.addToPane(versionLabel, titleContainer);
		FXUtils.addToPane(titleContainer, header);

		HBox.setMargin(versionLabel, PROP_VERSION_LABEL_OFFSET);

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
