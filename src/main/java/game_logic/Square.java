package game_logic;

public abstract class Square {

    private int squareIndex;

    // Every turn players roll the dice and based on the square they land on something happens.
    // Every square type is different so an abstract method is used to define the behavior of each square type when a player lands on it.
    // With every square type having a different implementation of the landOn method.
    public abstract void landOn(Player player);


}
