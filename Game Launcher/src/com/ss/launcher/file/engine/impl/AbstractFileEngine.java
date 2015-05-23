package com.ss.launcher.file.engine.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClients;

import com.ss.launcher.file.engine.FileEngine;

/**
 * Базовая реализации файлового движка.
 * 
 * @author Ronn
 */
public abstract class AbstractFileEngine implements FileEngine {

	private static final ThreadLocal<HttpClient> LOCAL_HTTP_CLIENT = new ThreadLocal<HttpClient>() {

		@Override
		protected HttpClient initialValue() {
			return HttpClients.createDefault();
		}
	};

	protected HttpClient getHttpClient() {
		return LOCAL_HTTP_CLIENT.get();
	}

	public String buildUrl(String baseUrl, String... params) {

		StringBuilder builder = new StringBuilder(baseUrl);

		if(params == null || params.length < 2) {
			return baseUrl.toString();
		}

		builder.append('?');

		for(int i = 0, length = params.length; i < length - 1; i += 2) {

			builder.append(params[i]).append('=');

			try {
				builder.append(URLEncoder.encode(params[i + 1], "UTF-8"));
			} catch(UnsupportedEncodingException e) {
				builder.append(params[i + 1]);
			}

			builder.append('&');
		}

		final int lastIndex = builder.length() - 1;

		if(builder.charAt(lastIndex) == '&') {
			builder.deleteCharAt(lastIndex);
		}

		return builder.toString();
	}
}
