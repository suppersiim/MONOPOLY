package client;

import server.GameServer;
import server.PacketType;

import java.io.*;
import java.net.Socket;

public class GameClient {

    private final String host;
    private final int port;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public GameClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public void send(PacketType packetType, byte[] data) throws IOException {
        if (data == null)
            data = new byte[0];

        out.writeInt(packetType.toInt());
        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    public String receive() throws IOException {
        PacketType packetType = PacketType.fromInt(in.readInt());
        int length = in.readInt();
        byte[] data = new byte[length];
        in.readFully(data);
        return "Received packet of type " + packetType + " with data: " + new String(data);
    }

    public void disconnect() throws IOException {
        if (socket != null) socket.close();
    }
}
