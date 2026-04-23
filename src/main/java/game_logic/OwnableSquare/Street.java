package game_logic.OwnableSquare;

import game_logic.Player;
import javafx.scene.paint.Color;

public class Street extends OwnableSquare{

    private String color;
    private int sector;
    private int numberOfHouses;
    private boolean hasHotel;
    private int streetNumber;
    private int houseCost;
    private static final long serialVersionUID = 1L;

    public Street(int[] rent, int price, Color color, String name, int houseCost) {
        super(rent, price, name);
        this.color = color.toString();
        this.houseCost = houseCost;
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
            return getRent()[numberOfHouses]; // This is different in real monopoly!!!
        } else if (isColorSetComplete()){
            return getRent()[0] * 2;
        }
        return getRent()[0];
    }

    public int getHousePrice(){
        // 1. sector: 50$, 2.sector: 100$, 3.sector: 150$, 4.sector: 200$
        return houseCost;
    }

    public int getNumberOfHouses(){
        return numberOfHouses;
    }

    public void addHouse(){
        numberOfHouses++;
        System.out.println("addHouse called, now: " + numberOfHouses);
    }

    public void removeHouse() {
        if (numberOfHouses > 0) numberOfHouses--;
    }

    public boolean hasHotel() {
        return hasHotel;
    }

    @Override
    public void landOn(Player player) {
        super.landOn(player);
    }

    public String getColor() {
        return color;
    }

    @Override
    public String toString() {
        return this.getName();
    }
}
