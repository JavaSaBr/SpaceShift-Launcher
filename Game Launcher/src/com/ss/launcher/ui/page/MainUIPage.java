package com.ss.launcher.ui.page;

import static com.ss.launcher.util.LauncherUtils.getCurrentVersion;
import static com.ss.launcher.util.LauncherUtils.isNeedUpdate;
import static javafx.application.Platform.runLater;
import static javafx.geometry.Pos.CENTER;
import static javafx.geometry.Pos.CENTER_LEFT;
import static javafx.geometry.Pos.TOP_CENTER;

import java.awt.Point;
import java.nio.file.Files;
import java.nio.file.Path;

import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import rlib.ui.page.impl.AbstractUIPage;
import rlib.ui.util.FXUtils;
import rlib.ui.window.UIWindow;

import com.ss.launcher.ExecutorManager;
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

	public static final Insets PROP_LINE_OFFSET = new Insets(10, 0, 10, 0);

	/** рут страницы */
	private VBox root;

	/** прогресс бар */
	private ProgressBar progressBar;
	/** статус прогресс бара */
	private Label progressBarStatus;

	/** главная кнопка */
	private Button mainButton;

	/**
	 * Проверка необходимости обновления клиента.
	 */
	protected void checkUpdate() {

		final Button mainButton = getMainButton();
		final Label progressBarStatus = getProgressBarStatus();

		try {

			if(!isNeedUpdate()) {
				runLater(() -> {

					mainButton.setText("Играть");
					mainButton.setOnAction(event -> processPlay());
					mainButton.setDisable(false);

					progressBarStatus.setText("Текущая версия клиента " + getCurrentVersion());
				});
			} else {
				runLater(() -> {

					mainButton.setText("Обновить");
					mainButton.setOnAction(event -> processUpdate());
					mainButton.setDisable(false);

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

		final HBox container = new HBox();
		container.setAlignment(CENTER);

		mainButton = new Button();

		final StackPane progressContainer = new StackPane();

		progressBar = new ProgressBar(0);
		progressBar.setId("HangarProgressStrength");

		progressBarStatus = new Label();
		progressBarStatus.setAlignment(CENTER_LEFT);

		FXUtils.bindFixedWidth(progressBarStatus, progressBar.widthProperty().subtract(10));

		FXUtils.addClassTo(mainButton, "main-button");
		FXUtils.addClassTo(mainButton, "text-arial-14-white");
		FXUtils.addClassTo(progressBar, "progress-update-bar");
		FXUtils.addClassTo(progressBarStatus, "progress-status-text");

		FXUtils.setFixedSize(mainButton, new Point(140, 34));
		FXUtils.setFixedSize(progressBar, new Point(564, 20));

		FXUtils.addToPane(mainButton, container);
		FXUtils.addToPane(progressBar, progressContainer);
		FXUtils.addToPane(progressBarStatus, progressContainer);
		FXUtils.addToPane(progressContainer, container);
		FXUtils.addToPane(container, root);

		VBox.setMargin(container, new Insets(440, 0, 0, 0));
		HBox.setMargin(progressContainer, new Insets(0, 0, 0, 10));

		return root;
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
			return;
		}

		final ExecutorManager executorManager = ExecutorManager.getInstance();
		executorManager.async(() -> {
			checkUpdate();
		});
	}

	/**
	 * Процесс скачивания клиента.
	 */
	private void processDownload() {

		final Button mainButton = getMainButton();
		mainButton.setDisable(true);

		final ExecutorManager executorManager = ExecutorManager.getInstance();
		executorManager.async(new DownloadClientTask(this));
	}

	/**
	 * Процесс запуска
	 */
	private void processPlay() {

		try {
			LauncherUtils.runClient();
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

		final ExecutorManager executorManager = ExecutorManager.getInstance();
		executorManager.async(new UpdateClientTask(this));
	}
}
