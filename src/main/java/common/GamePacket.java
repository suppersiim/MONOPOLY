package common;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class GamePacket {
    private final PacketType type;
    private final byte[] data;

    public GamePacket(PacketType type, byte[] data) {
        this.type = type;
        this.data = data;
    }

    public GamePacket(PacketType type, String data) {
        this.type = type;
        this.data = data.getBytes(StandardCharsets.UTF_8);
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

    public String getStringData() { return new String(data); }

    public PacketType getType() {
        return type;
    }
}
