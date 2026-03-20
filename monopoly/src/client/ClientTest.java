package client;

import server.PacketType;

import java.io.IOException;

public class ClientTest {
    static void main() throws Exception {
        Game game = new Game("localhost", 8080);
        game.start();
    }
}
