package game_logic.NonOwnableSquare;

import game_logic.Player;

@FunctionalInterface
public interface CardEffect {
    void applyEffect(Player player);
}