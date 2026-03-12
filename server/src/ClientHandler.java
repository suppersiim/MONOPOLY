import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new PrintWriter(socket.getOutputStream(), true);

            String inputLine;
            while ((inputLine = reader.readLine()) != null) {
                // handle game logic
                System.out.println("Received from client: " + inputLine);
                writer.println("Echo: " + inputLine);   // response
            }
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        }
    }
}
