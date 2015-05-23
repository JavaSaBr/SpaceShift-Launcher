package com.ss.launcher.tasks;

import static com.ss.launcher.Messages.MAIN_PAGE_STATUS_DOWNLOAD;
import static com.ss.launcher.Messages.MAIN_PAGE_STATUS_UPDATE_CLIENT;
import static com.ss.launcher.Messages.MAIN_PAGE_STATUS_UPDATE_SUCCESSFUL;
import static javafx.application.Platform.runLater;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import rlib.util.FileUtils;
import rlib.util.SafeTask;

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

		final Label progressBarStatus = page.getProgressBarStatus();
		final ProgressBar progressBar = page.getProgressBar();

		try {

			final FileEngine fileEngine = FileEngineManager.get(Config.FILE_ENGINE);
			final String lastVersion = fileEngine.getContent(Config.FILE_CLIENT_LAST_VERSION_URL);

			runLater(() -> progressBarStatus.setText(MAIN_PAGE_STATUS_UPDATE_CLIENT + " " + lastVersion));
			runLater(() -> progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS));

			final Path gameFolder = LauncherUtils.getGameFolder();

			try(DirectoryStream<Path> stream = Files.newDirectoryStream(gameFolder)) {
				stream.forEach(file -> FileUtils.delete(file));
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

					final ZipEntry toPrint = entry;

					runLater(() -> progressBarStatus.setText(MAIN_PAGE_STATUS_DOWNLOAD + " " + toPrint.getName()));

					final Path parent = entryPath.getParent();

					if(!Files.exists(parent)) {
						Files.createDirectories(parent);
					}

					if(!Files.exists(entryPath)) {
						Files.createFile(entryPath);
					}

					long size = entry.getSize();
					long writed = 0;
					long lastUpdate = 0;

					runLater(() -> progressBar.setProgress(0));

					try(OutputStream out = Files.newOutputStream(entryPath)) {
						for(int length = zin.read(buffer); length > 0; length = zin.read(buffer)) {

							out.write(buffer, 0, length);
							writed += length;

							if(writed - lastUpdate < 10000) {
								continue;
							}

							final double progress = writed * 1D / size;

							final String printSize = String.format("%.2f", (size / 1024D / 1024D));
							final String printWrited = String.format("%.2f", (writed / 1024D / 1024D));

							runLater(() -> progressBar.setProgress(progress));
							runLater(() -> progressBarStatus.setText(MAIN_PAGE_STATUS_DOWNLOAD + " " + toPrint.getName() + " (" + printWrited + "Mb / " + printSize + "Mb)"));

							lastUpdate = writed;
						}
					}

					runLater(() -> progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS));
				}

			} catch(IOException e) {
				throw new RuntimeException(e);
			}

			LauncherUtils.updateVersion(lastVersion);

			runLater(() -> progressBarStatus.setText(MAIN_PAGE_STATUS_UPDATE_SUCCESSFUL + " " + lastVersion));

		} catch(Exception e) {
			LauncherUtils.handleException(e);
		} finally {
			runLater(() -> progressBar.setProgress(0));
			runLater(() -> page.updateMainButton());
		}
	}
}
