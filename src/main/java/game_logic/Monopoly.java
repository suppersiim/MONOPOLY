package game_logic;

import java.util.List;

public class Monopoly{
    private List<Player> players;
    private int currentPlayer;
    private List<Square> squares;

    public Monopoly(List<Player> players) {
        this.players = players;
        this.currentPlayer = 0;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public int getCurrentPlayer() {
        return currentPlayer;
    }

    public int[] diceRoll(){
        int[] dice = new int[2];
        dice[0] = (int)(Math.random()*6+1);
        dice[1] = (int)(Math.random()*6+1);
        return dice;
    }

    public Square landedOn(int location){
        return squares.get(location);
    }

    public void onTurn(){
        if (!players.get(currentPlayer).isInJail()) {
            int doubles = 0;
            int[] dice = diceRoll();
            if (dice[0] == dice[1]) {
                doubles += 1;
                if (doubles == 3) {
                    players.get(currentPlayer).goJail();
                }
            }
            players.get(currentPlayer).move(dice[0] + dice[1]);

            Square squareCurrent = landedOn(players.get(currentPlayer).getLocation());
        }


    }





}