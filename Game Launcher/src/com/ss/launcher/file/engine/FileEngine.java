package com.ss.launcher.file.engine;

import java.io.InputStream;

/**
 * Интерфейс для реализации движка по вытягиванию данных необходимых для
 * проверки и обновления клиента.
 *
 * @author Ronn
 */
public interface FileEngine {

    public String getContent(String url);

    public InputStream getInputStream(String url);

    public String getName();
}
