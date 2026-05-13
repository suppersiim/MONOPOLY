package server;

import common.GamePacket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    public final int PORT;
    private final List<ClientHandler> clients;

    private final GameManager gameManager;

    public GameServer(int PORT) {
        this.PORT = PORT;
        this.clients = new ArrayList<>();
        this.gameManager = new GameManager(this);
    }

    // Start the game server
    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Game server started on port " + PORT);

            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler clientHandler = new ClientHandler(socket, this);

                clients.add(clientHandler);
                new Thread(clientHandler).start();
            }
        } catch (Exception e) {
            System.out.println("Error starting server (receiver): " + e.getMessage());
        }
    }

    public void removeClient(ClientHandler client) {
        int playerIndex = clients.indexOf(client);
        if (playerIndex > gameManager.getGame().currentPlayer)
            gameManager.getGame().currentPlayer--;

        gameManager.getGame().players.remove(playerIndex);
        clients.remove(client);

        if (clients.isEmpty()) {
            System.out.println("All clients disconnected. Resetting game state.");
            gameManager.resetGame();
        }
        else {
            gameManager.broadcastGameState();
        }
    }

    public ClientHandler getClientByPlayerIndex(int playerIndex) {
        if (playerIndex < 0 || playerIndex >= clients.size()) {
            return null;
        }
        return clients.get(playerIndex);
    }

    public void sendPacketToPlayerByName(String playerName, GamePacket packet) {
        try {
            int playerIndex = gameManager.getGame().getPlayerIndexByName(playerName);
            if (playerIndex == -1) {
                System.out.println("Player " + playerName + " not found in game; cannot send packet.");
                return;
            }
            getClientByPlayerIndex(playerIndex).send(packet);
        } catch (Exception e) {
            System.out.println("Error sending packet to player " + playerName + ": " + e.getMessage());
        }
    }

    public void sendToAllClients(GamePacket packet) throws IOException {
        for (ClientHandler client : clients) {
            client.send(packet);
        }
    }

    public GameManager getGameManager() {
        return gameManager;
    }
}
