package game_logic;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import game_logic.NonOwnableSquare.*;
import game_logic.OwnableSquare.RailRoad;
import game_logic.OwnableSquare.Street;
import game_logic.OwnableSquare.Utility;
import javafx.scene.control.Tab;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class BoardLoader {

    public List<Square> loadBoard() {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            InputStream inputStream = BoardLoader.class.getResourceAsStream("/game_information/board.json");
            if (inputStream == null) {
                throw new RuntimeException("board.json not found");
            }

            BoardData[] boardData = objectMapper.readValue(inputStream,BoardData[].class);

            List<Square> boardSquares = new ArrayList<>();
            for (BoardData data : boardData) {
                if (data.type.equals("community-chest")) boardSquares.add(new CommunityChest());
                else if (data.type.equals("go")) boardSquares.add(new GoSquare());
                else if (data.type.equals("property")) boardSquares.add(new Street(data.rent, data.cost,
                        Color.web(data.color), data.name, data.house));
                else if (data.type.equals("railroad")) boardSquares.add(new RailRoad(new int[]{0}, data.cost,
                        data.name));
                else if (data.type.equals("tax")) boardSquares.add(new Tax(data.cost));
                else if (data.type.equals("chance")) boardSquares.add(new Chance());
                else if (data.type.equals("jail")) boardSquares.add(new Jail());
                else if (data.type.equals("utility")) boardSquares.add(new Utility(new int[]{0},data.cost, data.name));
                else if (data.type.equals("free-parking")) boardSquares.add(new FreeParking());
                else if (data.type.equals("go-to-jail")) boardSquares.add(new GoToJail());
            }

            return boardSquares;

        } catch (IOException e) {
            System.out.println("Loading squares failed " + e.getMessage());
            //e.getStackTrace();
            throw new RuntimeException("Loading squares failed", e);
        }
    }

}
