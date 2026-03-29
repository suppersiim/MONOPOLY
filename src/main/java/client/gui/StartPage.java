package client.gui;

import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import client.Game;

public class StartPage {
    private VBox layout;

    public StartPage(Stage stage) {
        layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("Welcome to Monopoly!");
        TextField nameInput = new TextField();
        nameInput.setPromptText("Name");

        TextField ipInput = new TextField();
        ipInput.setPromptText("IP");

        Button startButton = new Button("Join game");

        startButton.setOnAction(e -> {
            String name = nameInput.getText();
            String ip = ipInput.getText();

            try {
                Game game = new Game(ip, 8080);

                System.out.println(name + " connecting to " + ip);
                BoardView board = new BoardView(game);

                Scene boardScene = new Scene(board, 1000, 700);
                stage.setScene(boardScene);
                stage.centerOnScreen();
            } catch (Exception ex) {
                System.out.println("Error connecting: " + ex.getMessage());
            }
        });

        layout.getChildren().addAll(title, nameInput, ipInput, startButton);
    }

    public VBox getLayout() {
        return layout;
    }
}
