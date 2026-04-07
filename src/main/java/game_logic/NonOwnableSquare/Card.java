package game_logic.NonOwnableSquare;

import game_logic.Player;

public class Card implements CardEffect {
    private final String description;
    private final CardEffect effect;

    public Card(String description, CardEffect effect) {
        this.description = description;
        this.effect = effect;
    }

    public String getDescription() { return description; }

    @Override
    public void applyEffect(Player player) {
        effect.applyEffect(player);
    }
}
