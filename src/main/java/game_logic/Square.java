package game_logic;

import server.Monopoly;

import server.GameManager;
import java.io.Serializable;

public class Square implements Serializable {

    private String name;
    //public GameState gameState;

    public Square(String name) {
        this.name = name;
    }

    // Every turn players roll the dice and based on the square they land on something happens.
    // Every square type is different so an abstract method is used to define the behavior of each square type when a player lands on it.
    // With every square type having a different implementation of the landOn method.
    public void landOn(Player player) {
        String eventMessage = player.getName() + " landed on " + name;
        GameManager.getInstance().broadcastEvent(eventMessage);
    }

    public String getName() {
        return name;
    }
}
