package game_logic.OwnableSquare;

import game_logic.Player;
import game_logic.Square;

public abstract class OwnableSquare extends Square {

        private Player owner;
        private int rent;

        public OwnableSquare(int rent, Player owner) {
            this.rent = rent;
            this.owner = owner;
        }

        // abstact method that every type of property can implement differently
        public abstract int calculateRent();

        public Player getOwner() {
            return owner;
        }

        public void setOwner(Player owner) {
            this.owner = owner;
        }

        public int getRent() {
            return rent;
        }

}
