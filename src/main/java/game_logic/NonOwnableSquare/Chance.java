package game_logic.NonOwnableSquare;

import game_logic.Player;

import java.util.List;

public class Chance extends NonOwnableSquare{

    public Chance() {
        super("Chance");
    }

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

    private final List<CardEffect> chanceCardsEffects = List.of(

            // Advance to Boardwalk
            (player) -> player.movePlayerToSquare(39),

            // Advance to Go (Collect $200)
            (player) -> player.movePlayerToSquare(0),

            // Advance to Illinois Avenue. If you pass Go, collect $200
            (player) -> player.movePlayerToSquare(24),

            // Advance to St. Charles Place. If you pass Go, collect $200
            (player) -> player.movePlayerToSquare(11),

            // Advance to the nearest Railroad. If unowned, you may buy it from the Bank. If owned, pay owner twice the rental to which they are otherwise entitled
            // TODO: pay double the rent!
            (player) -> player.movePlayerToNearestRailroad(),

            // Advance to the nearest Railroad. If unowned, you may buy it from the Bank. If owned, pay owner twice the rental to which they are otherwise entitled
            // TODO: pay double the rent!
            (player) -> player.movePlayerToNearestRailroad(),

            // Advance token to nearest Utility. If unowned, you may buy it from the Bank. If owned, throw dice and pay owner a total ten times amount thrown
            // TODO: pay 10 times the rent!
            (player) -> player.movePlayerToNearestUtility(),

            // Bank pays you dividend of $50
            (player) -> player.addMoney(50),

            // Get Out of Jail Free
            (player) -> player.givePlayerGetOutOfJailCard(),

            // Go Back 3 Spaces
            (player) -> player.setLocation((player.getLocation() - 3 + 40) % 40),

            // Go to Jail. Go directly to Jail, do not pass Go, do not collect $200
            (player) -> player.goToJail(),

            // Make general repairs on all your property. For each house pay $25. For each hotel pay $100
            (player) -> {
                int houses = player.getTotalHouses();
                int hotels = player.getTotalHotels();
                player.payMoney(houses * 25 + hotels * 100);
            },

            // Speeding fine $15
            (player) -> player.payMoney(15),

            // Take a trip to Reading Railroad. If you pass Go, collect $200
            (player) -> player.movePlayerToSquare(5),

            // You have been elected Chairman of the Board. Pay each player $50
            (player) -> {
                //TODO: pay all other players 50$
            },

            // Your building loan matures. Collect $150
            (player) -> player.addMoney(150)
    );

    public int getRandomCard() {
        return (int) (Math.random() * chanceCardsEffects.size());
    }

    @Override
    public void landOn(Player player) {
        applyEffect(player);
    }

    public void applyEffect(Player player) {
        // TODO: check if player moves over Go square and give them $200 if they do
        int cardNumber = getRandomCard();
        System.out.println(player.getName() + " drew a Chance card: " + chanceCards[cardNumber]);
        chanceCardsEffects.get(cardNumber).applyEffect(player);
    }
}

