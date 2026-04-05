package game_logic.OwnableSquare;

import game_logic.Player;

public class Utility extends OwnableSquare{

    private int numberOfUtilitiesOwned;

    public Utility(int rent, String name) {
        super(rent, name);
    }

    @Override
    public int calculateRent() {
        if (numberOfUtilitiesOwned == 1) return 4; // here should be 4 * getDiceRollResult()!!
        else if (numberOfUtilitiesOwned == 2) return 10; // here should be 10 * getDiceRollResult()!!
        else return 0;
    }

    @Override
    public void landOn(Player player) {
         // when a player lands on a utility, they must pay rent to the owner based on the dice roll and the number of utilities owned by the owner
         // if the utility is not owned, the player can choose to buy it or not
         // if the player decides not to buy it, the utility goes to auction and other players can bid for it
        if (getOwner() == null){
            // offer to buy the utility
            // TODO: popup window, where the player gets to choose to buy the utility or not

            // if the player doesn't buy - the utility goes to auction and other players can bid for it
            // TODO: auction


        } else {
            // if the utility is owned by another player, the current player pays rent to the owner
            int rent = calculateRent();
            player.payRentToPlayer(rent, getOwner());
        }
    }
}
