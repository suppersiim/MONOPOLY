package client.gui;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage primaryStage) {
        StartPage startPage = new StartPage(primaryStage);

        Scene scene = new Scene(startPage.getLayout(), 400, 300);
        primaryStage.setTitle("Monopoly - Start");
        primaryStage.setScene(scene);
        primaryStage.setOnCloseRequest(event -> {
            System.exit(0);
        });
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
