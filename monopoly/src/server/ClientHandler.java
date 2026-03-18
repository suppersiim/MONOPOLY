package server;

import java.io.*;
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
    }

    @Override
    public void run() {
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

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
        PacketType packetType = PacketType.fromInt(in.readInt());
        byte[] payload = readPacketData(in);
        packetHandler.handlePacket(packetType, payload);
    }

    // TODO: packet class to handle processing and contain data
    public void send(PacketType packetType, byte[] data) {
        if (data == null)
            data = new byte[0];

        try {
            out.writeInt(packetType.toInt());
            out.writeInt(data.length);
            out.write(data);
            out.flush();
        } catch (IOException e) {
            System.out.println("Error sending packet to client: " + e.getMessage());
        }
    }

    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing client: " + e.getMessage());
        }
    }

    /**
     * Read packet data, consuming the length and data
     * @return raw packet data
     */
    private static byte[] readPacketData(DataInputStream in) throws IOException {
        int packetSize = in.readInt();
        byte[] packetData = new byte[packetSize];
        in.readFully(packetData);
        return packetData;
    }
}
