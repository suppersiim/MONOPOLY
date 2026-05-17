package client.client;

import client.Game;
import common.GamePacket;
import common.PacketType;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;

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

    public void sendTradeOffer(String name, int offerMoney, List<String> offeredNames, int requestMoney, List<String> requestedNames) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            dos.writeUTF(name);
            dos.writeInt(offerMoney);
            dos.writeInt(offeredNames.size());
            for (String prop : offeredNames) {
                int squareIndex = game.getGameState().getSquareIndexByName(prop);
                if (squareIndex < 0) {
                    System.out.println("Invalid property name in trade offer: " + prop);
                    return;
                }
                dos.writeInt(squareIndex);
            }
            dos.writeInt(requestMoney);
            dos.writeInt(requestedNames.size());
            for (String prop : requestedNames) {
                int squareIndex = game.getGameState().getSquareIndexByName(prop);
                if (squareIndex < 0) {
                    System.out.println("Invalid property name in trade offer: " + prop);
                    return;
                }
                dos.writeInt(squareIndex);
            }
            dos.flush();
            GamePacket packet = new GamePacket(PacketType.CLIENT_TRADE_OFFER, baos.toByteArray());
            send(packet);
        } catch (IOException e) {
            System.out.println("Error sending trade offer: " + e.getMessage());
        }
    }

    public void sendTradeResponse(long tradeUID, boolean accepted) {
        try {
            String payload = (accepted ? "accepted" : "rejected") + ":" + tradeUID;
            send(new GamePacket(PacketType.CLIENT_TRADE_RESPONSE, payload));
        } catch (IOException e) {
            System.out.println("Error sending trade response: " + e.getMessage());
        }
    }

    public void sendUnmortgageRequest(String name) throws IOException {
        GamePacket packet = new GamePacket(PacketType.CLIENT_UNMORTGAGE, name);
        send(packet);
    }

    public void sendJailCardResponse(boolean accepted) throws IOException {
        String response = accepted ? "yes" : "no";
        GamePacket packet = new GamePacket(PacketType.CLIENT_USE_JAIL_CARD_RESPONSE, response);
        send(packet);
    }

    public void sendFinishTurn() throws IOException {
        GamePacket packet = new GamePacket(PacketType.CLIENT_FINISH_TURN, new byte[0]);
        send(packet);
    public void sendAuctionBid(String playerName, int amount) throws IOException {
        send(new GamePacket(PacketType.CLIENT_AUCTION_BID, playerName + ":" + amount));
    }

    public void sendAuctionPass(String playerName) throws IOException {
        send(new GamePacket(PacketType.CLIENT_AUCTION_PASS, playerName));
    }

    public boolean getRunning() {
        return running;
    }

    public PacketHandler getPacketHandler() {
        return packetHandler;
    }
}
