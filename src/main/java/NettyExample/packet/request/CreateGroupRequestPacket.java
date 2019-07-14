package NettyExample.packet.request;

import NettyExample.packet.Packet;

import java.util.List;

import static NettyExample.packet.Command.CREATE_GROUP_REQUEST;

public class CreateGroupRequestPacket extends Packet {

    private List<String> userIdList;

    public List<String> getUserIdList() {
        return userIdList;
    }

    public void setUserIdList(List<String> userIdList) {
        this.userIdList = userIdList;
    }

    @Override
    public Byte getCommand() {

        return CREATE_GROUP_REQUEST;
    }
}
