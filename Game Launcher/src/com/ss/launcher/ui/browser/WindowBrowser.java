package com.ss.launcher.ui.browser;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import rlib.ui.util.FXUtils;

/**
 * Реализация оконного браузера.
 * 
 * @author Ronn
 */
public class WindowBrowser extends VBox {

	private final WebView webView;

	public WindowBrowser() {
		this.webView = new WebView();

		final WebEngine engine = webView.getEngine();

		final TextField locationField = new TextField("");
		engine.locationProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				locationField.setText(newValue);
			}
		});

		EventHandler<ActionEvent> goAction = new EventHandler<ActionEvent>() {

			@Override
			public void handle(ActionEvent event) {
				engine.load(locationField.getText().startsWith("http://") ? locationField.getText() : "http://" + locationField.getText());
			}
		};

		locationField.setOnAction(goAction);

		Button goButton = new Button("Перейти");
		goButton.setDefaultButton(true);
		goButton.setOnAction(goAction);

		HBox toolbar = new HBox(locationField, goButton);
		HBox.setHgrow(locationField, Priority.ALWAYS);

		FXUtils.addToPane(toolbar, this);
		FXUtils.addToPane(webView, this);

		webView.minHeightProperty().bind(heightProperty().subtract(toolbar.heightProperty()));
		webView.requestFocus();
	}

	public WebView getWebView() {
		return webView;
	}
}
