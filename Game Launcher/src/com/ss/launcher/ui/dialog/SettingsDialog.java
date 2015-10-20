package com.ss.launcher.ui.dialog;

import com.ss.launcher.Config;
import com.ss.launcher.Messages;
import com.ss.launcher.util.LauncherUtils;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import javafx.stage.DirectoryChooser;
import rlib.ui.hanlder.WindowDragHandler;
import rlib.ui.util.FXUtils;
import rlib.ui.window.popup.dialog.AbstractPopupDialog;
import rlib.util.StringUtils;

import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

import static com.ss.launcher.Messages.*;
import static com.ss.launcher.ui.LauncherUIWindow.CSS_ID_CLOSE_BUTTON;
import static javafx.geometry.Pos.*;
import static javafx.scene.Cursor.HAND;

/**
 * Реализация диалога с настройками лаунчера.
 *
 * @author Ronn
 */
public class SettingsDialog extends AbstractPopupDialog {

    public static final String CSS_SETTINGS_BUTTON_OPEN = "settings-button-open";
    public static final String CSS_SETTING_TEXT_FIELD = "setting-text-field";
    public static final String CSS_SETTING_LABEL = "setting-label";
    public static final String CSS_DIALOG_BACKGROUND = "dialog-background";
    public static final String CSS_DIALOG_HEADER_BACKGROUND = "dialog-header-background";
    public static final String CSS_TITLE_DIALOG_TEXT = "title-dialog-text";
    public static final String CSS_MAIN_BUTTON = "main-button";

    public static final Insets SETTINGS_CONTAINER_OFFSET = new Insets(20, 0, 0, 0);
    public static final Insets APPLY_BUTTON_OFFSET = new Insets(15, 0, 0, 0);

    public static final Point PROP_BUTTON_APPLY_SIZE = new Point(120, 32);
    public static final Point PROP_OPEN_BUTTON_SIZE = new Point(24, 24);
    public static final Point PROP_DIALOG_SIZE = new Point(600, 200);

    public static final String FOLDER_SPACE_SHIFT_CLIENT = "SpaceShiftClient";

    public static final Point PROP_TITLE_CONTAINER_SIZE = new Point(598, 32);

    public static final int PROP_FIELD_HEIGHT = 24;
    public static final int PROP_FIELD_WIDTH = 340;
    public static final int PROP_LABEL_WIDTH = 140;

    /**
     * Кнопка для смены папки с клиентом.
     */
    private Button changeFolderButton;

    /**
     * Кнопка приминения изминений.
     */
    private Button applyButton;

    /**
     * Надпись о папке с игрой.
     */
    private Label gameFolderLabel;

    /**
     * Надпись о хосте для прокси.
     */
    private Label httpProxyHostLabel;

    /**
     * Надпись о порте для прокси.
     */
    private Label httpProxyPortLabel;

    /**
     * Поле для изминения папки с клиентом.
     */
    private TextField gameFolderField;

    /**
     * Поле для указания хоста прокси.
     */
    private TextField httpProxyHostField;

    /**
     * Поле для изминения порта прокси.
     */
    private TextField httpProxyPortField;

    /**
     * Построения кнопки для внесения изминений.
     */
    protected void createApplyButton(VBox root) {

        applyButton = new Button();
        applyButton.setText(SETTINGS_DIALOG_BUTTON_APPLY);
        applyButton.setCursor(HAND);
        applyButton.setOnAction(event -> processApply());

        FXUtils.setFixedSize(applyButton, PROP_BUTTON_APPLY_SIZE);
        FXUtils.addClassTo(applyButton, CSS_MAIN_BUTTON);
        FXUtils.addToPane(applyButton, root);

        VBox.setMargin(applyButton, APPLY_BUTTON_OFFSET);
    }

    @Override
    protected void createControls(VBox root) {
        root.setAlignment(TOP_CENTER);

        FXUtils.addClassTo(root, CSS_DIALOG_BACKGROUND);

        createHeader(root);
        createSettings(root);
        createApplyButton(root);

        init();
    }

    /**
     * Построение шапки диалога настроек.
     */
    protected void createHeader(VBox root) {

        final StackPane header = new StackPane();
        header.setAlignment(CENTER_RIGHT);

        HBox titleContainer = new HBox();
        titleContainer.setAlignment(CENTER_LEFT);
        titleContainer.setPickOnBounds(false);

        Label titleLabel = new Label(SETTINGS_DIALOG_TITLE);
        titleLabel.setTextAlignment(TextAlignment.LEFT);
        titleLabel.setAlignment(BOTTOM_LEFT);

        final Button closeButton = new Button();
        closeButton.setId(CSS_ID_CLOSE_BUTTON);
        closeButton.setOnAction(event -> hide());
        closeButton.setCursor(HAND);

        FXUtils.setFixedSize(titleContainer, PROP_TITLE_CONTAINER_SIZE);
        FXUtils.addClassTo(titleLabel, CSS_TITLE_DIALOG_TEXT);
        FXUtils.addClassTo(header, CSS_DIALOG_HEADER_BACKGROUND);

        FXUtils.addToPane(titleLabel, titleContainer);
        FXUtils.addToPane(closeButton, header);
        FXUtils.addToPane(titleContainer, header);

        WindowDragHandler.install(header);

        FXUtils.setFixedSize(closeButton, new Point(11, 11));

        HBox.setMargin(titleLabel, new Insets(0, 0, 0, 10));
        StackPane.setMargin(closeButton, new Insets(0, 10, 0, 0));

        FXUtils.addToPane(header, root);
    }

    /**
     * Построение элементов настроек.
     */
    protected void createSettings(VBox root) {

        GridPane container = new GridPane();
        container.setAlignment(CENTER);
        container.setVgap(4);
        container.setHgap(4);

        gameFolderLabel = new Label(SETTINGS_DIALOG_LABEL_GAME_FOLDER);
        gameFolderLabel.setAlignment(CENTER_RIGHT);
        gameFolderLabel.setTextAlignment(TextAlignment.RIGHT);

        httpProxyHostLabel = new Label(SETTINGS_DIALOG_LABEL_PROXY_HOST);
        httpProxyHostLabel.setAlignment(CENTER_RIGHT);
        httpProxyHostLabel.setTextAlignment(TextAlignment.RIGHT);

        httpProxyPortLabel = new Label(SETTINGS_DIALOG_LABEL_PROXY_PORT);
        httpProxyPortLabel.setAlignment(CENTER_RIGHT);
        httpProxyPortLabel.setTextAlignment(TextAlignment.RIGHT);

        gameFolderField = new TextField();
        httpProxyHostField = new TextField();
        httpProxyPortField = new TextField();

        changeFolderButton = new Button();
        changeFolderButton.setOnAction(event -> processChangeFolder());
        changeFolderButton.setCursor(HAND);

        container.add(gameFolderLabel, 0, 0);
        container.add(gameFolderField, 1, 0);
        container.add(changeFolderButton, 2, 0);

        container.add(httpProxyHostLabel, 0, 1);
        container.add(httpProxyHostField, 1, 1);

        container.add(httpProxyPortLabel, 0, 2);
        container.add(httpProxyPortField, 1, 2);

        FXUtils.addClassTo(gameFolderLabel, CSS_SETTING_LABEL);
        FXUtils.addClassTo(httpProxyHostLabel, CSS_SETTING_LABEL);
        FXUtils.addClassTo(httpProxyPortLabel, CSS_SETTING_LABEL);
        FXUtils.addClassTo(gameFolderField, CSS_SETTING_TEXT_FIELD);
        FXUtils.addClassTo(httpProxyHostField, CSS_SETTING_TEXT_FIELD);
        FXUtils.addClassTo(httpProxyPortField, CSS_SETTING_TEXT_FIELD);
        FXUtils.addClassTo(changeFolderButton, CSS_MAIN_BUTTON);
        FXUtils.addClassTo(changeFolderButton, CSS_SETTINGS_BUTTON_OPEN);

        FXUtils.setFixedWidth(gameFolderLabel, PROP_LABEL_WIDTH);
        FXUtils.setFixedWidth(httpProxyHostLabel, PROP_LABEL_WIDTH);
        FXUtils.setFixedWidth(httpProxyPortLabel, PROP_LABEL_WIDTH);
        FXUtils.setFixedWidth(gameFolderField, PROP_FIELD_WIDTH);
        FXUtils.setFixedWidth(httpProxyHostField, PROP_FIELD_WIDTH);
        FXUtils.setFixedWidth(httpProxyPortField, PROP_FIELD_WIDTH);
        FXUtils.setFixedHeight(gameFolderField, PROP_FIELD_HEIGHT);
        FXUtils.setFixedHeight(httpProxyHostField, PROP_FIELD_HEIGHT);
        FXUtils.setFixedHeight(httpProxyPortField, PROP_FIELD_HEIGHT);
        FXUtils.setFixedSize(changeFolderButton, PROP_OPEN_BUTTON_SIZE);

        FXUtils.addToPane(container, root);

        VBox.setMargin(container, SETTINGS_CONTAINER_OFFSET);
    }

    /**
     * @return поле для изминения папки с клиентом.
     */
    private TextField getGameFolderField() {
        return gameFolderField;
    }

    /**
     * @return поле для указания хоста прокси.
     */
    private TextField getHttpProxyHostField() {
        return httpProxyHostField;
    }

    /**
     * @return поле для изминения порта прокси.
     */
    private TextField getHttpProxyPortField() {
        return httpProxyPortField;
    }

    @Override
    protected Point getSize() {
        return PROP_DIALOG_SIZE;
    }

    /**
     * Инициализация текущих настроек.
     */
    private void init() {

        final Path gameFolder = LauncherUtils.getGameFolder();

        final TextField gameFolderField = getGameFolderField();
        gameFolderField.setText(gameFolder.toString());

        final TextField httpProxyHostField = getHttpProxyHostField();
        httpProxyHostField.setText(Config.httpProxyHost);

        final TextField httpProxyPortField = getHttpProxyPortField();
        httpProxyPortField.setText(Config.httpProxyPort);
    }

    /**
     * Приминение изминений если они есть.
     */
    private void processApply() {

        int change = 0;

        final TextField gameFolderField = getGameFolderField();
        final TextField httpProxyHostField = getHttpProxyHostField();
        final TextField httpProxyPortField = getHttpProxyPortField();

        String prevGameFolder = Config.gameFolder;
        String currentGameFolder = gameFolderField.getText();

        if (!StringUtils.equals(prevGameFolder, currentGameFolder)) {

            Path folder = Paths.get(currentGameFolder);

            if (!FOLDER_SPACE_SHIFT_CLIENT.equals(folder.getFileName())) {
                folder = Paths.get(folder.toString(), FOLDER_SPACE_SHIFT_CLIENT);
            }

            currentGameFolder = folder.toString();

            if (!StringUtils.equals(prevGameFolder, currentGameFolder)) {
                Config.gameFolder = currentGameFolder;
                change++;
            }
        }

        if (!StringUtils.equals(Config.httpProxyHost, httpProxyHostField.getText())) {
            Config.httpProxyHost = httpProxyHostField.getText();
            change++;
        }

        if (!StringUtils.equals(Config.httpProxyPort, httpProxyPortField.getText())) {
            Config.httpProxyPort = httpProxyPortField.getText();
            change++;
        }

        if (change < 1) {
            return;
        }

        Config.save();

        final Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(Messages.ALERT_INFO_TITLE);
        alert.setHeaderText(ALERT_INFO_HEADER_TEXT_NEED_RESTART);
        alert.initOwner(this);
        alert.showAndWait();

        System.exit(0);
    }

    /**
     * Процесс смены папки с игрой.
     */
    private void processChangeFolder() {

        final Scene scene = getScene();

        final DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(DIRECTORY_CHOOSER_TITLE);

        final String gameFolder = Config.gameFolder;

        if (gameFolder == null) {
            chooser.setInitialDirectory(new File(System.getProperty("user.home")));
        } else {
            chooser.setInitialDirectory(new File(gameFolder));
        }

        File folder = chooser.showDialog(scene.getWindow());

        if (folder == null) {
            return;
        }

        final TextField gameFolderField = getGameFolderField();
        gameFolderField.setText(folder.getAbsolutePath());
    }
}
