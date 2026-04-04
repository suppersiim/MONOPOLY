package game_logic;

import game_logic.OwnableSquare.OwnableSquare;
import game_logic.OwnableSquare.Street;

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

    public void buy(Street street){
        //money-=property.cost; need to get info from property
        properties.add(street);
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
            location = location+moves%40;
            money+=200;
        }
        else location+=moves;
    }

    public void goToJail(){
        location = 10;
        inJail = true;
    }


}