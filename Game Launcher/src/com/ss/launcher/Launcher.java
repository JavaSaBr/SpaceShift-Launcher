package com.ss.launcher;

import javafx.application.Application;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import rlib.ui.page.UIPage;
import rlib.ui.window.UIWindow;
import rlib.util.array.Array;
import rlib.util.array.ArrayFactory;

import com.ss.launcher.file.engine.FileEngineManager;
import com.ss.launcher.ui.LauncherUIWindow;
import com.ss.launcher.ui.page.MainUIPage;

/**
 * Стартовый класс лаунчера.
 * 
 * @author Ronn
 */
public class Launcher extends Application {

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
		Config.init();
		FileEngineManager.init();
		launch(args);
	}

	/** окно лаунчера */
	private volatile UIWindow window;

	@Override
	public void start(Stage primaryStage) throws Exception {

		final ObservableList<Image> icons = primaryStage.getIcons();
		icons.add(new Image(LINUX_ICON));

		instance = this;
		window = new LauncherUIWindow(primaryStage, AVAILABLE_PAGE);
		window.setTitle("SpaceShift Launcher");
		window.loadStylesheets(PROP_STYLE);
		window.showPage(MainUIPage.class);
	}
}
