package client.gui;

import game_logic.GameState;
import game_logic.OwnableSquare.Street;
import game_logic.Player;
import game_logic.Square;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import client.Game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BoardView extends BorderPane {
    private final Game game;
    private GridPane grid;
    private List<StackPane> spaces = new ArrayList<>();
    private List<Circle> playerTokens = new ArrayList<>();
    private Label statusLabel;
    private Label diceLabel;
    private Label moneyLabel;
    private Label currentSquareLabel;
    private Button rollButton;

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
        diceLabel = new Label("Dice: * - *");
        rollButton = new Button("Roll dice");
        moneyLabel = new Label("Money: ");
        currentSquareLabel = new Label("Current square: Go");

        //send to server
        rollButton.setOnAction(e -> {
            System.out.println("Rolling...");
            try {
                game.getClient().sendRoll();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        // Game state updates
        game.getClient().getPacketHandler().setOnGameStateUpdate(
                state -> Platform.runLater(() -> update(state)));

        // Buy offer dialog — only shown to the player who landed on the property
        game.getClient().getPacketHandler().setOnBuyOffer((propertyName, price) ->
                Platform.runLater(() -> showBuyDialog(propertyName, price)));

        controls.getChildren().addAll(statusLabel, diceLabel, moneyLabel, currentSquareLabel, rollButton);
        this.setRight(controls);

        update(game.getGameState());
    }

    /**
     * Shows a confirmation dialog asking whether the player wants to buy the property.
     * Sends the response to the server regardless of what the player chooses.
     */
    private void showBuyDialog(String propertyName, int price) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Buy Property?");
        alert.setHeaderText(propertyName);
        alert.setContentText("Would you like to buy " + propertyName + " for $" + price + "?");

        ButtonType buyButton = new ButtonType("Buy ($" + price + ")");
        ButtonType skipButton = new ButtonType("Skip", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(buyButton, skipButton);

        Optional<ButtonType> result = alert.showAndWait();
        boolean accepted = result.isPresent() && result.get() == buyButton;

        try {
            game.getClient().sendBuyResponse(accepted);
        } catch (IOException e) {
            System.out.println("Error sending buy response: " + e.getMessage());
        }
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

        Square square = game.getGameState().getSquare(index);
        Label label = new Label(square.getName());
        label.setMaxSize(50, 50);
        label.setWrapText(true);
        pane.getChildren().addAll(rect, label);
        if (square instanceof Street street) {
            Rectangle colorBar = new Rectangle(59, 10);
            colorBar.setFill(Color.valueOf(street.getColor()));
            pane.getChildren().add(colorBar);
            StackPane.setAlignment(colorBar, Pos.TOP_CENTER);
        }
        return pane;
    }

    // update pieces on the board
    public void update(GameState state) {
        System.out.println("Updating board...");
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

        if (state.getCurrentPlayer().getName().equals(game.getPlayerName())) {
            statusLabel.setText("Your turn");
            rollButton.setDisable(false);
        } else {
            statusLabel.setText(state.getCurrentPlayer().getName() + "'s turn");
            rollButton.setDisable(true);
        }

        diceLabel.setText("Dice: " + state.getDice()[0] + " - " + state.getDice()[1]);
        Player myPlayer = state.getPlayerByName(game.getPlayerName());
        moneyLabel.setText("Money: " + myPlayer.getMoney());
        currentSquareLabel.setText("Current square: " + game.getGameState().getSquare(myPlayer.getLocation()).getName());
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