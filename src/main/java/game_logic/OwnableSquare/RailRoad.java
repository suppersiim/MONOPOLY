package game_logic.OwnableSquare;

import game_logic.Player;

public class RailRoad extends OwnableSquare{

    private int numberOfTrainStationsOwned;
    private String railRoadName;

    public RailRoad(int rent, Player owner, String railRoadName) {
        super(rent, owner);
        this.railRoadName = railRoadName;
    }

    @Override
    public int calculateRent() {
        // 1 railroad: 25$, 2 railroads: 50$, 3 railroads: 100$, 4 railroads: 200$
        return (int) (25 * (Math.pow(2, numberOfTrainStationsOwned - 1)));
    }

    @Override
    public void landOn(Player player, int squareIndex) {
        // if this property is owned by another player, the current player pays rent to the owner
        // if this property is not owned by any player, the current player can choose to buy it or not
        // if the current player decides not to buy it, the property goes to auction and other players can bid for it
    }
}
