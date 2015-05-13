package com.ss.launcher.ui.page;

import static com.ss.launcher.util.LauncherUtils.getCurrentVersion;
import static com.ss.launcher.util.LauncherUtils.isNeedUpdate;
import static javafx.application.Platform.runLater;
import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.geometry.Pos.TOP_CENTER;

import java.awt.Point;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.application.HostServices;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;
import javafx.geometry.Insets;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.DirectoryChooser;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventListener;
import org.w3c.dom.events.EventTarget;

import rlib.ui.page.impl.AbstractUIPage;
import rlib.ui.util.FXUtils;
import rlib.ui.window.UIWindow;

import com.ss.launcher.Config;
import com.ss.launcher.ExecutorManager;
import com.ss.launcher.Launcher;
import com.ss.launcher.exception.IncorrectJavaException;
import com.ss.launcher.exception.NotFoundClientException;
import com.ss.launcher.tasks.DownloadClientTask;
import com.ss.launcher.tasks.UpdateClientTask;
import com.ss.launcher.util.LauncherUtils;

/**
 * Реализация основной страницы лаунчера.
 * 
 * @author Ronn
 */
public class MainUIPage extends AbstractUIPage {

	public static final String FOLDER_SPACE_SHIFT_CLIENT = "SpaceShiftClient";

	public static final String PROP_DEFAULT_HTML = "/com/ss/launcher/resources/welcome.html";
	public static final String PROP_INDEX_HTML = "http://spaceshift.ru/upd/index.html";

	public static final String EVENT_TYPE_CLICK = "click";
	public static final String EVENT_TYPE_MOUSEOVER = "mouseover";
	public static final String EVENT_TYPE_MOUSEOUT = "mouseclick";

	public static final Point PROP_MAIN_BUTTON_SIZE = new Point(140, 34);

	public static final Insets PROP_LINE_OFFSET = new Insets(10, 0, 10, 0);

	public static final int PROP_WEB_VIEW_HEIGHT = 432;
	public static final int PROP_WEB_VIEW_WIDTH = 798;

	public static final int PROP_BROWSER_HEIGHT = 768;
	public static final int PROP_BROWSER_WIDTH = 1300;

	private static final EventListener LINK_EVENT_HANDLER = event -> {

		final String eventType = event.getType();

		if(eventType.equals(EVENT_TYPE_CLICK)) {

			final String href = ((Element) event.getTarget()).getAttribute("href");

			final Launcher launcher = Launcher.getInstance();
			final HostServices hostServices = launcher.getHostServices();
			hostServices.showDocument(href);
		}
	};

	/** рут страницы */
	private VBox root;
	/** контейнер текста с вопросом */
	private HBox gameFolderContainer;
	/** контейнер прогресса обновления */
	private StackPane progressContainer;

	/** прогресс бар */
	private ProgressBar progressBar;
	/** статус прогресс бара */
	private Label progressBarStatus;
	/** линк для выбора папки с игрой */
	private Label openChooserLabel;
	/** вопрос о наличии уже игры */
	private Label questionLabel;

	/** главная кнопка */
	private Button mainButton;

	private WebView webView;

	/**
	 * Проверка необходимости обновления клиента.
	 */
	protected void checkUpdate() {

		final Button mainButton = getMainButton();
		final Label progressBarStatus = getProgressBarStatus();
		final ProgressBar progressBar = getProgressBar();

		try {

			if(!isNeedUpdate()) {
				runLater(() -> {

					mainButton.setText("Играть");
					mainButton.setOnAction(event -> processPlay());
					mainButton.setDisable(false);

					progressBar.setVisible(false);
					progressBarStatus.setText("Текущая версия клиента " + getCurrentVersion());
				});
			} else {
				runLater(() -> {

					mainButton.setText("Обновить");
					mainButton.setOnAction(event -> processUpdate());
					mainButton.setDisable(false);

					progressBar.setVisible(false);
					progressBarStatus.setText("Требуется обновление клиента");
				});
			}

		} catch(final Exception e) {
			runLater(() -> {
				final Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText(e.getLocalizedMessage());
				alert.setTitle("Ошибка");
				alert.showAndWait();
			});
		}
	}

	@Override
	protected Pane createRoot() {

		root = new VBox();
		root.setAlignment(TOP_CENTER);

		webView = new WebView();
		webView.setMaxWidth(PROP_WEB_VIEW_WIDTH);
		webView.setMaxHeight(PROP_WEB_VIEW_HEIGHT);
		webView.setMinWidth(PROP_WEB_VIEW_WIDTH);
		webView.setMinHeight(PROP_WEB_VIEW_HEIGHT);

		final WebEngine engine = webView.getEngine();
		engine.setCreatePopupHandler(features -> {
			return null;
		});

		final Worker<Void> loadWorker = engine.getLoadWorker();
		final ReadOnlyObjectProperty<State> stateProperty = loadWorker.stateProperty();
		stateProperty.addListener((ChangeListener<State>) (observable, oldValue, newValue) -> {

			if(newValue == State.FAILED) {
				engine.load(getClass().getResource(PROP_DEFAULT_HTML).toExternalForm());
			} else if(newValue == State.SUCCEEDED) {

				final Document doc = engine.getDocument();
				final NodeList nodeList = doc.getElementsByTagName("a");

				for(int i = 0; i < nodeList.getLength(); i++) {
					((EventTarget) nodeList.item(i)).addEventListener(EVENT_TYPE_CLICK, LINK_EVENT_HANDLER, false);
				}
			}
		});

		engine.load(PROP_INDEX_HTML);

		final HBox container = new HBox();
		container.setAlignment(CENTER);

		mainButton = new Button();
		mainButton.setCursor(Cursor.HAND);

		progressContainer = new StackPane();

		progressBar = new ProgressBar(0);
		progressBar.setId("HangarProgressStrength");

		progressBarStatus = new Label();
		progressBarStatus.setAlignment(CENTER_LEFT);

		gameFolderContainer = new HBox();
		gameFolderContainer.setVisible(false);

		questionLabel = new Label();
		questionLabel.setText("Игра уже установлена?");

		openChooserLabel = new Label();
		openChooserLabel.setText("Укажите путь установки игры.");
		openChooserLabel.setOnMouseClicked(event -> chooseGameFolder());
		openChooserLabel.setCursor(Cursor.HAND);

		FXUtils.bindFixedHeight(questionLabel, progressContainer.heightProperty());
		FXUtils.bindFixedHeight(openChooserLabel, progressContainer.heightProperty());
		FXUtils.bindFixedWidth(progressBarStatus, progressBar.widthProperty().subtract(10));

		FXUtils.addClassTo(questionLabel, "question-text-white");
		FXUtils.addClassTo(openChooserLabel, "question-text-blue");
		FXUtils.addClassTo(mainButton, "main-button");
		FXUtils.addClassTo(mainButton, "text-arial-14-white");
		FXUtils.addClassTo(progressBar, "progress-update-bar");
		FXUtils.addClassTo(progressBarStatus, "progress-status-text");

		FXUtils.setFixedSize(mainButton, PROP_MAIN_BUTTON_SIZE);
		FXUtils.setFixedSize(progressBar, new Point(564, 20));

		FXUtils.addToPane(questionLabel, gameFolderContainer);
		FXUtils.addToPane(openChooserLabel, gameFolderContainer);
		FXUtils.addToPane(gameFolderContainer, progressContainer);
		FXUtils.addToPane(mainButton, container);
		FXUtils.addToPane(progressBar, progressContainer);
		FXUtils.addToPane(progressBarStatus, progressContainer);
		FXUtils.addToPane(progressContainer, container);
		FXUtils.addToPane(webView, root);
		FXUtils.addToPane(container, root);

		VBox.setMargin(container, new Insets(13, 0, 0, 0));
		VBox.setMargin(webView, new Insets(0, 0, 0, 0));
		HBox.setMargin(progressContainer, new Insets(0, 0, 0, 10));
		HBox.setMargin(questionLabel, new Insets(0, 0, 0, 10));
		HBox.setMargin(openChooserLabel, new Insets(0, 0, 0, 5));

		return root;
	}

	/**
	 * Обработка вызова окна для выбора папки.
	 */
	private void chooseGameFolder() {

		final Scene scene = root.getScene();

		final DirectoryChooser chooser = new DirectoryChooser();
		chooser.setTitle("Папка для размещения игры");
		chooser.setInitialDirectory(new File(System.getProperty("user.home")));

		File folder = chooser.showDialog(scene.getWindow());

		if(folder == null) {
			return;
		}

		if(!FOLDER_SPACE_SHIFT_CLIENT.equals(folder.getName())) {
			folder = new File(folder, FOLDER_SPACE_SHIFT_CLIENT);
		}

		Config.gameFolder = folder.getAbsolutePath();
		Config.save();

		updateMainButton();
	}

	/**
	 * @return главная кнопка.
	 */
	public Button getMainButton() {
		return mainButton;
	}

	/**
	 * @return прогресс бар.
	 */
	public ProgressBar getProgressBar() {
		return progressBar;
	}

	/**
	 * @return статус прогресс бара.
	 */
	public Label getProgressBarStatus() {
		return progressBarStatus;
	}

	@Override
	public void postPageShow(final UIWindow window) {
		super.postPageShow(window);

		window.setRezisable(true);

		final Button mainButton = getMainButton();
		mainButton.setText("Проверка...");
		mainButton.setDisable(true);

		updateMainButton();
	}

	/**
	 * Обновление главной кнопки.
	 */
	public void updateMainButton() {

		final Button mainButton = getMainButton();
		final Path clientFile = LauncherUtils.getClientFile();

		if(!Files.exists(clientFile)) {
			mainButton.setText("Скачать");
			mainButton.setOnAction(event -> processDownload());
			mainButton.setDisable(false);

			if(Config.gameFolder == null) {
				gameFolderContainer.setVisible(true);
				progressBarStatus.setVisible(false);
				progressBar.setVisible(false);
			} else {
				gameFolderContainer.setVisible(false);
				progressBarStatus.setVisible(true);
				progressBarStatus.setText("Для продолжения загрузки нажмите кнопку \"Скачать\"...");
				progressBar.setVisible(false);
			}

			return;
		}

		gameFolderContainer.setVisible(false);
		progressBarStatus.setVisible(true);
		progressBar.setVisible(true);

		final ExecutorManager executorManager = ExecutorManager.getInstance();
		executorManager.async(() -> {
			checkUpdate();
		});
	}

	/**
	 * Процесс скачивания клиента.
	 */
	private void processDownload() {

		if(Config.gameFolder == null) {
			chooseGameFolder();
		}

		if(Config.gameFolder == null) {
			return;
		}

		progressBarStatus.setVisible(true);
		progressBarStatus.setText("");
		progressBar.setVisible(true);

		final Button mainButton = getMainButton();
		mainButton.setDisable(true);

		final ExecutorManager executorManager = ExecutorManager.getInstance();
		executorManager.async(new DownloadClientTask(this));
	}

	/**
	 * Процесс запуска
	 */
	private void processPlay() {

		final Button mainButton = getMainButton();

		try {
			LauncherUtils.runClient(() -> mainButton.setDisable(true), () -> mainButton.setDisable(false));
		} catch(final NotFoundClientException e) {

			final Alert alert = new Alert(AlertType.INFORMATION);
			alert.setHeaderText("Не найден клиент, перезапустите лаунчер.");
			alert.setTitle("Информация");
			alert.showAndWait();

		} catch(final IncorrectJavaException e) {

			final Alert alert = new Alert(AlertType.ERROR);
			alert.setHeaderText(e.getMessage());
			alert.setTitle("Ошибка");
			alert.showAndWait();
		}
	}

	private void processUpdate() {

		final Button mainButton = getMainButton();
		mainButton.setDisable(true);

		progressBarStatus.setVisible(true);
		progressBarStatus.setText("");
		progressBar.setVisible(true);

		final ExecutorManager executorManager = ExecutorManager.getInstance();
		executorManager.async(new UpdateClientTask(this));
	}
}
