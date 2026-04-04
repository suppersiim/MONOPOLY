package game_logic.NonOwnableSquare;

import game_logic.Player;

public class Chance extends NonOwnableSquare {

    private final String[] chanceCards = {
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

    public String getRandomCard() {
        int randomIndex = (int) (Math.random() * chanceCards.length);
        return chanceCards[randomIndex];
    }

    @Override
    public void landOn(Player player) {
        String card = getRandomCard();
        // Here you would implement the logic to apply the effect of the card to the player
        // For example, if the card is "Advance to Go (Collect $200)", you would move the player to the Go square and give them $200
        // This is just a placeholder and should be expanded based on the actual effects of each card
        System.out.println(player.getName() + " drew a Chance card: " + card);
        // TODO: every card you pull has an effect to the player - implement all these functions
    }
}
