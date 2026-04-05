package common;

public enum PacketType {

    // client packets
    CLIENT_JOIN,
    CLIENT_START_GAME,
    CLIENT_ROLL,

    // server packets
    SERVER_JOINED_PLAYERS_COUNT,
    SERVER_GAME_STATE_UPDATE,

    // general packets
    QUIT;


    public static PacketType fromInt(int value) {
        return values()[value];
    }

    public int toInt() {
        return ordinal();
    }
}
