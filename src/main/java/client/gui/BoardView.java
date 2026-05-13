package client.gui;

import common.GamePacket;
import common.PacketType;
import game_logic.GameState;
import game_logic.OwnableSquare.OwnableSquare;
import game_logic.OwnableSquare.Street;
import game_logic.Player;
import game_logic.Square;
import javafx.application.Platform;
import javafx.event.ActionEvent;
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
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

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
    private Button tradeButton;
    private Button finishTurnButton;
    private ListView<String> eventLog;
    private VBox playerStatsBox;

    private Dialog<Void> activeTradeOfferDialog;

    public record TradeInfo(
        long tradeUID,
        String offerer,
        int offerMoney,
        int requestMoney,
        String[] offerProperties,
        String[] requestProperties
    ) {}

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
        mortgageButton = new Button("Mortgage / Unmortgage");
        tradeButton = new Button("Trade");
        finishTurnButton = new Button("End Turn");
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
        tradeButton.setOnAction(e -> showTradeDialog());
        mortgageButton.setOnAction(e -> showMortgageDialog());
        finishTurnButton.setOnAction(e -> {
            try {
                game.getClient().send(new GamePacket(PacketType.CLIENT_FINISH_TURN, new byte[0]));
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

        // Buy a house for your property
        game.getClient().getPacketHandler().setOnBuyHouseOffer((propertyName, price) ->
                Platform.runLater(() -> showBuyDialog(propertyName, price)));

        // Incoming trade offer dialog
        game.getClient().getPacketHandler().setOnTradeOffer(tradeInfo ->
                Platform.runLater(() -> showIncomingTradeDialog(tradeInfo)));

        // Register callback
        game.getClient().getPacketHandler().setOnEventLog(msg ->
                Platform.runLater(() -> {
                    eventLog.getItems().add(0, msg);  // newest at top
                    if (eventLog.getItems().size() > 50) {
                        eventLog.getItems().remove(50); // keep last 50
                    }
                })
        );

        game.getClient().getPacketHandler().setOnTradeResponse(accepted ->
                Platform.runLater(() -> {
                    if (activeTradeOfferDialog != null) {
                        activeTradeOfferDialog.setResult(null);
                        activeTradeOfferDialog.close();
                        activeTradeOfferDialog = null;
                    }
                    Alert alert = new Alert(accepted ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
                    alert.setTitle("Trade Result");
                    alert.setHeaderText(accepted ? "Trade Accepted" : "Trade Rejected");
                    alert.setContentText(accepted ? "The trade was accepted!" : "The trade was rejected.");
                    alert.showAndWait();
                })
        );

        controls.getChildren().addAll(statusLabel, diceLabel, moneyLabel, currentSquareLabel, rollButton, buyHouseButton, mortgageButton, tradeButton, finishTurnButton, eventLog);
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
            alert.setContentText("You have no streets to build a house on.");
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
            alert.setContentText("You have no properties to mortgage or unmortgage.");
            alert.showAndWait();
            return;
        }

        List<OwnableSquare> unmortgaged = new ArrayList<>();
        List<OwnableSquare> mortgaged = new ArrayList<>();
        for (OwnableSquare p : properties) {
            if (p.isMortgaged()) mortgaged.add(p);
            else unmortgaged.add(p);
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Mortgage / Unmortgage");
        dialog.setHeaderText("Select a property to mortgage or unmortgage.");
        dialog.getDialogPane().setPrefWidth(520);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);

        VBox content = new VBox(12);
        content.setStyle("-fx-padding: 10;");

        if (!unmortgaged.isEmpty()) {
            Label mortgageTitle = new Label("Mortgage (receive half price):");
            mortgageTitle.setStyle("-fx-font-weight: bold;");
            content.getChildren().add(mortgageTitle);
            for (OwnableSquare prop : unmortgaged) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                Label lbl = new Label(prop.getName() + "  →  +$" + prop.getMortgageValue());
                lbl.setMinWidth(280);
                Button btn = new Button("Mortgage");
                btn.setOnAction(ev -> {
                    try { game.getClient().sendMortgageRequest(prop.getName()); }
                    catch (IOException ex) { ex.printStackTrace(); }
                    dialog.close();
                });
                row.getChildren().addAll(lbl, btn);
                content.getChildren().add(row);
            }
        }

        if (!mortgaged.isEmpty()) {
            Label unmortgageTitle = new Label("Unmortgage (pay back with 10% interest):");
            unmortgageTitle.setStyle("-fx-font-weight: bold; -fx-padding: 8 0 0 0;");
            content.getChildren().add(unmortgageTitle);
            for (OwnableSquare prop : mortgaged) {
                HBox row = new HBox(10);
                row.setAlignment(Pos.CENTER_LEFT);
                Label lbl = new Label(prop.getName() + "  →  -$" + prop.getUnmortgageCost() + "  [MORTGAGED]");
                lbl.setMinWidth(280);
                lbl.setStyle("-fx-text-fill: gray;");
                Button btn = new Button("Unmortgage");
                boolean canAfford = game.getGameState().getPlayerByName(game.getPlayerName()).getMoney() >= prop.getUnmortgageCost();
                btn.setDisable(!canAfford);
                btn.setOnAction(ev -> {
                    try { game.getClient().sendUnmortgageRequest(prop.getName()); }
                    catch (IOException ex) { ex.printStackTrace(); }
                    dialog.close();
                });
                row.getChildren().addAll(lbl, btn);
                content.getChildren().add(row);
            }
        }

        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setPrefHeight(350);
        dialog.getDialogPane().setContent(scroll);
        dialog.showAndWait();
    }

    private void showTradeDialog() {
        Player currentPlayer = game.getGameState().getCurrentPlayer();

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Trade");
        dialog.setHeaderText("Create trade offer");
        dialog.getDialogPane().setPrefWidth(600);

        ButtonType offerButtonType = new ButtonType("Make Offer", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(offerButtonType, ButtonType.CANCEL);

        ComboBox<Player> targetPlayerSelect = new ComboBox<>();
        for (Player p : game.getGameState().getPlayers()) {
            if (p != currentPlayer) targetPlayerSelect.getItems().add(p);
        }

        targetPlayerSelect.setCellFactory(_ -> new ListCell<>() {
            @Override
            protected void updateItem(Player player, boolean empty) {
                super.updateItem(player, empty);
                setText(empty || player == null ? null : player.getName());
            }
        });
        targetPlayerSelect.setButtonCell(targetPlayerSelect.getCellFactory().call(null));

        // Player offer
        VBox offerBox = new VBox(10);
        Label offerLabel = new Label("You offer:");
        HBox moneyOffer = new HBox(5, new Label("Money: $"), new TextField("0"));
        TextField offerMoneyField = (TextField) moneyOffer.getChildren().get(1);

        VBox offerPropsBox = new VBox(5);
        System.out.println("My name: " + currentPlayer.getName());
        for (OwnableSquare property : currentPlayer.getProperties()) {
            offerPropsBox.getChildren().add(new CheckBox(property.getName()));
            System.out.println("My property: " + property.getName());
        }
        ScrollPane offerScroll = new ScrollPane(offerPropsBox);
        offerScroll.setPrefHeight(200);
        offerScroll.setFitToWidth(true);

        offerBox.getChildren().addAll(offerLabel, moneyOffer, offerScroll);

        // Player request
        VBox requestBox = new VBox(10);
        Label requestLabel = new Label("You want:");
        HBox moneyRequest = new HBox(5, new Label("Money: $"), new TextField("0"));
        TextField requestMoneyField = (TextField) moneyRequest.getChildren().get(1);

        VBox requestPropsBox = new VBox(5);
        ScrollPane requestScroll = new ScrollPane(requestPropsBox);
        requestScroll.setPrefHeight(200);
        requestScroll.setFitToWidth(true);

        requestBox.getChildren().addAll(requestLabel, moneyRequest, requestScroll);

        targetPlayerSelect.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            requestPropsBox.getChildren().clear();
            if (newVal != null) {
                System.out.println("Selected player: " + newVal.getName());
                for (OwnableSquare property : newVal.getProperties()) {
                    requestPropsBox.getChildren().add(new CheckBox(property.getName()));
                    System.out.println("Their property: " + property.getName());
                }
            }
        });

        HBox tradePanels = new HBox(20);
        tradePanels.getChildren().addAll(offerBox, requestBox);
        VBox mainContent = new VBox(15, new HBox(10, new Label("Trade with:"), targetPlayerSelect), tradePanels);
        mainContent.setStyle("-fx-padding: 10;");

        dialog.getDialogPane().setContent(mainContent);

        // Disable the "Propose" button if no player is selected
        Node proposeButton = dialog.getDialogPane().lookupButton(offerButtonType);
        Node cancelButton = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
        proposeButton.setDisable(true);
        targetPlayerSelect.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            proposeButton.setDisable(newVal == null);
        });

        proposeButton.addEventFilter(ActionEvent.ACTION, event -> {
            event.consume();
            try {
                Player targetPlayer = targetPlayerSelect.getValue();
                int offerMoney = Integer.parseInt(offerMoneyField.getText());
                int requestMoney = Integer.parseInt(requestMoneyField.getText());

                if (offerMoney > currentPlayer.getMoney()) {
                    new Alert(Alert.AlertType.ERROR, "Not enough money!").showAndWait();
                    return;
                } else if (targetPlayer != null && requestMoney > targetPlayer.getMoney()) {
                    new Alert(Alert.AlertType.ERROR, targetPlayer.getName() + " does not have enough money!").showAndWait();
                    return;
                }

                List<String> offeredNames = new ArrayList<>();
                for (Node node : offerPropsBox.getChildren()) {
                    if (node instanceof CheckBox cb && cb.isSelected()) {
                        offeredNames.add(cb.getText());
                    }
                }

                List<String> requestedNames = new ArrayList<>();
                for (Node node : requestPropsBox.getChildren()) {
                    if (node instanceof CheckBox cb && cb.isSelected()) {
                        requestedNames.add(cb.getText());
                    }
                }

                System.out.println("Proposing trade to " + targetPlayer.getName());
                System.out.println("Offering: $" + offerMoney + " and " + offeredNames);
                System.out.println("Asking for: $" + requestMoney + " and " + requestedNames);

                proposeButton.setDisable(true);
                cancelButton.setDisable(true);
                targetPlayerSelect.setDisable(true);
                offerMoneyField.setDisable(true);
                requestMoneyField.setDisable(true);
                offerPropsBox.setDisable(true);
                requestPropsBox.setDisable(true);
                Button pb = (Button) proposeButton;
                pb.setText("Waiting...");

                game.getClient().sendTradeOffer(targetPlayer.getName(), offerMoney, offeredNames, requestMoney, requestedNames);

            } catch (NumberFormatException e) {
                new Alert(Alert.AlertType.ERROR, "Invalid number for money!").showAndWait();
            }
        });

        if (!targetPlayerSelect.getItems().isEmpty()) {
            targetPlayerSelect.getSelectionModel().selectFirst();
        }

        activeTradeOfferDialog = dialog;
        dialog.showAndWait();
        activeTradeOfferDialog = null;
    }

    public void showIncomingTradeDialog(TradeInfo tradeInfo) {
        long tradeUID = tradeInfo.tradeUID;
        String traderName = tradeInfo.offerer;
        int offerMoney = tradeInfo.offerMoney;
        int requestMoney = tradeInfo.requestMoney;
        String[] offeredProps = tradeInfo.offerProperties;
        String[] requestedProps = tradeInfo.requestProperties;

        Dialog<Boolean> dialog = new Dialog<>();
        dialog.setTitle("Trade Request");
        Text prefix = new Text("Trade offer from ");
        Text nameText = new Text(traderName);

        nameText.setFill(getPlayerColor(game.getGameState().getPlayerIndexByName(traderName)));
        nameText.setStyle("-fx-font-weight: bold;");

        TextFlow headerFlow = new TextFlow(prefix, nameText);

        dialog.getDialogPane().setHeader(headerFlow);
        dialog.getDialogPane().setPrefWidth(600);

        ButtonType acceptButtonType = new ButtonType("Accept", ButtonBar.ButtonData.OK_DONE);
        ButtonType rejectButtonType = new ButtonType("Reject", ButtonBar.ButtonData.CANCEL_CLOSE);
        dialog.getDialogPane().getButtonTypes().addAll(acceptButtonType, rejectButtonType);

        // They offer
        VBox offerBox = new VBox(10);
        Label offerLabel = new Label(traderName + " offers:");
        offerLabel.setStyle("-fx-font-weight: bold;");
        Label moneyOfferLabel = new Label("Money: $" + offerMoney);

        ListView<String> offerPropsList = new ListView<>();
        offerPropsList.getItems().addAll(offeredProps);
        offerPropsList.setPrefHeight(200);
        offerBox.getChildren().addAll(offerLabel, moneyOfferLabel, offerPropsList);

        // They request
        VBox requestBox = new VBox(10);
        Label requestLabel = new Label(traderName + " wants:");
        requestLabel.setStyle("-fx-font-weight: bold;");
        Label moneyRequestLabel = new Label("Money: $" + requestMoney);

        ListView<String> requestPropsList = new ListView<>();
        requestPropsList.getItems().addAll(requestedProps);
        requestPropsList.setPrefHeight(200);
        requestBox.getChildren().addAll(requestLabel, moneyRequestLabel, requestPropsList);

        HBox tradePanels = new HBox(20);
        tradePanels.getChildren().addAll(offerBox, requestBox);
        VBox mainContent = new VBox(15, tradePanels);
        mainContent.setStyle("-fx-padding: 10;");

        dialog.getDialogPane().setContent(mainContent);

        dialog.setResultConverter(button -> button == acceptButtonType);

        Optional<Boolean> result = dialog.showAndWait();
        boolean accepted = result.orElse(false);

        game.getClient().sendTradeResponse(tradeUID, accepted);
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
            if (property instanceof Street street) {
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
            String name = p.getName();
            if (name.equals(game.getPlayerName())) name += " (You)";
            Label infoLabel = new Label(name + ": $" + p.getMoney());
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

            boolean hasRolled = game.getGameState().getPlayerByName(game.getPlayerName()).hasRolled();

            rollButton.setDisable(hasRolled);
            buyHouseButton.setDisable(false);
            mortgageButton.setDisable(false);
            tradeButton.setDisable(false);
            finishTurnButton.setDisable(!hasRolled);
        } else {
            statusLabel.setText(state.getCurrentPlayer().getName() + "'s turn");
            rollButton.setDisable(true);
            buyHouseButton.setDisable(true);
            mortgageButton.setDisable(true);
            tradeButton.setDisable(true);
            finishTurnButton.setDisable(true);
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
