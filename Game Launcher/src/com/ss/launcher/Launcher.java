package com.ss.launcher;

import com.ss.launcher.file.engine.FileEngine;
import com.ss.launcher.file.engine.FileEngineManager;
import com.ss.launcher.ui.LauncherUIWindow;
import com.ss.launcher.ui.page.MainUIPage;
import javafx.application.Application;
import javafx.application.HostServices;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import rlib.logging.LoggerLevel;
import rlib.ui.page.UIPage;
import rlib.ui.window.UIWindow;
import rlib.util.StringUtils;
import rlib.util.array.Array;
import rlib.util.array.ArrayFactory;

import java.util.Optional;

import static com.ss.launcher.Messages.ALERT_INFO_HEADER_TEXT_NEED_UPDATE_LAUNCHER;
import static javafx.application.Platform.runLater;

/**
 * Стартовый класс лаунчера.
 *
 * @author Ronn
 */
public class Launcher extends Application {

    public static final String LINUX_ICON = "/com/ss/launcher/resources/icons/SpaceShiftLauncher.png";

    public static final String PROP_STYLE = "/com/ss/launcher/resources/css/style.css";

    private static final Array<Class<? extends UIPage>> AVAILABLE_PAGE = ArrayFactory.newArray(Class.class);

    private static Launcher instance;

    static {
        AVAILABLE_PAGE.add(MainUIPage.class);
    }

    public static Launcher getInstance() {
        return instance;
    }

    public static void main(String[] args) {
        Config.initPreferences();
        launch(args);
    }

    /**
     * Окно лаунчера.
     */
    private volatile UIWindow window;

    /**
     * Проверка наличия обновленного лаунчера.
     */
    private void checkUpdate() {
        final ExecutorManager executorManager = ExecutorManager.getInstance();
        executorManager.async(this::checkVersion);
    }

    /**
     * Проверка версии лаунчера.
     */
    private void checkVersion() {

        try {

            final FileEngine fileEngine = FileEngineManager.get(Config.fileEngine);
            final String lastVersion = fileEngine.getContent(Config.fileLauncherLastVersionUrl);

            if (StringUtils.equals(lastVersion, Config.CURRENT_VERSION)) {
                return;
            }

        } catch (Exception e) {
            return;
        }

        runLater(this::showOutdatedVersion);
    }

    /**
     * Отображения сообщения об устаревшей версии.
     */
    private void showOutdatedVersion() {

        final Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle(Messages.ALERT_INFO_TITLE);
        alert.setHeaderText(ALERT_INFO_HEADER_TEXT_NEED_UPDATE_LAUNCHER);

        final Optional<ButtonType> buttonType = alert.showAndWait();

        if (buttonType.get() == ButtonType.OK) {
            final HostServices hostServices = getHostServices();
            hostServices.showDocument(Config.updateLauncherUrl);
        }
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        instance = this;

        LoggerLevel.INFO.setEnabled(true);
        LoggerLevel.DEBUG.setEnabled(false);

        Config.initConfig();
        FileEngineManager.init();

        checkUpdate();

        final ObservableList<Image> icons = primaryStage.getIcons();
        icons.add(new Image(LINUX_ICON));

        window = new LauncherUIWindow(primaryStage, AVAILABLE_PAGE);
        window.setTitle("SpaceShift Launcher");
        window.loadStylesheets(PROP_STYLE);
        window.showPage(MainUIPage.class);

        primaryStage.setOnHidden(event -> System.exit(0));
    }
}
