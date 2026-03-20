package client;

import common.GamePacket;

import java.io.*;
import java.net.Socket;

public class GameClient {

    private final Game game;
    private final String host;
    private final int port;

    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private final PacketHandler packetHandler;
    private ClientReceiver receiver;
    private volatile boolean running = false;

    public GameClient(Game game, String host, int port) {
        this.game = game;
        this.host = host;
        this.port = port;
        this.packetHandler = new PacketHandler(game);
    }

    public void connect() throws IOException {
        socket = new Socket(host, port);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());

        running = true;
        receiver = new ClientReceiver(this, in);
        new Thread(receiver).start();
    }

    public void send(GamePacket packet) throws IOException {
        packet.writeTo(out);
        out.flush();
    }

    public void onReceivePacket(GamePacket packet) {
        System.out.println("Received packet: " + packet.getType());
        packetHandler.handlePacket(packet);

        game.update();
    }

    public void disconnect() throws IOException {
        if (socket != null) socket.close();
    }

    public boolean getRunning() {
        return running;
    }
}
