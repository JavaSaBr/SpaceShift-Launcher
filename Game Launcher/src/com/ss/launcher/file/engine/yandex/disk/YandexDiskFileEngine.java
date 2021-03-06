package com.ss.launcher.file.engine.yandex.disk;

import com.ss.launcher.file.engine.impl.AbstractFileEngine;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import static com.ss.launcher.Messages.RUNTIME_EXCEPTION_MESSAGE_CANT_DOWNLOAD;
import static com.ss.launcher.util.LauncherUtils.readStream;

/**
 * Реализация файлового движка с использованием Yandex.Disk.
 *
 * @author Ronn
 */
public class YandexDiskFileEngine extends AbstractFileEngine {

    public static final String PROP_BASE_URL = "https://cloud-api.yandex.net/v1/disk/public/resources/download";
    public static final String PARAM_PUBLIC_KEY = "public_key";

    @Override
    public String getContent(final String url) {
        return readStream(getInputStream(url));
    }

    @Override
    public InputStream getInputStream(final String url) {

        final String targetUrl = buildUrl(PROP_BASE_URL, PARAM_PUBLIC_KEY, url);
        final CloseableHttpClient httpClient = getHttpClient();
        try {

            HttpResponse response = httpClient.execute(new HttpGet(targetUrl));
            HttpEntity entity = response.getEntity();

            final String jsonString = readStream(entity.getContent());
            final JSONObject object = new JSONObject(jsonString);

            if (!object.has("href")) {
                throw new RuntimeException(RUNTIME_EXCEPTION_MESSAGE_CANT_DOWNLOAD);
            }

            final String href = object.getString("href");

            response = httpClient.execute(new HttpGet(href));
            entity = response.getEntity();

            return entity.getContent();

        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "Yandex.Disk";
    }
}
