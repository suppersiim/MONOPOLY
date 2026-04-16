package game_logic.OwnableSquare;

import game_logic.GameState;
import game_logic.Player;
import game_logic.Square;
import server.Monopoly;

public abstract class OwnableSquare extends Square {

    private Player owner;
    private int rent;
    private int price;

    public OwnableSquare(String name, GameState gameState, Player owner, int rent, int price) {
        super(name, gameState);
        this.owner = owner;
        this.rent = rent;
        this.price = price;
    }

    // abstact method that every type of property can implement differently
    public abstract int calculateRent();

    @Override
    public void landOn(Player player){
        //Square square = getSquare(player.getLocation());
        //System.out.println(player.getName() + " landed on: " + this.getName() + " (position " + player.getLocation
        // () + ")");
        if (this.getOwner() == null) {
            //gamestate.pendingPurchase = ownable;
            //gamestate.waitingForBuyResponse = true;
            System.out.println(player.getName() + " can buy " + this.getName() + " for $" + this.getPrice() + ".");
        }
        else this.landOn(player);
    }

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public int getRent() {
        return rent;
    }

    public int getPrice() {
        return price;
    }
}
