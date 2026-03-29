package game_logic;

import java.util.ArrayList;
import java.util.List;

public class Player{
    private String name;
    private int money;
    private List<Property> properties;
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

    public void buy(Property property){
        //money-=property.cost; need to get info from property
        properties.add(property);
    }

    public void payRent(Property property){
        //money-=property.rent; need to get info from property
    }



    public void move(int moves){
        if (location+moves>=40){
            location = location+moves%40;
            money+=200;
        }
        else location+=moves;
    }

    public void goJail(){
        location = 10;
        inJail = true;
    }


}