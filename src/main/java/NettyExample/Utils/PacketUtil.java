package NettyExample.Utils;

import NettyExample.packet.Packet;
import NettyExample.packet.request.*;
import NettyExample.packet.response.*;
import NettyExample.serialize.JSONSerializer;
import NettyExample.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.util.HashMap;
import java.util.Map;

import static NettyExample.packet.Command.*;
import static NettyExample.serialize.SerializeMethod.JSON;

/*
该类用于对二进制的数据Packet进行封装成ByteBuf；以及将ByteBuf封装成Packet
*/
public class PacketUtil {

    private static final int MAGIC_NUMBER = 0x12345678;
    private static final Map<Byte, Class<? extends Packet>> packetTypeMap;
    private static final Map<Byte, Serializer> serializerMap;

    static {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(LOGIN_REQUEST, LoginRequestPacket.class);
        packetTypeMap.put(LOGIN_RESPONSE, LoginResponsePacket.class);
        packetTypeMap.put(MESSAGE_REQUEST, MessageRequestPacket.class);
        packetTypeMap.put(MESSAGE_RESPONSE, MessageResponsePacket.class);
        packetTypeMap.put(LOGOUT_REQUEST, LogoutRequestPacket.class);
        packetTypeMap.put(LOGOUT_RESPONSE, LogoutResponsePacket.class);
        packetTypeMap.put(CREATE_GROUP_REQUEST, CreateGroupRequestPacket.class);
        packetTypeMap.put(CREATE_GROUP_RESPONSE, CreateGroupResponsePacket.class);
        packetTypeMap.put(GROUP_MESSAGE_REQUEST, GroupMessageRequestPacket.class);
        packetTypeMap.put(GROUP_MESSAGE_RESPONSE, GroupMessageResponsePacket.class);

        serializerMap = new HashMap<>();
        Serializer serializer = new JSONSerializer();
        serializerMap.put(serializer.getSerializeMethod(), serializer);
    }


    public static ByteBuf encode(Packet packet) {
        // 1. 创建 ByteBuf 对象
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();
        Serializer serializer = serializerMap.get(JSON);
        // 2. 序列化 java 对象
        byte[] bytes = serializer.seralize(packet);

        // 3. 实际编码过程
        byteBuf.writeInt(MAGIC_NUMBER);
        byteBuf.writeByte(packet.getVersion());
        byteBuf.writeByte(serializer.getSerializeMethod());
        byteBuf.writeByte(packet.getCommand());
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);

        return byteBuf;
    }

    public static void encode(Packet packet, ByteBuf byteBuf) {
        // 1. 序列化 java 对象
        Serializer serializer = serializerMap.get(JSON);
        byte[] bytes = serializer.seralize(packet);

        // 2. 实际编码过程
        byteBuf.writeInt(MAGIC_NUMBER);
        byteBuf.writeByte(packet.getVersion());
        byteBuf.writeByte(serializer.getSerializeMethod());
        byteBuf.writeByte(packet.getCommand());
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);

    }


    public static Packet decode(ByteBuf byteBuf) {
        // 跳过 magic number
        byteBuf.skipBytes(4);

        // 跳过版本号
        byteBuf.skipBytes(1);

        // 序列化算法
        byte serializeAlgorithm = byteBuf.readByte();

        // 指令
        byte command = byteBuf.readByte();

        // 数据包长度
        int length = byteBuf.readInt();

        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);

        //获取序列化所用的算法(通过serialize指令获取)
        Class<? extends Packet> requestType = getRequestType(command);

        //获取该包对应的class对象(通过command指令获取)
        Serializer serializer = getSerializer(serializeAlgorithm);

        //若传过来的包不符合标准则未空
        if (requestType != null && serializer != null) {
            return serializer.deserilaze(bytes,requestType);
        }

        return null;
    }

    private static Serializer getSerializer(byte serializeAlgorithm) {

        return serializerMap.get(serializeAlgorithm);
    }

    private static Class<? extends Packet> getRequestType(byte command) {

        return packetTypeMap.get(command);
    }
}