package com.ss.launcher.ui;

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

import java.awt.*;

import static com.ss.launcher.Messages.WINDWOW_TITLE;
import static com.ss.launcher.Messages.WINDWOW_TITLE_VERSION;
import static javafx.geometry.Pos.*;
import static javafx.scene.Cursor.HAND;
import static javafx.scene.paint.Color.TRANSPARENT;

/**
 * Реализация окна лаунчера.
 *
 * @author Ronn
 */
public class LauncherUIWindow extends UndecoratedUIWindow {

    public static final String CSS_CLASS_TITLE_VERSION_TEXT = "title-version-text";
    public static final String CSS_CLASS_WINDOW_BACKGROUND = "window-background";
    public static final String CSS_CLASS_TITLE_TEXT = "title-text";

    public static final String CSS_ID_CLOSE_BUTTON = "CloseButton";

    public static final Point PROP_TITLE_CONTAINER_SIZE = new Point(780, 52);

    public static final Insets PROP_VERSION_LABEL_OFFSET = new Insets(0, 0, 0, 4);
    public static final Insets PROP_CLOSE_BUTTON_OFFSET = new Insets(0, 20, 0, 0);

    public static final int WINDOW_WIDTH = 800;
    public static final int WINDOW_HEIGHT = 544;

    public LauncherUIWindow(Stage stage, Array<Class<? extends UIPage>> availablePages) {
        super(stage, availablePages);
    }

    @Override
    protected void applyMarginToCloseButton(Button closeButton) {
        StackPane.setMargin(closeButton, PROP_CLOSE_BUTTON_OFFSET);
    }

    @Override
    public void close() {
        super.close();
        System.exit(0);
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

    @Override
    protected Button createCloseButton() {

        final Button button = new Button();
        button.setId(CSS_ID_CLOSE_BUTTON);
        button.setOnAction(event -> close());
        button.setCursor(HAND);

        FXUtils.setFixedSize(button, new Point(11, 11));

        return button;
    }

    @Override
    protected Pane createHeader() {

        final StackPane header = new StackPane();
        header.setAlignment(CENTER_RIGHT);

        HBox titleContainer = new HBox();
        titleContainer.setAlignment(CENTER_LEFT);

        Label titleLabel = new Label(WINDWOW_TITLE);
        titleLabel.setTextAlignment(TextAlignment.LEFT);
        titleLabel.setAlignment(BOTTOM_LEFT);

        Label versionLabel = new Label(WINDWOW_TITLE_VERSION);
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
    protected Pane createRoot() {
        final Pane root = super.createRoot();
        FXUtils.addClassTo(root, CSS_CLASS_WINDOW_BACKGROUND);
        return root;
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
}
