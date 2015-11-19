package com.ss.launcher;

import com.ss.launcher.util.LauncherUtils;
import org.json.JSONObject;
import rlib.logging.Logger;
import rlib.logging.LoggerManager;
import rlib.util.StringUtils;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Ronn
 */
public class Config {

    private static final Logger LOGGER = LoggerManager.getLogger(Config.class);

    public static final String CONFIG_URL = "http://spaceshift.ru/launcher/config.json";

    public static final String PROP_FILE_LAUNCHER_LAST_VERSION_URL = "fileLauncherLastVersionURL";
    public static final String PROP_FILE_CLIENT_URL = "fileClientUrl";
    public static final String PROP_FILE_CLIENT_LAST_VERSION_URL = "fileClientLastVersionURL";
    public static final String PROP_FILE_ENGINE = "fileEngine";
    public static final String PROP_UPDATE_LAUNCHER_URL = "updateLauncherUrl";
    public static final String PROP_INDEX_HTML_URL = "indexHtmlUrl";

    public static final String PREF_GAME_FOLDER = "gameFolder";
    public static final String PREF_HTTP_PROXY_HOST = "httpProxyHost";
    public static final String PREF_HTTP_PROXY_PORT = "httProxyPort";

    public static String CURRENT_VERSION = "1.5";

    /**
     * URL файла с актуальной версией клиента.
     */
    public static volatile String fileClientLastVersionUrl = "";

    /**
     * URL файла-архива с клиентом.
     */
    public static volatile String fileClientUrl = "";

    /**
     * URL файла с актуальной версией лаунчера.
     */
    public static volatile String fileLauncherLastVersionUrl = "";

    /**
     * Тип движка для выкачивания клиента.
     */
    public static volatile String fileEngine = "";

    /**
     * Папка расположения клиента.
     */
    public static volatile String gameFolder;

    /**
     * Хост прокси сервера.
     */
    public static volatile String httpProxyHost;

    /**
     * Порт прокси сервера.
     */
    public static volatile String httpProxyPort;

    /**
     * Адресс страницы с обновлением лаунчера.
     */
    public static volatile String updateLauncherUrl;

    /**
     * Адресс страницы с новостями для лаунчера.
     */
    public static volatile String indexHtmlUrl;

    /**
     * Инициализация конфига.
     */
    public static void initPreferences() {

        final Preferences preferences = Preferences.userNodeForPackage(Launcher.class);

        gameFolder = preferences.get(PREF_GAME_FOLDER, null);
        httpProxyHost = preferences.get(PREF_HTTP_PROXY_HOST, "");
        httpProxyPort = preferences.get(PREF_HTTP_PROXY_PORT, "");

        if (!StringUtils.isEmpty(Config.httpProxyHost)) {
            System.setProperty("http.proxyHost", Config.httpProxyHost);
            System.setProperty("http.proxyPort", Config.httpProxyPort);
        }
    }

    /**
     * Инициализация конфига.
     */
    public static void initConfig() {

        final JSONObject config = LauncherUtils.getConfig();

        fileEngine = config.getString(PROP_FILE_ENGINE);
        fileClientLastVersionUrl = config.getString(PROP_FILE_CLIENT_LAST_VERSION_URL);
        fileClientUrl = config.getString(PROP_FILE_CLIENT_URL);
        fileLauncherLastVersionUrl = config.getString(PROP_FILE_LAUNCHER_LAST_VERSION_URL);
        updateLauncherUrl = config.getString(PROP_UPDATE_LAUNCHER_URL);
        indexHtmlUrl = config.getString(PROP_INDEX_HTML_URL);
    }

    /**
     * Сохранение текущих настроек.
     */
    public static void save() {

        final Preferences preferences = Preferences.userNodeForPackage(Launcher.class);

        if (gameFolder != null) {
            preferences.put(PREF_GAME_FOLDER, gameFolder);
        } else {
            preferences.remove(PREF_GAME_FOLDER);
        }

        preferences.put(PREF_HTTP_PROXY_HOST, httpProxyHost);
        preferences.put(PREF_HTTP_PROXY_PORT, httpProxyPort);

        try {
            preferences.flush();
        } catch (BackingStoreException e) {
            LOGGER.error(e);
        }
    }
}
