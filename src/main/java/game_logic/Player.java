package game_logic;

import game_logic.OwnableSquare.OwnableSquare;
import game_logic.OwnableSquare.RailRoad;
import game_logic.OwnableSquare.Street;
import game_logic.OwnableSquare.Utility;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    private String name;
    private int money;
    private List<OwnableSquare> properties;
    private int location;
    private boolean inJail;

    public Player(String name) {
        this.name = name;
        this.money = 1500;
        this.properties = new ArrayList<>();
        this.location = 0;
        this.inJail = false;
    }

    public int getLocation() {
        return location;
    }

    public boolean isInJail() {
        return inJail;
    }

    public String getName() {
        return name;
    }

    public int getMoney() {
        return money;
    }

    public List<OwnableSquare> getProperties(Player player) {
        return player.properties;
    }

    public void buy(OwnableSquare property) {
        money -= property.getPrice();
        property.setOwner(this);
        properties.add(property);
    }

    public void setInJail(boolean inJail) {
        this.inJail = inJail;
    }

    public void addMoney(int money){
        this.money += money;
    }

    public void payTax(int tax){
        this.money -= tax;
    }

    public void payRentToPlayer(int rent, Player owner){
        this.money -= rent;
        owner.addMoney(rent);
    }

    public void move(int moves){
        if (location+moves>=40){
            location = (location+moves)%40;
            money+=200;
        }
        else location+=moves;
    }

    public void goToJail(){
        location = 10;
        inJail = true;
    }

    public void payFineToGetOutOfJail(){
        money -= 50;
        inJail = false;
    }

    public List<RailRoad> railRoadsOwned(){
        List<RailRoad> railRoadsOwned = new ArrayList<>();
        for (OwnableSquare property : properties){
            if (property instanceof RailRoad){
                railRoadsOwned.add((RailRoad) property);
            }
        }
        return railRoadsOwned;
    }

    public List<Street> streetsOwned(){
        List<Street> streetsOwned = new ArrayList<>();
        for (OwnableSquare property : properties){
            if (property instanceof Street){
                streetsOwned.add((Street) property);
            }
        }
        return streetsOwned;
    }

    public List<Utility> utilitiesOwned(){
        List<Utility> utilitiesOwned = new ArrayList<>();
        for (OwnableSquare property : properties){
            if (property instanceof Utility){
                utilitiesOwned.add((Utility) property);
            }
        }
        return utilitiesOwned;
    }

    public boolean isBankrupt(){
        // TODO: check if player has any money (has houses? can mortgage something? has cash?)
        return false;
    }

}