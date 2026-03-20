package client;

import common.GamePacket;
import common.PacketType;

public class PacketHandler {
    private final Game game;

    public PacketHandler(Game game) {
        this.game = game;
    }

    private void handleGameStateUpdate(GamePacket packet) {
        game.getGameState().deserialize(packet.getData());
    }

    public void handlePacket(GamePacket packet) {
        switch (packet.getType()) {
            case PacketType.SERVER_GAME_STATE_UPDATE:
                handleGameStateUpdate(packet);
                break;
        }
    }
}
