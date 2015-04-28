package com.ss.launcher.ui.page;

import static javafx.geometry.Pos.TOP_CENTER;

import java.awt.Point;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import rlib.ui.page.impl.AbstractUIPage;
import rlib.ui.util.FXUtils;
import rlib.ui.window.UIWindow;

import com.ss.launcher.ExecutorManager;
import com.ss.launcher.exception.IncorrectJavaException;
import com.ss.launcher.exception.NotFoundClientException;
import com.ss.launcher.tasks.UpdateClientTask;
import com.ss.launcher.util.LauncherUtils;

/**
 * Реализация основной страницы лаунчера.
 * 
 * @author Ronn
 */
public class MainUIPage extends AbstractUIPage {

	public static final Insets PROP_LINE_OFFSET = new Insets(10, 0, 10, 0);

	private VBox root;

	private ImageView logoImageView;

	private Button updateClientButton;
	private Button playButton;

	private ProgressBar progressBar;

	private Label progressBarStatus;

	@Override
	public void postPageShow(final UIWindow window) {
		super.postPageShow(window);

		window.setSize(1000, 600);
		window.setRezisable(true);
	}

	@Override
	protected Pane createRoot() {

		root = new VBox();
		root.setAlignment(TOP_CENTER);

		createSplitLine(root);
		createLogo();
		createProgressBar();
		createButtons();

		return root;
	}

	protected void createProgressBar() {

		progressBar = new ProgressBar(0);
		progressBar.setId("HangarProgressStrength");

		progressBarStatus = new Label();

		VBox.setMargin(progressBarStatus, new Insets(10, 0, 0, 0));

		FXUtils.addClassTo(progressBarStatus, "arial-label-17");

		FXUtils.setFixedSize(progressBar, new Point(800, 10));
		FXUtils.addToPane(progressBarStatus, root);
		FXUtils.addToPane(progressBar, root);
	}

	protected void createButtons() {

		final HBox buttonContainer = new HBox();
		buttonContainer.setAlignment(Pos.CENTER);

		updateClientButton = new Button();
		updateClientButton.setText("Проверить обновление");
		updateClientButton.setOnAction(event -> processUpdate());

		playButton = new Button();
		playButton.setText("Играть");
		playButton.setOnAction(event -> processPlay());

		FXUtils.addClassTo(updateClientButton, "arial-label-17");
		FXUtils.addClassTo(playButton, "arial-label-17");
		FXUtils.setFixedSize(updateClientButton, new Point(300, 26));
		FXUtils.setFixedSize(playButton, new Point(300, 26));

		FXUtils.addToPane(updateClientButton, buttonContainer);
		FXUtils.addToPane(playButton, buttonContainer);
		FXUtils.addToPane(buttonContainer, root);

		VBox.setMargin(buttonContainer, new Insets(10, 0, 0, 0));
		HBox.setMargin(updateClientButton, new Insets(0, 5, 0, 0));
		HBox.setMargin(playButton, new Insets(0, 0, 0, 5));
	}

	public Button getPlayButton() {
		return playButton;
	}

	public Button getUpdateClientButton() {
		return updateClientButton;
	}

	public ProgressBar getProgressBar() {
		return progressBar;
	}

	public Label getProgressBarStatus() {
		return progressBarStatus;
	}

	private void processUpdate() {

		final Button playButton = getPlayButton();
		playButton.setDisable(true);

		final Button updateClientButton = getUpdateClientButton();
		updateClientButton.setDisable(true);

		final ExecutorManager executorManager = ExecutorManager.getInstance();
		executorManager.async(new UpdateClientTask(this));
	}

	protected void createLogo() {

		logoImageView = new ImageView();
		logoImageView.setImage(new Image("/com/ss/launcher/resources/logo.png"));

		FXUtils.addToPane(logoImageView, root);
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

	/**
	 * Создание разделительной линии.
	 */
	protected void createSplitLine(final VBox container) {

		final Line splitLine = new Line();
		splitLine.setId("BlueGradientHorizontalLine");
		splitLine.setStartX(0);
		splitLine.endXProperty().bind(container.widthProperty());

		VBox.setMargin(splitLine, PROP_LINE_OFFSET);
		FXUtils.addToPane(splitLine, container);
	}
}
