package com.ss.launcher;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * @author Ronn
 */
public class Config {

	public static final String PROP_GAME_FOLDER = "gameFolder";

	public static final String FILE_LAST_VERSION_URL = "https://yadi.sk/i/uv_NfFRRgbFVV";
	public static final String FILE_CLIENT_URL = "https://yadi.sk/d/-pgiqKu-gGDJH";

	public static final String FILE_ENGINE = "Yandex.Disk";

	public static volatile String gameFolder;

	/**
	 * Инициализация конфига.
	 */
	public static void init() {

		final Preferences preferences = Preferences.userNodeForPackage(Launcher.class);

		gameFolder = preferences.get(PROP_GAME_FOLDER, null);
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

		try {
			preferences.flush();
		} catch(BackingStoreException e) {
			e.printStackTrace();
		}
	}
}
