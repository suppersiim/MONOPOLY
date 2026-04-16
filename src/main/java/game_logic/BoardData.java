package game_logic;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BoardData {
    public String name;
    public String type;
    public int cost;
    public int[] rent;
    public String color;
    public int house;
}
