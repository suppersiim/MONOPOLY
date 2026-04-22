package game_logic.OwnableSquare;

import game_logic.GameState;
import game_logic.Player;
import game_logic.Square;
import server.Monopoly;
import server.GameManager;

public abstract class OwnableSquare extends Square {

    private Player owner;
    private int[] rent;
    private int price;
    private static final long serialVersionUID = 1L;
    private boolean isMortgaged;

    public OwnableSquare(int[] rent, int price, String name) {
        super(name);
        this.price = price;
        this.rent = rent;
    }

    // abstact method that every type of property can implement differently
    public abstract int calculateRent();

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public int[] getRent() {
        return rent;
    }

    public int getPrice() {
        return price;
    }

    public void mortgage(){
        isMortgaged = true;
    }

    public void unmortgage(){
        isMortgaged = false;
    }

    public boolean isMortgaged() {
        return isMortgaged;
    }

    public void setMortgaged(boolean mortgaged) {
        isMortgaged = mortgaged;
    }

    public int getMortgageValue() {
        return price / 2;
    }

    public int getUnmortgageCost() {
        return (int) (price / 2 * 1.1);
    }

    @Override
    public void landOn(Player player) {
        super.landOn(player);
        // if this property is owned by another player, the current player pays rent to the owner
        // if this property is not owned by any player, the current player can choose to buy

        if (getOwner() == null){
            // offer to buy the property
            GameManager.getInstance().getGame().setPendingPurchase(this);
            GameManager.getInstance().getGame().setWaitingForBuyResponse(true);
        } else if (getOwner() != player) {
            if (!isMortgaged) {
                // if the property is owned by another player, the current player pays rent to the owner
                int rent = calculateRent();
                player.payRentToPlayer(rent, getOwner());
            }
        }
    }
}
