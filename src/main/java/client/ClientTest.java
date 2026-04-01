package client;

public class ClientTest {
    static void main() throws Exception {
        Game game = Game.createInstance("localhost", 8080);
        game.connect();
    }
}
