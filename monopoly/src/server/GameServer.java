package server;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class GameServer {

    public final int PORT;
    private final List<ClientHandler> clents;

    public GameServer(int PORT) {
        this.PORT = PORT;
        this.clents = new ArrayList<>();
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
            System.out.println("Error starting server: " + e.getMessage());
        }
    }
}
