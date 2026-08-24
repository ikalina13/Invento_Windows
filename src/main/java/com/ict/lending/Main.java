package com.ict.lending;

import com.ict.lending.database.DatabaseConnection;
import com.ict.lending.service.AuthService;
import com.ict.lending.service.BackupService;
import com.ict.lending.utils.ThemeManager;
import com.ict.lending.view.LoginView;
import com.ict.lending.view.SplashView;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

/**
 * Entry point for Invento — Device Lending Management System.
 */
public class Main extends Application {

    private final AuthService authService = new AuthService();
    private boolean splashDone;
    private boolean initDone;
    private boolean revealed;
    private Throwable initError;
    private SplashView splashView;
    private LoginView loginView;
    private StackPane bootRoot;

    @Override
    public void start(Stage stage) {
        stage.setTitle("Invento");
        stage.setMinWidth(1100);
        stage.setMinHeight(700);

        var iconStream = getClass().getResourceAsStream("/icons/invento-logo.png");
        if (iconStream != null) {
            stage.getIcons().add(new Image(iconStream));
        }

        bootRoot = new StackPane();
        splashView = new SplashView();
        loginView = new LoginView(authService, stage);
        bootRoot.getChildren().addAll(splashView.getRoot(), loginView.getRoot());

        Scene scene = new Scene(bootRoot, 1100, 720);
        ThemeManager.apply(scene);
        stage.setScene(scene);
        stage.show();

        startBackgroundInit();
        splashView.play(this::onSplashFinished);
    }

    private void startBackgroundInit() {
        Task<Void> initTask = new Task<>() {
            @Override
            protected Void call() {
                DatabaseConnection.getInstance();
                new BackupService().autoBackupIfNeeded();
                return null;
            }
        };
        initTask.setOnSucceeded(e -> {
            initDone = true;
            tryRevealLogin();
        });
        initTask.setOnFailed(e -> {
            initDone = true;
            initError = initTask.getException();
            tryRevealLogin();
        });
        Thread t = new Thread(initTask, "invento-init");
        t.setDaemon(true);
        t.start();
    }

    private void onSplashFinished() {
        splashDone = true;
        tryRevealLogin();
    }

    private void tryRevealLogin() {
        if (!splashDone || !initDone || revealed) {
            return;
        }
        revealed = true;
        Platform.runLater(() -> {
            if (initError != null) {
                initError.printStackTrace();
            }
            loginView.getRoot().setManaged(true);
            loginView.getRoot().setVisible(true);
            bootRoot.layout();
            loginView.playEntrance(() -> bootRoot.getChildren().remove(splashView.getRoot()));
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
