package NettyExample.packet.request;

import NettyExample.packet.Command;
import NettyExample.packet.Packet;

public class MessageRequestPacket extends Packet {

    Long toUserId;

    String message;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getToUserId() {
        return toUserId;
    }

    public void setToUserId(Long toUserId) {
        this.toUserId = toUserId;
    }

    @Override
    public Byte getCommand() {
        return Command.MESSAGE_REQUEST;
    }
}
