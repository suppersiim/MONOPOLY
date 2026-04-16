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
import java.util.Objects;
import java.util.Optional;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

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
    private ListView<String> eventLog;

    public BoardView(Game game) {
        this.game = game;
        grid = new GridPane();

        grid.setMaxSize(600, 600);

        StackPane boardPane = new StackPane();
        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/monopoly-board.png")));
            ImageView imageView = new ImageView(image);
            imageView.setFitWidth(600);
            imageView.setFitHeight(600);
            boardPane.getChildren().add(imageView);
        } catch (Exception e) {
            System.err.println("Could not load board image: " + e.getMessage());
        }
        boardPane.getChildren().add(grid);

        setupBoard();
        this.setCenter(boardPane);

        // Right panel
        VBox controls = new VBox(20);
        controls.setAlignment(Pos.CENTER);
        controls.setPrefWidth(300);

        statusLabel = new Label("Your turn");
        diceLabel = new Label("Dice: * - *");
        rollButton = new Button("Roll dice");
        moneyLabel = new Label("Money: ");
        currentSquareLabel = new Label("Current square: Go");

        eventLog = new ListView<>();
        eventLog.setPrefSize(400, 200);
        eventLog.setPlaceholder(new Label("No events yet"));
        Label logTitle = new Label("Activity Log");
        logTitle.setStyle("-fx-font-weight: bold;");

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

        // Register callback
        game.getClient().getPacketHandler().setOnEventLog(msg ->
                Platform.runLater(() -> {
                    eventLog.getItems().add(0, msg);  // newest at top
                    if (eventLog.getItems().size() > 50) {
                        eventLog.getItems().remove(50); // keep last 50
                    }
                })
        );


        controls.getChildren().addAll(statusLabel, diceLabel, moneyLabel, currentSquareLabel, rollButton, eventLog);
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
        for (int i = 0; i < 11; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            double size = (i == 0 || i == 10) ? (200.0 / 13.0) : (100.0 / 13.0);
            colConst.setPercentWidth(size);
            grid.getColumnConstraints().add(colConst);

            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(size);
            grid.getRowConstraints().add(rowConst);
        }

        for (int i = 0; i < 40; i++) {
            StackPane space = createSpace(i);
            spaces.add(space);

            int col, row;
            if (i <= 10) { row = 10; col = 10 - i; }        // bottom row
            else if (i <= 20) { col = 0; row = 10 - (i - 10); } // left row
            else if (i <= 30) { row = 0; col = i - 20; }        // top row
            else { col = 10; row = i - 30; }                   // right row

            grid.add(space, col, row);
        }
    }

    private StackPane createSpace(int index) {
        StackPane pane = new StackPane();
        return pane;
    }

    // update pieces on the board
    public void update(GameState state) {
        System.out.println("Updating board...");
        // remove old
        for (Circle token : playerTokens) {
            ((Pane)token.getParent()).getChildren().remove(token);
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
