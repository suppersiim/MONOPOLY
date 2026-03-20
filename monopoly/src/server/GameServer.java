package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    public final int PORT;
    private final List<ClientHandler> clents;

    private GameState gameState;

    public GameServer(int PORT) {
        this.PORT = PORT;
        this.clents = new ArrayList<>();
        this.gameState = new GameState(this);
    }

    // Start the game server
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Game server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, this);

                clents.add(clientHandler);
                new Thread(clientHandler).start();
                clientHandler.send(new GamePacket(PacketType.SERVER_GAME_STATE_UPDATE, gameState.serialize()));
            }
        } catch (Exception e) {
            System.out.println("Error starting server (receiver): " + e.getMessage());
        }
    }

    public void sendToAllClients(GamePacket packet) throws IOException {
        for (ClientHandler client : clents) {
            client.send(packet);
        }
    }

    public GameState getGameState() {
        return gameState;
    }
}
