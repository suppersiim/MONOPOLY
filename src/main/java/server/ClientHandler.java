package server;

import common.GamePacket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.Socket;

/**
 * Handles raw client messages
 */
public class ClientHandler implements Runnable {

    private final GameServer server;
    private final Socket socket;

    private DataInputStream in;
    private DataOutputStream out;

    private final PacketHandler packetHandler;

    public ClientHandler(Socket socket, GameServer server) {
        this.socket = socket;
        this.server = server;
        this.packetHandler = new PacketHandler(this, server);

        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error initializing client handler: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                receive();
            }
        } catch (EOFException e) {
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.out.println("Error handling client: " + e.getMessage());
        }
    }

    private void receive() throws IOException {
        // TODO: dismiss packet if unexpected sender
        GamePacket packet = GamePacket.readFrom(in);
        packetHandler.handlePacket(packet);
    }

    public void send(GamePacket packet) throws IOException {
        packet.writeTo(out);
        out.flush();
    }

    public void close() throws IOException {
        in.close();
        out.close();
        socket.close();
    }
}
