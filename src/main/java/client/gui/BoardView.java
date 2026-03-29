package client.gui;

import common.GamePacket;
import common.GameState;
import common.PacketType;
import game_logic.Player;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import client.Game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BoardView extends BorderPane {
    private final Game game;
    private GridPane grid;
    private List<StackPane> spaces = new ArrayList<>();
    private List<Circle> playerTokens = new ArrayList<>();
    private Label statusLabel;

    public BoardView(Game game) {
        this.game = game;
        grid = new GridPane();
        setupBoard();
        this.setCenter(grid);

        // Right panel
        VBox controls = new VBox(20);
        controls.setAlignment(Pos.CENTER);
        controls.setPrefWidth(200);

        statusLabel = new Label("Your turn");
        Button rollButton = new Button("Roll dice");

        //send to server
        rollButton.setOnAction(e -> {
            System.out.println("Rolling...");
            try {
                game.getClient().send(new GamePacket(PacketType.ROLL, new byte[0]));
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        controls.getChildren().addAll(statusLabel, rollButton);
        this.setRight(controls);
    }

    private void setupBoard() {
        for (int i = 0; i < 40; i++) {
            StackPane space = createSpace(i);
            spaces.add(space);


            int col = 0, row = 0;
            if (i <= 10) { row = 10; col = 10 - i; }        // bottom row
            else if (i <= 20) { col = 0; row = 10 - (i - 10); } // left row
            else if (i <= 30) { row = 0; col = i - 20; }        // top row
            else { col = 10; row = i - 30; }                   // right row

            grid.add(space, col, row);
        }

        // LOGO
        Label logo = new Label("MONOPOLY");
        logo.setStyle("-fx-font-size: 40px; -fx-font-weight: bold;");
        grid.add(logo, 1, 1, 9, 9);
        GridPane.setHalignment(logo, javafx.geometry.HPos.CENTER);
    }

    private StackPane createSpace(int index) {
        StackPane pane = new StackPane();
        Rectangle rect = new Rectangle(60, 60);
        rect.setFill(Color.WHITE);
        rect.setStroke(Color.BLACK);

        Label label = new Label(String.valueOf(index));
        pane.getChildren().addAll(rect, label);
        return pane;
    }

    // update pieces on the board
    public void update(GameState state) {
        // remove old
        for (Circle token : playerTokens) {
            ((StackPane)token.getParent()).getChildren().remove(token);
        }
        playerTokens.clear();

        // add new
        for (Player p : state.getPlayers()) {
            Circle token = new Circle(10, getPlayerColor(state.getPlayers().indexOf(p)));
            playerTokens.add(token);
            spaces.get(p.getLocation()).getChildren().add(token);
        }

    }

    private Color getPlayerColor(int index) {
        switch (index) {
            case 0: return Color.RED;
            case 1: return Color.BLUE;
            case 2: return Color.GREEN;
            default: return Color.YELLOW;
        }
    }
}