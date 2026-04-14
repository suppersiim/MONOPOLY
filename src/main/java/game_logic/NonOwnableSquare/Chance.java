package game_logic.NonOwnableSquare;

import game_logic.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Chance extends NonOwnableSquare{

    public Chance() {
        super("Chance");
    }

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
    // TODO: replace movePlayerToSquare with move method and add int moves as param
    private final List<Card> allChanceCards = List.of(

            new Card("Advance to Boardwalk",
                    player -> player.movePlayerToSquare(39)),

            new Card("Advance to Go (Collect $200)",
                    player -> player.movePlayerToSquare(0)),

            new Card("Advance to Illinois Avenue. If you pass Go, collect $200",
                    player -> player.movePlayerToSquare(24)), // TODO: check if player passed GO

            new Card("Advance to St. Charles Place. If you pass Go, collect $200",
                    player -> player.movePlayerToSquare(11)), // TODO: check if player passed GO

            new Card("Advance to the nearest Railroad. If unowned, you may buy it from the Bank. If owned, pay owner twice the rental",
                    player -> player.movePlayerToNearestRailroad()), // TODO: pay double rent

            new Card("Advance to the nearest Railroad. If unowned, you may buy it from the Bank. If owned, pay owner twice the rental",
                    player -> player.movePlayerToNearestRailroad()), // TODO: pay double rent

            new Card("Advance to nearest Utility. If unowned, you may buy it. If owned, throw dice and pay owner ten times amount thrown",
                    player -> player.movePlayerToNearestUtility()), // TODO: pay 10x dice roll

            new Card("Bank pays you dividend of $50",
                    player -> player.addMoney(50)),

            new Card("Get Out of Jail Free",
                    player -> player.givePlayerGetOutOfJailCard()),

            new Card("Go Back 3 Spaces",
                    player -> player.setLocation((player.getLocation() - 3 + 40) % 40)),

            new Card("Go to Jail. Go directly to Jail, do not pass Go, do not collect $200",
                    player -> player.goToJail()), // TODO: do not pay player 200$ if passed GO

            new Card("Make general repairs on all your property. For each house pay $25. For each hotel pay $100",
                    player -> {
                        int houses = player.getTotalHouses();
                        int hotels = player.getTotalHotels();
                        player.payMoney(houses * 25 + hotels * 100);
                    }),

            new Card("Speeding fine $15",
                    player -> player.payMoney(15)),

            new Card("Take a trip to Reading Railroad. If you pass Go, collect $200",
                    player -> player.movePlayerToSquare(5)), // TODO: check if player passed GO

            new Card("You have been elected Chairman of the Board. Pay each player $50",
                    player -> { /* TODO: pay all other players $50 */ }),

            new Card("Your building loan matures. Collect $150",
                    player -> player.addMoney(150))
    );

    // Simulate card deck, where you draw a card from the top untill there are no more cards left
    private final List<Card> chanceDeck = new ArrayList<>(allChanceCards);

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
        // TODO: check if player moves over Go square and give them $200 if they do
        Card card = drawCard();
        System.out.println(player.getName() + " drew a Chance card: " + card.getDescription());
        card.applyEffect(player);
    }
}

