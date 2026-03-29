package game_logic.OwnableSquare;

import game_logic.Player;

public class RailRoad extends OwnableSquare{

    private int numberOfTrainStationsOwned;

    public RailRoad(int rent, Player owner) {
        super(rent, owner);
    }

    @Override
    public int calculateRent() {
        return (int) (25 * (Math.pow(2, numberOfTrainStationsOwned - 1)));
    }
}
