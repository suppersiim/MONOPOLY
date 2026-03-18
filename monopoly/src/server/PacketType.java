package server;

public enum PacketType {
    TEST,
    QUIT;


    public static PacketType fromInt(int value) {
        return values()[value];
    }

    public int toInt() {
        return ordinal();
    }
}
