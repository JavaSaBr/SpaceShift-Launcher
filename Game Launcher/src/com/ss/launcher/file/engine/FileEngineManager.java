package com.ss.launcher.file.engine;

import rlib.util.dictionary.DictionaryFactory;
import rlib.util.dictionary.ObjectDictionary;

import com.ss.launcher.file.engine.yandex.disk.YandexDiskFileEngine;

/**
 * Менеджер файловых движков.
 * 
 * @author Ronn
 */
public class FileEngineManager {

	/** таблица доступных движков */
	private static final ObjectDictionary<String, FileEngine> ENGINES = DictionaryFactory.newObjectDictionary();

	public static void init() {
		register(new YandexDiskFileEngine());
	}

	/**
	 * Получение соотвествующего двжика по его названию.
	 */
	public static FileEngine get(String name) {
		return ENGINES.get(name);
	}

	private static void register(FileEngine engine) {
		ENGINES.put(engine.getName(), engine);
	}
}
