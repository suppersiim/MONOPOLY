package game_logic.NonOwnableSquare;

import game_logic.Player;
import server.GameManager;

import java.io.Serializable;

public class Card implements CardEffect, Serializable {
    private final String description;
    private final CardEffect effect;

    public Card(String description, CardEffect effect) {
        this.description = description;
        this.effect = effect;
    }

    public String getDescription() { return description; }

    @Override
    public void applyEffect(Player player) {
        GameManager.getInstance().broadcastEvent(player.getName() + " drew a card: " + description);
        effect.applyEffect(player);
    }
}
