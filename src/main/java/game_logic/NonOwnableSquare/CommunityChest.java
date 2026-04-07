package game_logic.NonOwnableSquare;

import game_logic.Player;

public class CommunityChest extends NonOwnableSquare implements CardEffect{

    public CommunityChest() {
        super("Community Chest");
    }

    private final String[] communityChestCards = {
            "Advance to Go (Collect $200)",
            "Bank error in your favor. Collect $200",
            "Doctor’s fee. Pay $50",
            "From sale of stock you get $50",
            "Get Out of Jail Free",
            "Go to Jail. Go directly to jail, do not pass Go, do not collect $200",
            "Holiday fund matures. Receive $100",
            "Income tax refund. Collect $20",
            "It is your birthday. Collect $10 from every player",
            "Life insurance matures. Collect $100",
            "Pay hospital fees of $100",
            "Pay school fees of $50",
            "Receive $25 consultancy fee",
            "You are assessed for street repair. $40 per house. $115 per hotel",
            "You have won second prize in a beauty contest. Collect $10",
            "You inherit $100"
    };

    public String getRandomCard() {
        int randomIndex = (int) (Math.random() * communityChestCards.length);
        return communityChestCards[randomIndex];
    }

    @Override
    public void landOn(Player player) {
        String card = getRandomCard();
        // Here you would implement the logic to apply the effect of the card to the player
        // For example, if the card is "Advance to Go (Collect $200)", you would move the player to the Go square and give them $200
        // This is just a placeholder and should be expanded based on the actual effects of each card
        System.out.println(player.getName() + " drew a Community Chest card: " + card);
        // TODO: every card you pull has an effect to the player - implement all these functions
    }

    @Override
    public void applyEffect(Player player) {

    }
}

