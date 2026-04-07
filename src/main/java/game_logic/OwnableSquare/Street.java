package game_logic.OwnableSquare;

import game_logic.Player;
import javafx.scene.paint.Color;

public class Street extends OwnableSquare{

    private String color;
    private int sector;
    private int numberOfHouses;
    private boolean hasHotel;
    private int streetNumber;

    public Street(int rent, int price, Color color, int sector, String name) {
        super(rent, price, name);
        this.color = color.toString();
        this.sector = sector;
    }

    public int numberOfStreetsInColorSet(){
        // only brown and dark blue colored streets have 2 streets in color set
        if (color.equals(Color.BROWN.toString()) || color.equals(Color.DARKBLUE.toString())) return 2;
        else return 3;
    }

    public boolean isColorSetComplete(){
        // TODO: Determine if one player has all the streets from one color aka the color set is complete!
        // NB! DARK BLUE and BROWN have 2 streets in the colorset
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

    public int getNumberOfHouses(){
        return numberOfHouses;
    }

    public boolean hasHotel() {
        return hasHotel;
    }

    @Override
    public void landOn(Player player) {
        // if this property is owned by another player, the current player pays rent to the owner
        // if this property is not owned by any player, the current player can choose to buy it or not
        // if the current player decides not to buy it, the property goes to auction and other players can bid for it

        if (getOwner() == null){
            // offer to buy the street
            // TODO: popup window, where the player gets to choose to buy the street or not

            // if the player doesn't buy - the street goes to auction and other players can bid for it
            //TODO: auction

        } else {
            // if the street is owned by another player, the current player pays rent to the owner
            int rent = calculateRent();
            player.payRentToPlayer(rent, getOwner());
        }

    }

    public String getColor() {
        return color;
    }
}
