package com.ss.launcher.ui.page;

import static javafx.geometry.Pos.TOP_CENTER;

import java.awt.Point;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
import rlib.util.Util;

import com.ss.launcher.Launcher;

/**
 * Реализация основной страницы лаунчера.
 * 
 * @author Ronn
 */
public class MainUIPage extends AbstractUIPage {

	public static final Insets PROP_LINE_OFFSET = new Insets(10, 0, 10, 0);

	private VBox root;

	private ImageView logoImageView;

	private Button updateClient;

	private Button playButton;

	private ProgressBar progressBar;

	@Override
	public void postPageShow(UIWindow window) {
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

		VBox.setMargin(progressBar, new Insets(10, 0, 0, 0));

		FXUtils.setFixedSize(progressBar, new Point(800, 10));
		FXUtils.addToPane(progressBar, root);
	}

	protected void createButtons() {

		HBox buttonContainer = new HBox();
		buttonContainer.setAlignment(Pos.CENTER);

		updateClient = new Button();
		updateClient.setText("Проверить обновление");
		updateClient.setOnAction(event -> processUpdate());

		playButton = new Button();
		playButton.setText("Играть");
		playButton.setOnAction(event -> processPlay());

		FXUtils.addClassTo(updateClient, "arial-label-17");
		FXUtils.addClassTo(playButton, "arial-label-17");
		FXUtils.setFixedSize(updateClient, new Point(300, 26));
		FXUtils.setFixedSize(playButton, new Point(300, 26));

		FXUtils.addToPane(updateClient, buttonContainer);
		FXUtils.addToPane(playButton, buttonContainer);
		FXUtils.addToPane(buttonContainer, root);

		VBox.setMargin(buttonContainer, new Insets(10, 0, 0, 0));
		HBox.setMargin(updateClient, new Insets(0, 5, 0, 0));
		HBox.setMargin(playButton, new Insets(0, 0, 0, 5));
	}

	private void processUpdate() {

	}

	protected void createLogo() {

		logoImageView = new ImageView();
		logoImageView.setImage(new Image("/com/ss/launcher/resources/logo.png"));

		FXUtils.addToPane(logoImageView, root);
	}

	private void processPlay() {
		processRun();
	}

	protected void processRun() {

		final Path rootFolder = Util.getRootFolderFromClass(Launcher.class);
		final Path targetFile = Paths.get(rootFolder.toString(), "game", "spaceshift.jar");

		Path javaFolder = Paths.get(System.getProperty("java.home"), "bin");
		Path targetJava = javaFolder.resolve("java");

		if(!Files.exists(targetJava)) {
			targetJava = javaFolder.resolve("java.exe");
		}

		if(!Files.exists(targetJava)) {
			javaFolder = Paths.get(System.getProperty("java.home"), "lib");
			// targetJava = javaFolder.resolve("jexec");
		}

		if(!Files.exists(targetJava)) {
			// targetJava = javaFolder.resolve("jexec.exe");
		}

		// System.out.println("properties: ");

		for(Entry<Object, Object> entry : System.getProperties().entrySet()) {
			// System.out.println(entry.getKey() + " = " + entry.getValue());
		}

		String java = Files.exists(targetJava) ? targetJava.toString() : "java";

		List<String> commands = new ArrayList<>();
		commands.add(java);
		commands.add("-jar");
		commands.add("-XX:CompileThreshold=200");
		commands.add("-XX:+AggressiveOpts");
		commands.add("-XX:CompileThreshold=200");
		commands.add("-XX:+UseParallelGC");
		commands.add("-XX:+UseTLAB");
		commands.add("-Xmx512m");
		commands.add(targetFile.toString());

		System.out.println("commands " + commands);

		ProcessBuilder builder = new ProcessBuilder(commands);
		builder.inheritIO();

		try {
			final Process process = builder.start();
			process.waitFor();
		} catch(IOException | InterruptedException e) {
			e.printStackTrace();
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
