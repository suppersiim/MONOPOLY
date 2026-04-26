package client.client;

import client.Game;
import common.GamePacket;
import common.PacketType;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
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

    public synchronized void send(GamePacket packet) throws IOException {
        packet.writeTo(out);
        out.flush();
    }

    public void disconnect() throws IOException {
        running = false;
        if (socket != null) socket.close();
    }

    public void sendJoinGame() throws IOException {
        GamePacket packet = new GamePacket(PacketType.CLIENT_JOIN, game.getPlayerName().getBytes());
        game.getClient().send(packet);
    }

    public void sendStartGame() throws IOException {
        GamePacket packet = new GamePacket(PacketType.CLIENT_START_GAME, new byte[0]);
        game.getClient().send(packet);
    }

    public void sendRoll() throws IOException {
        GamePacket packet = new GamePacket(PacketType.CLIENT_ROLL, new byte[0]);
        game.getClient().send(packet);
    }

    public void sendBuyResponse(boolean accepted) throws IOException {
        String response = accepted ? "yes" : "no";
        GamePacket packet = new GamePacket(PacketType.CLIENT_BUY_RESPONSE, response);
        send(packet);
    }

    public void sendBuyHouseResponse(boolean accepted, String streetName) throws IOException {
        String response = accepted ? "yes:" + streetName : "no";
        System.out.println("Sending buy house response: " + response);
        GamePacket packet = new GamePacket(PacketType.CLIENT_BUY_HOUSE_RESPONSE, response);
        send(packet);
    }

    public void sendMortgageRequest(String propertyName) throws IOException {
        GamePacket packet = new GamePacket(PacketType.CLIENT_MORTGAGE, propertyName);
        send(packet);
    }

    public void sendUnmortgageRequest(String propertyName) throws IOException {
        GamePacket packet = new GamePacket(PacketType.CLIENT_UNMORTGAGE, propertyName);
        send(packet);
    }

    public void sendEndTurn() throws IOException {
        GamePacket packet = new GamePacket(PacketType.CLIENT_END_TURN, new byte[0]);
        send(packet);
    }

    public boolean getRunning() {
        return running;
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }
}
