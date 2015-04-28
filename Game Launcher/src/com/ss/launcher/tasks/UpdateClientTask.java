package com.ss.launcher.tasks;

import static com.ss.launcher.util.LauncherUtils.FOLDER_LOG;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import rlib.util.FileUtils;
import rlib.util.SafeTask;
import rlib.util.StringUtils;

import com.ss.launcher.Config;
import com.ss.launcher.file.engine.FileEngine;
import com.ss.launcher.file.engine.FileEngineManager;
import com.ss.launcher.ui.page.MainUIPage;
import com.ss.launcher.util.LauncherUtils;

/**
 * Реализация задачи по обновлению клиента.
 * 
 * @author Ronn
 */
public class UpdateClientTask implements SafeTask {

	/** главная страница лаунчера */
	private final MainUIPage page;

	public UpdateClientTask(MainUIPage page) {
		this.page = page;
	}

	/**
	 * @return главная страница лаунчера.
	 */
	private MainUIPage getPage() {
		return page;
	}

	@Override
	public void runImpl() {

		final MainUIPage page = getPage();

		final Button playButton = page.getPlayButton();
		final Button updateClientButton = page.getUpdateClientButton();

		final Label progressBarStatus = page.getProgressBarStatus();
		final ProgressBar progressBar = page.getProgressBar();

		try {

			Platform.runLater(() -> {
				progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
				progressBarStatus.setText("Проверка наличия новой версии...");
			});

			final FileEngine fileEngine = FileEngineManager.get(Config.FILE_ENGINE);

			final String lastVersion = fileEngine.getContent(Config.FILE_LAST_VERSION_URL);
			final String currentVersion = LauncherUtils.getCurrentVersion();

			if(StringUtils.equals(lastVersion, currentVersion)) {
				Platform.runLater(() -> {
					progressBar.setProgress(0);
					progressBarStatus.setText("У вас последняя версия клиента");
				});
				return;
			}

			Platform.runLater(() -> progressBarStatus.setText("Обновление клиента до версии " + lastVersion));

			final Path gameFolder = LauncherUtils.getGameFolder();

			try(DirectoryStream<Path> stream = Files.newDirectoryStream(gameFolder)) {

				for(Path file : stream) {

					final String fileName = file.getFileName().toString();

					if(FOLDER_LOG.equals(fileName)) {
						continue;
					}

					FileUtils.delete(file);
				}

			} catch(IOException e) {
				throw new RuntimeException(e);
			}

			byte[] buffer = new byte[512];

			try(ZipInputStream zin = new ZipInputStream(fileEngine.getInputStream(Config.FILE_CLIENT_URL))) {

				for(ZipEntry entry = zin.getNextEntry(); entry != null && zin.available() != -1; entry = zin.getNextEntry()) {

					final Path entryPath = Paths.get(gameFolder.toString(), entry.getName());

					if(entry.isDirectory()) {
						Files.createDirectories(entryPath);
						continue;
					}

					final Path parent = entryPath.getParent();

					if(!Files.exists(parent)) {
						Files.createDirectories(parent);
					}

					if(!Files.exists(entryPath)) {
						Files.createFile(entryPath);
					}

					try(OutputStream out = Files.newOutputStream(entryPath)) {
						for(int length = zin.read(buffer); length > 0; length = zin.read(buffer)) {
							out.write(buffer, 0, length);
						}
					}

					final ZipEntry toPrint = entry;

					Platform.runLater(() -> progressBarStatus.setText("Обновлен файл: " + toPrint.getName()));
				}

			} catch(IOException e) {
				throw new RuntimeException(e);
			}

			LauncherUtils.updateVersion(lastVersion);

			Platform.runLater(() -> progressBarStatus.setText("Клиент был успешно обновлен до версии " + lastVersion));

		} catch(Exception e) {
			Platform.runLater(() -> {
				final Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText(e.getMessage());
				alert.setTitle("Ошибка");
				alert.setHeaderText("Во время обновления клиента произошла ошибка");
				alert.showAndWait();
			});

		} finally {
			Platform.runLater(() -> {
				progressBar.setProgress(0);
				playButton.setDisable(false);
				updateClientButton.setDisable(false);
			});
		}
	}
}
