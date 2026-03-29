package game_logic.OwnableSquare;

import game_logic.Player;

public class Street extends OwnableSquare{

    private String color;
    private int sector;

    public Street(int rent, Player owner) {
        super(rent, owner);
    }

    public boolean isColorSetComplete(){
        return true;
    }

    @Override
    public int calculateRent() {
        return 0;
    }

    public int getHousePrice(){
        return sector * 50;
    }



}
