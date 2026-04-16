package game_logic.NonOwnableSquare;

import game_logic.GameState;
import game_logic.Square;

import java.io.Serializable;

public abstract class NonOwnableSquare extends Square implements Serializable {

    public NonOwnableSquare(String name) {
        super(name);
    }
}
