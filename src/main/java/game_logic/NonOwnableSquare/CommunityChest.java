package game_logic.NonOwnableSquare;

import game_logic.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CommunityChest extends NonOwnableSquare{

    public CommunityChest() {
        super("Community Chest");
    }

    /*
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
     */

    private final List<Card> allCommunityChestCards = List.of(

            new Card("Advance to Go (Collect $200)",
                    player -> player.movePlayerToSquare(0)),

            new Card("Bank error in your favor. Collect $200",
                    player -> player.addMoney(200)),

            new Card("Doctor's fee. Pay $50",
                    player -> player.payMoney(50)),

            new Card("From sale of stock you get $50",
                    player -> player.addMoney(50)),

            new Card("Get Out of Jail Free",
                    player -> player.givePlayerGetOutOfJailCard()),

            new Card("Go to Jail. Go directly to jail, do not pass Go, do not collect $200",
                    player -> player.goToJail()),

            new Card("Holiday fund matures. Receive $100",
                    player -> player.addMoney(100)),

            new Card("Income tax refund. Collect $20",
                    player -> player.addMoney(20)),

            new Card("It is your birthday. Collect $10 from every player",
                    player -> { /* TODO: collect $10 from every other player */ }),

            new Card("Life insurance matures. Collect $100",
                    player -> player.addMoney(100)),

            new Card("Pay hospital fees of $100",
                    player -> player.payMoney(100)),

            new Card("Pay school fees of $50",
                    player -> player.payMoney(50)),

            new Card("Receive $25 consultancy fee",
                    player -> player.addMoney(25)),

            new Card("You are assessed for street repair. $40 per house. $115 per hotel",
                    player -> {
                        int houses = player.getTotalHouses();
                        int hotels = player.getTotalHotels();
                        player.payMoney(houses * 40 + hotels * 115);
                    }),

            new Card("You have won second prize in a beauty contest. Collect $10",
                    player -> player.addMoney(10)),

            new Card("You inherit $100",
                    player -> player.addMoney(100))
    );

    private final List<Card> communityChestDeck = new ArrayList<>(allCommunityChestCards);

    public Card drawCard() {
        // Reshuffle deck if empty
        if (communityChestDeck.isEmpty()) {
            communityChestDeck.addAll(allCommunityChestCards);
            Collections.shuffle(communityChestDeck);
        }
        // Draw the top card
        return communityChestDeck.removeFirst();
    }

    @Override
    public void landOn(Player player) {
        Card card = drawCard();
        System.out.println(player.getName() + " drew a Community Chest card: " + card.getDescription());
        card.applyEffect(player);
    }

}

