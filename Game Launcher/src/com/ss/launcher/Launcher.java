package com.ss.launcher;

import javafx.application.Application;
import javafx.stage.Stage;
import rlib.ui.page.UIPage;
import rlib.ui.window.UIWindow;
import rlib.util.array.Array;
import rlib.util.array.ArrayFactory;

import com.ss.launcher.ui.LauncherUIWindow;
import com.ss.launcher.ui.page.MainUIPage;

public class Launcher extends Application {

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
		window = new LauncherUIWindow(primaryStage, AVAILABLE_PAGE);
		window.setTitle("SayoFXTool");
		window.loadStylesheets("/ui/fx/css/base.css");
		window.loadStylesheets("/ui/fx/css/custom_ids.css");
		window.showPage(MainUIPage.class);
	}
}
