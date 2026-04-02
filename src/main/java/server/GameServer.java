package server;

import common.GamePacket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    public final int PORT;
    private final List<ClientHandler> clents;

    private final GameManager gameManager;

    public GameServer(int PORT) {
        this.PORT = PORT;
        this.clents = new ArrayList<>();
        this.gameManager = new GameManager(this);
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

    public GameManager getGameManager() {
        return gameManager;
    }
}
