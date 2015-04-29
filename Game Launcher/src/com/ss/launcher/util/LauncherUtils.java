package com.ss.launcher.util;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import rlib.util.FileUtils;
import rlib.util.StringUtils;
import rlib.util.Util;

import com.ss.launcher.Config;
import com.ss.launcher.Launcher;
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

	public static final String FOLDER_GAME = "game";
	public static final String FOLDER_LOG = "log";

	public static final String FILE_SPACESHIFT_JAR = "spaceshift.jar";
	public static final String FILE_LAST_VERSION = "last_version";

	/**
	 * @return получение файла для запуска клиента.
	 */
	public static Path getClientFile() {

		final Path rootFolder = Util.getRootFolderFromClass(Launcher.class);
		final Path targetFile = Paths.get(rootFolder.toString(), FOLDER_GAME, FILE_SPACESHIFT_JAR);

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
		final Path rootFolder = Util.getRootFolderFromClass(Launcher.class);
		return Paths.get(rootFolder.toString(), FOLDER_GAME);
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
	public static void runClient() {

		final Path targetFile = getClientFile();

		if(!Files.exists(targetFile)) {
			throw new NotFoundClientException("Обновите версию клиента");
		}

		final String javaVersion = LauncherUtils.getSystemJavaVersion();

		if(javaVersion == null) {
			throw new IncorrectJavaException("Отсутствует JAVA. Пожайлуста, установите JAVA (java.com/ru/downoads)");
		}

		if(!(javaVersion.contains("1.8") || javaVersion.contains("1.9"))) {
			throw new IncorrectJavaException("Установленная версия JAVA устарела. Пожалуйста, обновите JAVA (java.com/ru/downoads)");
		}

		final List<String> commands = new ArrayList<>();
		commands.add("java");
		commands.add("-jar");
		commands.add("-XX:CompileThreshold=200");
		commands.add("-XX:+AggressiveOpts");
		commands.add("-XX:+UseParallelGC");
		commands.add("-XX:+UseTLAB");
		commands.add("-Xmx1024m");
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

		if(result == -2) {
			runClient();
		}
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

		final String lastVersion = fileEngine.getContent(Config.FILE_LAST_VERSION_URL);
		final String currentVersion = LauncherUtils.getCurrentVersion();

		return !StringUtils.equals(lastVersion, currentVersion);
	}
}
