package game_logic;

import game_logic.OwnableSquare.Street;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    private String name;
    private int money;
    private List<Street> properties;
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

    public void setLocation(int location) {
        this.location = location;
    }

    public void buy(Street street){
        //money-=property.cost; need to get info from property
        properties.add(street);
    }

    public void payRent(Street property){
        //money-=property.rent; need to get info from property
    }

    public void pay(int amount){
        money+=amount;
    }



    public void move(int moves){
        if (location+moves>=40){
            location = (location+moves)%40;
            money+=200;
        }
        else location+=moves;
    }

    public void goJail(){
        location = 10;
        inJail = true;
    }


}