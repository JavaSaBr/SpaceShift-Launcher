package com.ss.launcher.util;

import static com.ss.launcher.Config.PROP_CONFIG_URL;
import static com.ss.launcher.Messages.ALERT_ERROR_TITLE;
import static com.ss.launcher.Messages.INCORRECT_JAVA_EXCEPTION_MESSAGE_NEED_INSTALL_ORACLE;
import static com.ss.launcher.Messages.INCORRECT_JAVA_EXCEPTION_MESSAGE_NOT_FOUND_JAVA;
import static com.ss.launcher.Messages.INCORRECT_JAVA_EXCEPTION_MESSAGE_OLD_VERSION;
import static com.ss.launcher.Messages.NOT_FOUND_CLIENT_EXCEPTION_MESSAGE_NEED_UPDATE;
import static com.ss.launcher.Messages.RUNTIME_EXCEPTION_MESSAGE_CONNECT_PROBLEM;
import static javafx.application.Platform.runLater;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import rlib.logging.Logger;
import rlib.logging.LoggerManager;
import rlib.util.FileUtils;
import rlib.util.StringUtils;

import com.ss.launcher.Config;
import com.ss.launcher.exception.IncorrectJavaException;
import com.ss.launcher.exception.NotFoundClientException;
import com.ss.launcher.file.engine.FileEngine;
import com.ss.launcher.file.engine.FileEngineManager;

/**
 * Набор утильных методов для лаунчера.
 * 
 * @author Ronn
 */
public class LauncherUtils {

	protected static final Logger LOGGER = LoggerManager.getLogger(LauncherUtils.class);

	public static final String FOLDER_GAME = "game";
	public static final String FOLDER_LOG = "log";

	public static final String FILE_SPACESHIFT_JAR = "spaceshift.jar";
	public static final String FILE_LAST_VERSION = "last_version";

	/**
	 * @return получение файла для запуска клиента.
	 */
	public static Path getClientFile() {

		final Path gameFolder = getGameFolder();
		final Path targetFile = Paths.get(gameFolder.toString(), FILE_SPACESHIFT_JAR);

		return targetFile;
	}

	/**
	 * @return текущая версия клиента.
	 */
	public static String getCurrentVersion() {

		final Path gameFolder = getGameFolder();
		final Path file = Paths.get(gameFolder.toString(), FILE_LAST_VERSION);

		if(!Files.exists(file)) {
			return StringUtils.EMPTY;
		}

		try {

			String result = new String(Files.readAllBytes(file));
			result = result.trim();

			return result;
		} catch(final IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return путь к папке с самим клиентом.
	 */
	public static Path getGameFolder() {

		Path gameFolder = null;

		if(Config.gameFolder == null) {
			gameFolder = Paths.get(System.getProperty("user.home"), ".ss_launcher", FOLDER_GAME);
		} else {
			gameFolder = Paths.get(Config.gameFolder);
		}

		if(!Files.exists(gameFolder)) {
			try {
				Files.createDirectories(gameFolder);
			} catch(IOException e) {
				throw new RuntimeException(e);
			}
		}

		return gameFolder;
	}

	public static String getSystemJavaVersion() {

		Path tempFile = null;

		try {

			tempFile = Files.createTempFile("ss_launcher", "check_java_version");

			final ProcessBuilder builder = new ProcessBuilder("java", "-version");
			builder.redirectOutput(tempFile.toFile());
			builder.redirectError(tempFile.toFile());

			final Process start = builder.start();
			final int result = start.waitFor();

			if(result != 0) {
				return null;
			}

			final String content = new String(Files.readAllBytes(tempFile));

			if(StringUtils.isEmpty(content) || !content.contains("java version")) {
				return null;
			}

			if(content.contains("OpenJDK")) {
				return "OpenJDK";
			}

			final int startIndex = content.indexOf('"');
			final int endIndex = content.lastIndexOf('"');

			return content.substring(startIndex + 1, endIndex);

		} catch(final Exception e) {
			throw new RuntimeException(e);
		} finally {
			FileUtils.delete(tempFile);
		}
	}

	/**
	 * Запуск клиента.
	 */
	public static void runClient(final Runnable startHandler, final Runnable finishHandler) {

		final Path targetFile = getClientFile();

		if(!Files.exists(targetFile)) {
			throw new NotFoundClientException(NOT_FOUND_CLIENT_EXCEPTION_MESSAGE_NEED_UPDATE);
		}

		final String javaVersion = LauncherUtils.getSystemJavaVersion();

		if(javaVersion == null) {
			throw new IncorrectJavaException(INCORRECT_JAVA_EXCEPTION_MESSAGE_NOT_FOUND_JAVA);
		} else if("OpenJDK".equals(javaVersion)) {
			throw new IncorrectJavaException(INCORRECT_JAVA_EXCEPTION_MESSAGE_NEED_INSTALL_ORACLE);
		} else if(!(javaVersion.contains("1.8") || javaVersion.contains("1.9"))) {
			throw new IncorrectJavaException(INCORRECT_JAVA_EXCEPTION_MESSAGE_OLD_VERSION);
		}

		Platform.runLater(() -> startHandler.run());

		Thread fork = new Thread(() -> {

			final List<String> commands = new ArrayList<>();
			commands.add("java");
			commands.add("-jar");
			commands.add("-XX:CompileThreshold=1000");
			commands.add("-XX:+AggressiveOpts");
			commands.add("-XX:+UseParallelGC");
			commands.add("-XX:+UseTLAB");
			commands.add("-Xmx3072m");
			commands.add(targetFile.toString());

			final ProcessBuilder builder = new ProcessBuilder(commands);
			builder.inheritIO();

			int result = 0;

			try {

				final Process process = builder.start();
				result = process.waitFor();

			} catch(IOException | InterruptedException e) {
				throw new RuntimeException(e);
			}

			Platform.runLater(() -> finishHandler.run());

			if(result == -2) {
				runClient(startHandler, finishHandler);
			}
		});
		fork.start();
	}

	/**
	 * Обновление версии клиента.
	 */
	public static void updateVersion(final String newVersion) {

		final Path gameFolder = getGameFolder();
		final Path file = Paths.get(gameFolder.toString(), FILE_LAST_VERSION);

		try {

			if(!Files.exists(file)) {
				Files.createFile(file);
			}

			try(PrintWriter print = new PrintWriter(Files.newOutputStream(file))) {
				print.println(newVersion);
			}

		} catch(final Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * @return надо ли обновлять клиента.
	 */
	public static boolean isNeedUpdate() {

		final FileEngine fileEngine = FileEngineManager.get(Config.FILE_ENGINE);

		final String lastVersion = fileEngine.getContent(Config.FILE_CLIENT_LAST_VERSION_URL);
		final String currentVersion = LauncherUtils.getCurrentVersion();

		return !StringUtils.equals(lastVersion, currentVersion);
	}

	/**
	 * @return конфигурация лаунчера.
	 */
	public static JSONObject getConfig() {

		final HttpClient httpClient = HttpClients.createDefault();
		try {

			HttpResponse response = httpClient.execute(new HttpGet(PROP_CONFIG_URL));
			HttpEntity entity = response.getEntity();

			final String jsonString = readStream(entity.getContent());
			final JSONObject object = new JSONObject(jsonString);

			return object;
		} catch(final IOException e) {
			LauncherUtils.handleException(new RuntimeException(RUNTIME_EXCEPTION_MESSAGE_CONNECT_PROBLEM));
		}

		final JSONObject result = new JSONObject();
		result.put(Config.PROP_FILE_ENGINE, "Yandex.Disk");
		result.put(Config.PROP_FILE_CLIENT_LAST_VERSION_URL, "");
		result.put(Config.PROP_FILE_CLIENT_URL, "");
		result.put(Config.PROP_FILE_LAUNCHER_LAST_VERSION_URL, "");

		return result;
	}

	public static String readStream(InputStream in) {

		String result = null;

		try(Scanner scanner = new Scanner(in)) {

			final StringBuilder builder = new StringBuilder();

			while(scanner.hasNext()) {
				builder.append(scanner.nextLine());
			}

			result = builder.toString();
		}

		return result;
	}

	/**
	 * Обработка ошибки.
	 */
	public static void handleException(Exception e) {
		runLater(() -> {
			final Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle(ALERT_ERROR_TITLE);
			alert.setHeaderText(e.getLocalizedMessage());
			alert.showAndWait();
		});
	}
}
