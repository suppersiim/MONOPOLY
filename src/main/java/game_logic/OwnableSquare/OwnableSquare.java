package game_logic.OwnableSquare;

import game_logic.Player;

public abstract class OwnableSquare {

        private Player owner;
        private int rent;

        public OwnableSquare(int rent) {
            this.rent = rent;
            this.owner = null;
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

}
