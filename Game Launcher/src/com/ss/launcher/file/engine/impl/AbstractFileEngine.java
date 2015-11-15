package com.ss.launcher.file.engine.impl;

import com.ss.launcher.file.engine.FileEngine;
import com.ss.launcher.util.LauncherUtils;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Базовая реализации файлового движка.
 *
 * @author Ronn
 */
public abstract class AbstractFileEngine implements FileEngine {

    public String buildUrl(String baseUrl, String... params) {

        StringBuilder builder = new StringBuilder(baseUrl);

        if (params == null || params.length < 2) {
            return builder.toString();
        }

        builder.append('?');

        for (int i = 0, length = params.length; i < length - 1; i += 2) {

            builder.append(params[i]).append('=');

            try {
                builder.append(URLEncoder.encode(params[i + 1], "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                builder.append(params[i + 1]);
            }

            builder.append('&');
        }

        final int lastIndex = builder.length() - 1;

        if (builder.charAt(lastIndex) == '&') {
            builder.deleteCharAt(lastIndex);
        }

        return builder.toString();
    }

    protected CloseableHttpClient getHttpClient() {
        return LauncherUtils.createHttpClient();
    }
}
