package game_logic.NonOwnableSquare;

import game_logic.OwnableSquare.RailRoad;
import game_logic.OwnableSquare.Utility;
import game_logic.Player;
import game_logic.Square;
import server.GameManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Chance extends NonOwnableSquare{
    /*
    //LIST OF CHANCE CARDS:
    final String[] chanceCards = {
            "Advance to Boardwalk",
            "Advance to Go (Collect $200)",
            "Advance to Illinois Avenue. If you pass Go, collect $200",
            "Advance to St. Charles Place. If you pass Go, collect $200",
            "Advance to the nearest Railroad. If unowned, you may buy it from the Bank. If owned, pay owner twice the rental to which they are otherwise entitled",
            "Advance to the nearest Railroad. If unowned, you may buy it from the Bank. If owned, pay owner twice the rental to which they are otherwise entitled",
            "Advance token to nearest Utility. If unowned, you may buy it from the Bank. If owned, throw dice and pay owner a total ten times amount thrown",
            "Bank pays you dividend of $50",
            "Get Out of Jail Free",
            "Go Back 3 Spaces",
            "Go to Jail. Go directly to Jail, do not pass Go, do not collect $200",
            "Make general repairs on all your property. For each house pay $25. For each hotel pay $100",
            "Speeding fine $15",
            "Take a trip to Reading Railroad. If you pass Go, collect $200",
            "You have been elected Chairman of the Board. Pay each player $50",
            "Your building loan matures. Collect $150"
    };
    */

    private final List<Card> allChanceCards = List.of(

            new Card("Advance to Boardwalk",
                    player -> player.movePlayerToSquare(39)),

            new Card("Advance to Go (Collect $200)",
                    player -> player.move(player.calculateDistance(0))),

            new Card("Advance to Illinois Avenue. If you pass Go, collect $200",
                    player -> player.move(player.calculateDistance(24))),

            new Card("Advance to St. Charles Place. If you pass Go, collect $200",
                    player -> player.move(player.calculateDistance(11))),

            new Card("Advance to the nearest Railroad. If unowned, you may buy it from the Bank. If owned, pay owner twice the rental",
                    player -> {
                        player.movePlayerToNearestRailroad();
                        Square square = GameManager.getInstance().getGame().getSquare(player.getLocation());
                        if (square instanceof RailRoad railroad) {
                            if (railroad.getOwner() == null) {
                                // offer to buy the property
                                railroad.landOn(player);
                            } else if (railroad.getOwner() != player) {
                                // pay double rent if owned buy
                                int doubleRent = railroad.calculateRent() * 2;
                                player.payRentToPlayer(doubleRent, railroad.getOwner());
                            }
                        }
                    }),

            new Card("Advance to the nearest Railroad. If unowned, you may buy it from the Bank. If owned, pay owner twice the rental",
                    player -> {
                        player.movePlayerToNearestRailroad();
                        Square square = GameManager.getInstance().getGame().getSquare(player.getLocation());
                        if (square instanceof RailRoad railroad) {
                            if (railroad.getOwner() == null) {
                                // offer to buy the property
                                railroad.landOn(player);
                            } else if (railroad.getOwner() != player) {
                                // pay double rent if owned buy
                                int doubleRent = railroad.calculateRent() * 2;
                                player.payRentToPlayer(doubleRent, railroad.getOwner());
                            }
                        }
                    }),

            new Card("Advance to nearest Utility. If unowned, you may buy it. If owned, throw dice and pay owner ten times amount thrown",
                    player -> {
                        player.movePlayerToNearestUtility();
                        Square square = GameManager.getInstance().getGame().getSquare(player.getLocation());
                        if (square instanceof Utility utility) {
                            if (utility.getOwner() == null) {
                                utility.landOn(player);
                            } else if (utility.getOwner() != player) {
                                // roll dice and pay 10x
                                int roll = (int)(Math.random() * 6 + 1) + (int)(Math.random() * 6 + 1);
                                int rent = roll * 10;
                                GameManager.getInstance().broadcastEvent(player.getName() + " rolled " + roll + " for utility rent");
                                player.payRentToPlayer(rent, utility.getOwner());
                            }
                        }
                    }),

            new Card("Bank pays you dividend of $50",
                    player -> player.addMoney(50)),

            new Card("Get Out of Jail Free",
                    player -> player.givePlayerGetOutOfJailCard()),

            new Card("Go Back 3 Spaces",
                    player -> player.setLocation((player.getLocation() - 3 + 40) % 40)),

            new Card("Go to Jail. Go directly to Jail, do not pass Go, do not collect $200",
                    player -> player.goToJail()),

            new Card("Make general repairs on all your property. For each house pay $25. For each hotel pay $100",
                    player -> {
                        int houses = player.getTotalHouses();
                        int hotels = player.getTotalHotels();
                        player.payMoney(houses * 25 + hotels * 100);
            }),

            new Card("Speeding fine $15",
                    player -> player.payMoney(15)),

            new Card("Take a trip to Reading Railroad. If you pass Go, collect $200",
                    player -> player.move(player.calculateDistance(5))),

            new Card("You have been elected Chairman of the Board. Pay each player $50",
                    player -> {
                List<Player> players = player.getAllPlayers();
                int total = 0;
                for (Player p : players) {
                    if (player != p){
                        player.payMoney(50);
                        p.addMoney(50);
                        total += 50;
                    }
                }
                GameManager.getInstance().broadcastEvent(player.getName() + " paid each player $50 (total: $" + total + ")");
            }),

            new Card("Your building loan matures. Collect $150",
                    player -> player.addMoney(150))
    );

    private final List<Card> chanceDeck;

    public Chance() {
        super("Chance");
        chanceDeck = new ArrayList<>(allChanceCards);
        Collections.shuffle(chanceDeck);
    }

    public Card drawCard() {
        // Reshuffle deck if empty
        if (chanceDeck.isEmpty()) {
            chanceDeck.addAll(allChanceCards);
            Collections.shuffle(chanceDeck);
        }
        // Draw the top card
        return chanceDeck.removeFirst();
    }

    @Override
    public void landOn(Player player) {
        super.landOn(player);
        Card card = drawCard();
        //System.out.println(player.getName() + " drew a Chance card: " + card.getDescription());
        GameManager.getInstance().broadcastEvent(player.getName() + " drew a Chance card: " + card.getDescription());
        card.applyEffect(player);
    }
}

