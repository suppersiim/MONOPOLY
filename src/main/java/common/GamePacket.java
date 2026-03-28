package common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class GamePacket {
    private final PacketType type;
    private final byte[] data;

    public GamePacket(PacketType type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public static GamePacket readFrom(DataInputStream stream) throws IOException {
        PacketType type = PacketType.fromInt(stream.readInt());
        int length = stream.readInt();
        byte[] data = new byte[length];
        stream.readFully(data);
        return new GamePacket(type, data);
    }

    public void writeTo(DataOutputStream stream) throws IOException {
        stream.writeInt(type.toInt());
        stream.writeInt(data.length);
        stream.write(data);
    }

    public DataInputStream getDataStream() {
        return new DataInputStream(new ByteArrayInputStream(data));
    }

    public byte[] getData() {
        return data;
    }

    public PacketType getType() {
        return type;
    }
}
