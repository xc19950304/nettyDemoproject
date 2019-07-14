package NettyExample.packet.request;

import NettyExample.packet.Packet;

import static NettyExample.packet.Command.LOGOUT_REQUEST;

public class LogoutRequestPacket extends Packet {
    @Override
    public Byte getCommand() {

        return LOGOUT_REQUEST;
    }
}
