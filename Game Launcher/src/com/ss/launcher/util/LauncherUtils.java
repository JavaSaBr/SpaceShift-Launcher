package com.ss.launcher.util;

import com.ss.launcher.Config;
import com.ss.launcher.exception.IncorrectJavaException;
import com.ss.launcher.exception.NotFoundClientException;
import com.ss.launcher.file.engine.FileEngine;
import com.ss.launcher.file.engine.FileEngineManager;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.json.JSONObject;
import rlib.logging.Logger;
import rlib.logging.LoggerManager;
import rlib.util.FileUtils;
import rlib.util.StringUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.ProxySelector;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static com.ss.launcher.Config.CONFIG_URL;
import static com.ss.launcher.Messages.*;
import static javafx.application.Platform.runLater;

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
        return gameFolder.resolve(FILE_SPACESHIFT_JAR);
    }

    /**
     * @return конфигурация лаунчера.
     */
    public static JSONObject getConfig() {

        try(final CloseableHttpClient httpClient = createHttpClient()) {

            final HttpResponse response = httpClient.execute(new HttpGet(CONFIG_URL));
            final StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() != HttpStatus.SC_OK) {
                throw new IOException();
            }

            final HttpEntity entity = response.getEntity();
            final String jsonString = readStream(entity.getContent());

            return new JSONObject(jsonString);

        } catch (final IOException e) {
            LauncherUtils.handleException(new RuntimeException(RUNTIME_EXCEPTION_MESSAGE_CONNECT_PROBLEM));
        }

        final JSONObject result = new JSONObject();
        result.put(Config.PROP_FILE_ENGINE, "Yandex.Disk");
        result.put(Config.PROP_FILE_CLIENT_LAST_VERSION_URL, "");
        result.put(Config.PROP_FILE_CLIENT_URL, "");
        result.put(Config.PROP_FILE_LAUNCHER_LAST_VERSION_URL, "");
        result.put(Config.PROP_UPDATE_LAUNCHER_URL, "https://spaceshift.ru/forum/index.php?topic=910.0");
        result.put(Config.PROP_INDEX_HTML_URL, "https://spaceshift.ru/upd/index.html");

        return result;
    }

    /**
     * @return текущая версия клиента.
     */
    public static String getCurrentVersion() {

        final Path gameFolder = getGameFolder();
        final Path versionFile = gameFolder.resolve(FILE_LAST_VERSION);

        if (!Files.exists(versionFile)) {
            return StringUtils.EMPTY;
        }

        try {

            String result = new String(Files.readAllBytes(versionFile));
            result = result.trim();

            return result;

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @return путь к папке с самим клиентом.
     */
    public static Path getGameFolder() {

        Path gameFolder;

        if (Config.gameFolder == null) {
            gameFolder = Paths.get(System.getProperty("user.home"), ".ss_launcher", FOLDER_GAME);
        } else {
            gameFolder = Paths.get(Config.gameFolder);
        }

        if (!Files.exists(gameFolder)) {
            try {
                Files.createDirectories(gameFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return gameFolder;
    }

    public static String getSystemJavaVersion() {
        LOGGER.debug("start check system version of java");

        Path tempFile = null;

        try {

            tempFile = Files.createTempFile("ss_launcher", "check_java_version");

            final ProcessBuilder builder = new ProcessBuilder("java", "-version");
            builder.redirectOutput(tempFile.toFile());
            builder.redirectError(tempFile.toFile());

            final Process start = builder.start();
            final int result = start.waitFor();

            LOGGER.debug("result check process = " + result);

            if (result != 0) {
                return null;
            }

            final String content = new String(Files.readAllBytes(tempFile));

            LOGGER.debug("result process content:\n" + content);

            if (StringUtils.isEmpty(content) || !content.contains("java version")) {
                return null;
            }

            if (content.contains("OpenJDK")) {
                return "OpenJDK";
            }

            final String versionLine = content.substring(0, content.indexOf('\n'));

            LOGGER.debug("versionLine = " + versionLine);

            final int startIndex = versionLine.indexOf('"');
            final int endIndex = versionLine.lastIndexOf('"');

            final String version = versionLine.substring(startIndex + 1, endIndex);

            LOGGER.debug("version = " + version);

            return version;

        } catch (final Exception e) {
            throw new RuntimeException(e);
        } finally {
            FileUtils.delete(tempFile);
        }
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

    /**
     * @return надо ли обновлять клиента.
     */
    public static boolean isNeedUpdate() {

        final FileEngine fileEngine = FileEngineManager.get(Config.fileEngine);

        final String lastVersion = fileEngine.getContent(Config.fileClientLastVersionUrl);
        final String currentVersion = LauncherUtils.getCurrentVersion();

        return !StringUtils.equals(lastVersion, currentVersion);
    }

    public static String readStream(InputStream in) {

        String result = null;

        try (Scanner scanner = new Scanner(in)) {

            final StringBuilder builder = new StringBuilder();

            while (scanner.hasNext()) {
                builder.append(scanner.nextLine());
            }

            result = builder.toString();
        }

        return result;
    }

    /**
     * Запуск клиента.
     */
    public static void runClient(final Runnable startHandler, final Runnable finishHandler) {

        final Path targetFile = getClientFile();

        if (!Files.exists(targetFile)) {
            throw new NotFoundClientException(NOT_FOUND_CLIENT_EXCEPTION_MESSAGE_NEED_UPDATE);
        }

        final String javaVersion = LauncherUtils.getSystemJavaVersion();

        if (javaVersion == null) {
            throw new IncorrectJavaException(INCORRECT_JAVA_EXCEPTION_MESSAGE_NOT_FOUND_JAVA);
        } else if ("OpenJDK".equals(javaVersion)) {
            throw new IncorrectJavaException(INCORRECT_JAVA_EXCEPTION_MESSAGE_NEED_INSTALL_ORACLE);
        } else if (!(javaVersion.contains("1.8") || javaVersion.contains("1.9"))) {
            throw new IncorrectJavaException(INCORRECT_JAVA_EXCEPTION_MESSAGE_OLD_VERSION);
        }

        Platform.runLater(startHandler::run);

        Thread fork = new Thread(() -> {

            final List<String> commands = new ArrayList<>();
            commands.add("java");
            commands.add("-jar");
            commands.add("-XX:CompileThreshold=1000");
            commands.add("-XX:+AggressiveOpts");
            commands.add("-XX:+UseParallelGC");
            commands.add("-XX:+UseTLAB");

            if (!StringUtils.isEmpty(Config.httpProxyHost)) {
                commands.add("-Dhttp.proxyHost=" + Config.httpProxyHost);
                commands.add("-Dhttp.proxyHost=" + Config.httpProxyPort);
            }

            commands.add(targetFile.toString());

            final ProcessBuilder builder = new ProcessBuilder(commands);
            builder.inheritIO();

            int result = 0;

            try {

                final Process process = builder.start();
                result = process.waitFor();

            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }

            Platform.runLater(finishHandler::run);

            if (result == -2) {
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

            if (!Files.exists(file)) {
                Files.createFile(file);
            }

            try (PrintWriter print = new PrintWriter(Files.newOutputStream(file))) {
                print.println(newVersion);
            }

        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CloseableHttpClient createHttpClient() {

        if(StringUtils.isEmpty(Config.httpProxyHost)) {
            return HttpClients.createDefault();
        }

        final SystemDefaultRoutePlanner routePlanner = new SystemDefaultRoutePlanner(ProxySelector.getDefault());

        HttpClientBuilder custom = HttpClients.custom();
        custom = custom.setRoutePlanner(routePlanner);

        return custom.build();
    }
}
