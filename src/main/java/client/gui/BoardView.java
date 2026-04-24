package client.gui;

import game_logic.GameState;
import game_logic.OwnableSquare.OwnableSquare;
import game_logic.OwnableSquare.Street;
import game_logic.Player;
import game_logic.Square;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Node;
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
import javafx.beans.binding.Bindings;
import javafx.beans.binding.NumberBinding;
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
    private Button buyHouseButton;
    private Button mortgageButton;
    private ListView<String> eventLog;
    private VBox playerStatsBox;

    public BoardView(Game game) {
        this.game = game;
        grid = new GridPane();

        StackPane boardPane = new StackPane();
        boardPane.setMaxSize(700, 700); // cap max board image size
        boardPane.setMinSize(0, 0);     // allow it to scale down

        NumberBinding minSide = Bindings.min(boardPane.widthProperty(), boardPane.heightProperty());
        grid.maxWidthProperty().bind(minSide);
        grid.maxHeightProperty().bind(minSide);

        try {
            Image image = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/monopoly-board.png")));
            ImageView imageView = new ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.fitWidthProperty().bind(minSide);
            imageView.fitHeightProperty().bind(minSide);
            boardPane.getChildren().add(imageView);
        } catch (Exception e) {
            System.err.println("Could not load board image: " + e.getMessage());
        }
        boardPane.getChildren().add(grid);

        setupBoard();
        this.setCenter(boardPane);

        // Left overview panel
        VBox leftPanel = new VBox(10);
        leftPanel.setAlignment(Pos.TOP_CENTER);
        leftPanel.setPrefWidth(200);

        playerStatsBox = new VBox(8);
        playerStatsBox.setAlignment(Pos.TOP_LEFT);
        playerStatsBox.setStyle("-fx-padding: 10;");

        leftPanel.getChildren().add(playerStatsBox);
        this.setLeft(leftPanel);

        // Right panel
        VBox controls = new VBox(20);
        controls.setAlignment(Pos.CENTER);
        controls.setPrefWidth(300);

        statusLabel = new Label("Your turn");
        diceLabel = new Label("Dice: * - *");
        rollButton = new Button("Roll dice");
        buyHouseButton = new Button("Buy house");
        mortgageButton = new Button("Mortgage");
        moneyLabel = new Label("Money: ");
        currentSquareLabel = new Label("Current square: Go");

        eventLog = new ListView<>();
        eventLog.setPrefSize(400, 200);
        eventLog.setPlaceholder(new Label("No events yet"));
        Label logTitle = new Label("Activity Log");
        logTitle.setStyle("-fx-font-weight: bold;");

        //send to server
        rollButton.setOnAction(e -> {
            rollButton.setDisable(true);
            System.out.println("Rolling...");
            try {
                game.getClient().sendRoll();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        });

        buyHouseButton.setOnAction(e -> showBuyHouseDialog());
        mortgageButton.setOnAction(e -> showMortgageDialog());

        // Game state updates
        game.getClient().getPacketHandler().setOnGameStateUpdate(
                state -> Platform.runLater(() -> update(state)));

        // Buy offer dialog — only shown to the player who landed on the property
        game.getClient().getPacketHandler().setOnBuyOffer((propertyName, price) ->
                Platform.runLater(() -> showBuyDialog(propertyName, price)));

        // Buy a house for your property
        game.getClient().getPacketHandler().setOnBuyHouseOffer((propertyName, price) ->
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


        controls.getChildren().addAll(statusLabel, diceLabel, moneyLabel, currentSquareLabel, rollButton, buyHouseButton,mortgageButton, eventLog);
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

    public void auction(String propertyName, int price){
        // TODO: get all other players and make an auction between them
        // TODO: pop-up window where there is 10s to place a bid and if someone overbids then there is up to 5s time for others to then overbid that guy
    }

    private void showBuyHouseDialog() {
        Player currentPlayer = game.getGameState().getCurrentPlayer();
        List<Street> availableStreets = getAvailableStreets(currentPlayer);

        if (availableStreets.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No available properties");
            alert.setContentText("You have no complete street color sets to buy houses for.");
            alert.showAndWait();
            return;
        }

        Dialog<Street> dialog = buildHouseDialog(availableStreets);
        dialog.showAndWait().ifPresent(chosen -> {
            System.out.println("chosen street: " + chosen);
            try {
                game.getClient().sendBuyHouseResponse(true, chosen.getName());
            } catch (IOException ex) {
                System.out.println("Error sending buy house response: " + ex.getMessage());
            }
        });
    }

    private void showMortgageDialog() {
        Player currentPlayer = game.getGameState().getCurrentPlayer();
        List<OwnableSquare> properties = currentPlayer.getProperties();

        if (properties.isEmpty()) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("No properties");
            alert.setContentText("You have no properties to mortgage.");
            alert.showAndWait();
            return;
        }

        Dialog<OwnableSquare> dialog = buildMortgageDialog(properties);
        dialog.showAndWait().ifPresent(chosen -> {
            try {
                game.getClient().sendMortgageRequest(chosen.getName());
            } catch (IOException ex) {
                System.out.println("Error sending mortgage request: " + ex.getMessage());
            }
        });
    }

    private Dialog<Street> buildHouseDialog(List<Street> streets) {
        Dialog<Street> dialog = new Dialog<>();
        dialog.setTitle("Buy House");
        dialog.setHeaderText("Select a street to build a house on");

        ButtonType buyButton = new ButtonType("Buy House", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(buyButton, cancelButton);

        ListView<Street> listView = new ListView<>();
        listView.getItems().addAll(streets);
        listView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Street street, boolean empty) {
                super.updateItem(street, empty);
                if (empty || street == null) setText(null);
                else setText(street.getName() + " — Houses: " + street.getNumberOfHouses() + " — Price: $" + street.getHousePrice());
            }
        });
        listView.setPrefHeight(200);

        Node buyHouseButton = dialog.getDialogPane().lookupButton(buyButton);
        buyHouseButton.setDisable(true);
        listView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> buyHouseButton.setDisable(selected == null)
        );

        dialog.getDialogPane().setContent(listView);
        dialog.setResultConverter(button -> button == buyButton ? listView.getSelectionModel().getSelectedItem() : null);
        return dialog;
    }

    private Dialog<OwnableSquare> buildMortgageDialog(List<OwnableSquare> properties) {
        Dialog<OwnableSquare> dialog = new Dialog<>();
        dialog.setTitle("Mortgage");
        dialog.setHeaderText("Select a property to mortgage/unmortgage");
        dialog.getDialogPane().setPrefWidth(500);

        ButtonType confirmButton = new ButtonType("Confirm", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(confirmButton, cancelButton);

        ListView<OwnableSquare> listView = new ListView<>();
        listView.getItems().addAll(properties);
        listView.setPrefHeight(250);

        Node confirmBtn = dialog.getDialogPane().lookupButton(confirmButton);
        confirmBtn.setDisable(true);
        listView.getSelectionModel().selectedItemProperty().addListener(
                (obs, old, selected) -> confirmBtn.setDisable(selected == null)
        );

        dialog.getDialogPane().setContent(listView);
        dialog.setResultConverter(btn -> btn == confirmButton ? listView.getSelectionModel().getSelectedItem() : null);
        return dialog;
    }

    private List<Street> getAvailableStreets(Player player) {
        List<Street> streets = new ArrayList<>();
        for (OwnableSquare property : player.getProperties()) {
            if (property instanceof Street street && street.isColorSetComplete() && street.getNumberOfHouses() < 5) {
                streets.add(street);
            }
        }
        return streets;
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

        playerStatsBox.getChildren().clear();
        for (int i = 0; i < state.getPlayers().size(); i++) {
            Player p = state.getPlayers().get(i);
            HBox playerInfo = new HBox(10);
            playerInfo.setAlignment(Pos.CENTER_LEFT);
            Circle colorIndicator = new Circle(7, getPlayerColor(i));
            Label infoLabel = new Label(p.getName() + ": $" + p.getMoney());
            playerInfo.getChildren().addAll(colorIndicator, infoLabel);
            playerStatsBox.getChildren().add(playerInfo);
        }

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
            buyHouseButton.setDisable(false);
        } else {
            statusLabel.setText(state.getCurrentPlayer().getName() + "'s turn");
            rollButton.setDisable(true);
            buyHouseButton.setDisable(true);
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
