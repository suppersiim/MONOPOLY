package game_logic.OwnableSquare;

import game_logic.Player;

public abstract class OwnableSquare {

        private Player owner;
        private int rent;

        public OwnableSquare(int rent, Player owner) {
            this.rent = rent;
            this.owner = owner;
        }

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
