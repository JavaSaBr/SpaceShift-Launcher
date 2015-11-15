package com.ss.launcher.tasks;

import com.ss.launcher.Config;
import com.ss.launcher.file.engine.FileEngine;
import com.ss.launcher.file.engine.FileEngineManager;
import com.ss.launcher.ui.page.MainUIPage;
import com.ss.launcher.util.LauncherUtils;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import rlib.util.FileUtils;
import rlib.util.SafeTask;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.ss.launcher.Messages.*;
import static javafx.application.Platform.runLater;

/**
 * Реализация задачи по скачиванию клиента.
 *
 * @author Ronn
 */
public class DownloadClientTask implements SafeTask {

    /**
     * Главная страница лаунчера.
     */
    private final MainUIPage page;

    public DownloadClientTask(MainUIPage page) {
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

            final FileEngine fileEngine = FileEngineManager.get(Config.fileEngine);
            final String lastVersion = fileEngine.getContent(Config.fileClientLastVersionUrl);

            runLater(() -> progressBarStatus.setText(MAIN_PAGE_STATUS_PREPARE_DOWNLOAD + " " + lastVersion));
            runLater(() -> progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS));

            final Path gameFolder = LauncherUtils.getGameFolder();

            try (final DirectoryStream<Path> stream = Files.newDirectoryStream(gameFolder)) {
                stream.forEach(FileUtils::delete);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            byte[] buffer = new byte[512];

            try (ZipInputStream zin = new ZipInputStream(fileEngine.getInputStream(Config.fileClientUrl))) {

                for (ZipEntry entry = zin.getNextEntry(); entry != null && zin.available() != -1; entry = zin.getNextEntry()) {

                    final Path entryPath = Paths.get(gameFolder.toString(), entry.getName());

                    if (entry.isDirectory()) {
                        Files.createDirectories(entryPath);
                        continue;
                    }

                    final ZipEntry toPrint = entry;

                    runLater(() -> progressBarStatus.setText(MAIN_PAGE_STATUS_DOWNLOAD + " " + toPrint.getName()));

                    final Path parent = entryPath.getParent();

                    if (!Files.exists(parent)) {
                        Files.createDirectories(parent);
                    }

                    if (!Files.exists(entryPath)) {
                        Files.createFile(entryPath);
                    }

                    long size = entry.getSize();
                    long wrote = 0;
                    long lastUpdate = 0;

                    runLater(() -> progressBar.setProgress(0));

                    try (final OutputStream out = Files.newOutputStream(entryPath)) {
                        for (int length = zin.read(buffer); length > 0; length = zin.read(buffer)) {

                            out.write(buffer, 0, length);
                            wrote += length;

                            if (wrote - lastUpdate < 10000) {
                                continue;
                            }

                            final double progress = wrote * 1D / size;

                            final String printSize = String.format("%.2f", (size / 1024D / 1024D));
                            final String printWrote = String.format("%.2f", (wrote / 1024D / 1024D));

                            runLater(() -> progressBar.setProgress(progress));
                            runLater(() -> progressBarStatus.setText(MAIN_PAGE_STATUS_DOWNLOAD + " " + toPrint.getName() + " (" + printWrote + "Mb / " + printSize + "Mb)"));

                            lastUpdate = wrote;
                        }
                    }

                    runLater(() -> progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS));
                }

            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            LauncherUtils.updateVersion(lastVersion);

            runLater(() -> progressBarStatus.setText(MAIN_PAGE_STATUS_DOWNLOAD_SUCCESSFUL));

        } catch (Exception e) {
            LauncherUtils.handleException(e);
        } finally {
            runLater(() -> progressBar.setProgress(0));
            runLater(page::updateMainButton);
        }
    }
}
