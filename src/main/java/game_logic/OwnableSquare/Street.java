package game_logic.OwnableSquare;

import game_logic.Player;

public class Street extends OwnableSquare{

    private String color;
    private int sector;
    private int numberOfHouses;
    private int streetNumber;

    public Street(int rent, Player owner, String color, int sector, int numberOfHouses, int streetNumber) {
        super(rent, owner);
        this.color = color;
        this.sector = sector;
        this.numberOfHouses = numberOfHouses;
        this.streetNumber = streetNumber;
    }

    public int numberOfStreetsInColorSet(){
        // only brown and dark blue colored streets have 2 streets in color set
        if (color.equals("brown") || color.equals("dark blue")) return 2;
        else return 3;
    }

    public boolean isColorSetComplete(){
        // Determine if one player has all the streets from one color aka the color set is complete!
        return true;
    }

    @Override
    public int calculateRent() {
        if (numberOfHouses > 0){
            return numberOfHouses * getRent(); // This is different in real monopoly!!!
        } else if (isColorSetComplete()){
            return 2 * getRent();
        }
        return getRent();
    }

    public int getHousePrice(){
        // 1. sector: 50$, 2.sector: 100$, 3.sector: 150$, 4.sector: 200$
        return sector * 50;
    }


    @Override
    public void landOn(Player player) {
        // if this property is owned by another player, the current player pays rent to the owner
        // if this property is not owned by any player, the current player can choose to buy it or not
        // if the current player decides not to buy it, the property goes to auction and other players can bid for it
    }
}
