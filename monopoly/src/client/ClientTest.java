package client;

import server.PacketType;

import java.io.IOException;

public class ClientTest {
    static void main() throws IOException {
        GameClient gameClient = new GameClient("localhost", 8080);
        gameClient.connect();

        gameClient.send(PacketType.TEST, "Hello from the client!".getBytes());
        System.out.println(gameClient.receive());

        gameClient.disconnect();
    }
}
