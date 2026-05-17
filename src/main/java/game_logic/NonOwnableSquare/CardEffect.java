package game_logic.NonOwnableSquare;

import game_logic.Player;

import java.io.Serializable;

@FunctionalInterface
public interface CardEffect extends Serializable {
    void applyEffect(Player player);
}