package common;

public enum PacketType {

    // client packets
    CLIENT_JOIN,
    CLIENT_START_GAME,
    CLIENT_ROLL,
    CLIENT_BUY_RESPONSE,

    // server packets
    SERVER_JOINED_PLAYERS_COUNT,
    SERVER_GAME_STATE_UPDATE,
    SERVER_BUY_OFFER,
    SERVER_EVENT_LOG,

    // general packets
    QUIT;


    public static PacketType fromInt(int value) {
        return values()[value];
    }

    public int toInt() {
        return ordinal();
    }
}
