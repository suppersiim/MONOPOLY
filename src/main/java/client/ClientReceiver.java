package client;

import common.GamePacket;

import java.io.DataInputStream;

public class ClientReceiver implements Runnable {

    private final GameClient client;
    private final DataInputStream in;

    public ClientReceiver(GameClient client, DataInputStream in) {
        this.client = client;
        this.in = in;
    }

    @Override
    public void run() {
        try {
            while (client.getRunning()) {
                GamePacket packet = GamePacket.readFrom(in);
                client.onReceivePacket(packet);
            }
        } catch (Exception e) {
            System.out.println("Error receiving packet: " + e.getMessage());
        } finally {
            try {
                client.disconnect();
            } catch (Exception e) {
                System.out.println("Error disconnecting client: " + e.getMessage());
            }
        }
    }
}
