package game_logic;

import game_logic.NonOwnableSquare.Card;
import game_logic.OwnableSquare.OwnableSquare;
import game_logic.OwnableSquare.RailRoad;
import game_logic.OwnableSquare.Street;
import game_logic.OwnableSquare.Utility;
import server.GameManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Player implements Serializable {
    private String name;
    private int money;
    private List<OwnableSquare> properties;
    private int location;
    private boolean inJail;
    private List<Card> playerCards;
    private static final long serialVersionUID = 1L;

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

    public void setLocation(int location) {
        this.location = location;
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

    /*
    public List<Player> getAllPlayers(){
        return getplayers()
    }
    //TODO: get all players
     */

    public List<OwnableSquare> getProperties() {
        return properties;
    }

    public List<Card> getPlayerCards() {
        return playerCards;
    }

    public List<OwnableSquare> getProperties(Player player) {
        return player.properties;
    }

    public void buy(OwnableSquare property) {
        money -= property.getPrice();
        property.setOwner(this);
        properties.add(property);
    }

    public void buyHouse(Street street){
        money -= street.getHousePrice();
        street.addHouse();
    }

    public void setInJail(boolean inJail) {
        this.inJail = inJail;
    }

    public void addMoney(int money){
        this.money += money;
    }

    public void payMoney(int money){
        String event = this.name + " paid $" + money;
        GameManager.getInstance().broadcastEvent(event);
        this.money -= money;
    }

    public void payRentToPlayer(int rent, Player owner){
        String event = this.name + " paid $" + rent + " in rent to " + owner.getName();
        GameManager.getInstance().broadcastEvent(event);
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
        String event = this.name + " was sent to Jail!";
        GameManager.getInstance().broadcastEvent(event);
        location = 10;
        inJail = true;
    }

    public void payFineToGetOutOfJail(){
        String event = this.name + " paid $50 to get out of Jail.";
        GameManager.getInstance().broadcastEvent(event);
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

    public void movePlayerToSquare(int squareIndex) {
        location = squareIndex;
    }

    public int calculateDistance(int squareIndex) {
        if (squareIndex > location) return squareIndex-location;
        else return 40-location+squareIndex;
    }

    public void movePlayerToNearestRailroad() {
        int[] railroadPositions = {5, 15, 25, 35};
        int nearestRailroad = 0;
        for (int pos : railroadPositions) {
            if (pos > location) {
                nearestRailroad = pos;
                break;
            }
        }
        // If the player is past all railroads, loop back to the first one
        if (nearestRailroad == 0) nearestRailroad = 5;
        setLocation(nearestRailroad);

        //TODO: check if square is owned

    }

    public void movePlayerToNearestUtility() {
        int[] utilityPositions = {12, 28};
        int nearestUtility = 0;
        for (int pos : utilityPositions) {
            if (pos > location) {
                nearestUtility = pos;
                break;
            }
        }
        // If the player is past both utilities, loop back to the first one
        if (nearestUtility == 0) nearestUtility = 12;
        setLocation(nearestUtility);
    }

    public void givePlayerGetOutOfJailCard() {
        String event = this.name + " received a Get Out of Jail Free card!";
        GameManager.getInstance().broadcastEvent(event);
        // TODO: add a Get Out of Jail Free card to the player's inventory
    }

    public int getTotalHouses() {
        int totalHouses = 0;
        for (OwnableSquare property : properties) {
            if (property instanceof Street) {
                totalHouses += ((Street) property).getNumberOfHouses();
            }
        }
        return totalHouses;
    }

    public int getTotalHotels(){
        int totalHotels = 0;
        for (OwnableSquare property : properties) {
            if (property instanceof Street) {
                if (((Street) property).hasHotel()) {
                    totalHotels++;
                }
            }
        }
        return totalHotels;
    }

    public void mortgage(OwnableSquare property){
        money += property.getMortgageValue();
        property.setMortgaged(true);
    }

    public void unmortgage(OwnableSquare property){
        money -= property.getUnmortgageCost(); // 10% interest to unmortgage!
        property.setMortgaged(false);
    }

    public int getNumberOfPlayers(){
        return GameManager.getInstance().getGame().getPlayers().size();
    }
}