package com.ss.launcher;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import org.json.JSONObject;

import rlib.logging.Logger;
import rlib.logging.LoggerManager;
import rlib.util.StringUtils;

import com.ss.launcher.util.LauncherUtils;

/**
 * @author Ronn
 */
public class Config {

	private static final Logger LOGGER = LoggerManager.getLogger(Config.class);

	public static final String PROP_FILE_LAUNCHER_LAST_VERSION_URL = "fileLauncherLastVersionURL";
	public static final String PROP_FILE_CLIENT_URL = "fileClientUrl";
	public static final String PROP_FILE_CLIENT_LAST_VERSION_URL = "fileClientLastVersionURL";
	public static final String PROP_FILE_ENGINE = "fileEngine";

	public static final String PROP_CONFIG_URL = "http://spaceshift.ru/launcher/config.json";
	public static final String PROP_GAME_FOLDER = "gameFolder";
	public static final String PROP_HTTP_PROXY_HOST = "httpProxyHost";
	public static final String PROP_HTTP_PROXY_PORT = "httProxyPort";

	public static String CURRENT_VERSION = "1.3";

	public static String FILE_CLIENT_LAST_VERSION_URL = "";
	public static String FILE_CLIENT_URL = "";

	public static String FILE_LAUNCHER_LAST_VERSION_URL = "";

	public static String FILE_ENGINE = "";

	public static volatile String gameFolder;
	public static volatile String httpProxyHost;
	public static volatile String httpProxyPort;

	/**
	 * Инициализация конфига.
	 */
	public static void init() {

		final Preferences preferences = Preferences.userNodeForPackage(Launcher.class);

		gameFolder = preferences.get(PROP_GAME_FOLDER, null);
		httpProxyHost = preferences.get(PROP_HTTP_PROXY_HOST, "");
		httpProxyPort = preferences.get(PROP_HTTP_PROXY_PORT, "");

		if(!StringUtils.isEmpty(Config.httpProxyHost)) {
			System.setProperty("http.proxyHost", Config.httpProxyHost);
			System.setProperty("http.proxyPort", Config.httpProxyPort);
		}

		final JSONObject config = LauncherUtils.getConfig();

		FILE_ENGINE = config.getString(PROP_FILE_ENGINE);
		FILE_CLIENT_LAST_VERSION_URL = config.getString(PROP_FILE_CLIENT_LAST_VERSION_URL);
		FILE_CLIENT_URL = config.getString(PROP_FILE_CLIENT_URL);
		FILE_LAUNCHER_LAST_VERSION_URL = config.getString(PROP_FILE_LAUNCHER_LAST_VERSION_URL);
	}

	/**
	 * Сохранение текущих настроек.
	 */
	public static void save() {

		final Preferences preferences = Preferences.userNodeForPackage(Launcher.class);

		if(gameFolder != null) {
			preferences.put(PROP_GAME_FOLDER, gameFolder);
		} else {
			preferences.remove(PROP_GAME_FOLDER);
		}

		preferences.put(PROP_HTTP_PROXY_HOST, httpProxyHost);
		preferences.put(PROP_HTTP_PROXY_PORT, httpProxyPort);

		try {
			preferences.flush();
		} catch(BackingStoreException e) {
			LOGGER.error(e);
		}
	}
}
