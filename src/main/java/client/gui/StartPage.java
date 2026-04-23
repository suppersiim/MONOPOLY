package client.gui;

import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import client.Game;

import java.io.IOException;

public class StartPage {
    private VBox layout;

    public StartPage(Stage stage) {
        layout = new VBox(10);
        layout.setAlignment(Pos.CENTER);

        Label title = new Label("Welcome to Monopoly!");

        Label playerCountLabel = new Label("Players: 0");

        TextField nameInput = new TextField();
        nameInput.setPromptText("Name");
        nameInput.setText("Player" + (int)(Math.random() * 100));

        TextField ipInput = new TextField();
        ipInput.setPromptText("IP");
        ipInput.setText("localhost");

        Button joinButton = new Button("Join game");
        Button startButton = new Button("Start game");
        startButton.setDisable(true);

        joinButton.setOnAction(e -> {
            String name = nameInput.getText();
            String ip = ipInput.getText();

            try {
                System.out.println(name + " connecting to " + ip);
                Game.createInstance(ip, 8080, name);
                Game game = Game.getInstance();
                game.connect();
                game.getClient().getPacketHandler().setOnJoinedPlayersCount(count -> {
                    Platform.runLater(() -> {
                        playerCountLabel.setText("Players: " + count);
                        if (count == 1)
                            startButton.setDisable(false);
                    });
                });

                game.getClient().getPacketHandler().setOnGameStateUpdate(count -> {
                    Platform.runLater(() -> {
                        BoardView board = new BoardView(game);

                        Scene boardScene = new Scene(board, 1000, 700);
                        stage.setScene(boardScene);
                        stage.centerOnScreen();
                    });
                });

                game.getClient().sendJoinGame();
                joinButton.setDisable(true);
            } catch (Exception ex) {
                System.out.println("Error connecting: " + ex.getMessage());
            }
        });

        startButton.setOnAction(e -> {
            try {
                Game game = Game.getInstance();

                game.getClient().sendStartGame();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        layout.getChildren().addAll(title, playerCountLabel, nameInput, ipInput, joinButton, startButton);
    }

    public VBox getLayout() {
        return layout;
    }
}
