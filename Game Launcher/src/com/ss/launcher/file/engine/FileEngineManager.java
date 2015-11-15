package com.ss.launcher.file.engine;

import com.ss.launcher.file.engine.yandex.disk.YandexDiskFileEngine;
import rlib.util.dictionary.DictionaryFactory;
import rlib.util.dictionary.ObjectDictionary;

/**
 * Менеджер файловых движков.
 *
 * @author Ronn
 */
public class FileEngineManager {

    /**
     * Таблица доступных движков.
     */
    private static final ObjectDictionary<String, FileEngine> ENGINES = DictionaryFactory.newObjectDictionary();

    /**
     * Получение соотвествующего двжика по его названию.
     */
    public static FileEngine get(String name) {
        return ENGINES.get(name);
    }

    public static void init() {
        register(new YandexDiskFileEngine());
    }

    private static void register(FileEngine engine) {
        ENGINES.put(engine.getName(), engine);
    }
}
