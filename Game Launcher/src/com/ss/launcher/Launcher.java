package com.ss.launcher;

import static com.ss.launcher.Messages.ALERT_INFO_HEADER_TEXT_NEED_UPDATE_LAUNCHER;
import static javafx.application.Platform.runLater;

import java.util.Optional;

import javafx.application.Application;
import javafx.application.HostServices;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import rlib.ui.page.UIPage;
import rlib.ui.window.UIWindow;
import rlib.util.StringUtils;
import rlib.util.array.Array;
import rlib.util.array.ArrayFactory;

import com.ss.launcher.file.engine.FileEngine;
import com.ss.launcher.file.engine.FileEngineManager;
import com.ss.launcher.ui.LauncherUIWindow;
import com.ss.launcher.ui.page.MainUIPage;

/**
 * Стартовый класс лаунчера.
 * 
 * @author Ronn
 */
public class Launcher extends Application {

	public static final String LAUNCHER_HOST = "http://spaceshift.ru/forum/index.php?topic=27328.0";

	public static final String LINUX_ICON = "/com/ss/launcher/resources/icons/SpaceShiftLauncher.png";

	public static final String PROP_STYLE = "/com/ss/launcher/resources/css/style.css";

	private static final Array<Class<? extends UIPage>> AVAILABLE_PAGE = ArrayFactory.newArray(Class.class);

	static {
		AVAILABLE_PAGE.add(MainUIPage.class);
	}

	private static Launcher instance;

	public static Launcher getInstance() {
		return instance;
	}

	public static void main(String[] args) {
		launch(args);
	}

	/** окно лаунчера */
	private volatile UIWindow window;

	@Override
	public void start(Stage primaryStage) throws Exception {
		instance = this;

		Config.init();
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

	/**
	 * Проверка наличия обновленного лаунчераю
	 */
	private void checkUpdate() {

		final ExecutorManager executorManager = ExecutorManager.getInstance();
		executorManager.async(() -> {

			try {

				final FileEngine fileEngine = FileEngineManager.get(Config.FILE_ENGINE);
				final String lastVersion = fileEngine.getContent(Config.FILE_LAUNCHER_LAST_VERSION_URL);

				if(StringUtils.equals(lastVersion, Config.CURRENT_VERSION)) {
					return;
				}

			} catch(Exception e) {
				return;
			}

			runLater(() -> {

				final Alert alert = new Alert(AlertType.CONFIRMATION);
				alert.setTitle(Messages.ALERT_INFO_TITLE);
				alert.setHeaderText(ALERT_INFO_HEADER_TEXT_NEED_UPDATE_LAUNCHER);

				final Optional<ButtonType> buttonType = alert.showAndWait();

				if(buttonType.get() == ButtonType.OK) {
					final HostServices hostServices = getHostServices();
					hostServices.showDocument(LAUNCHER_HOST);
				}
			});
		});
	}
}
