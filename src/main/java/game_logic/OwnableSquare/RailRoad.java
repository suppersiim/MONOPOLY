package game_logic.OwnableSquare;

import game_logic.Player;

public class RailRoad extends OwnableSquare{

    //private int numberOfTrainStationsOwned;

    public RailRoad(int[] rent, int price, String name) {
        super(rent, price, name);
    }

    @Override
    public int calculateRent() {
        // 1 railroad: 25$, 2 railroads: 50$, 3 railroads: 100$, 4 railroads: 200$
        //TODO: check if owner() == null

        int numberOfTrainStationsOwned = getOwner().railRoadsOwned().size();
        return (int) (25 * (Math.pow(2, numberOfTrainStationsOwned - 1)));
    }
}
