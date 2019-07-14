package NettyExample.packet.response;

import NettyExample.packet.Command;
import NettyExample.packet.Packet;

import java.util.List;

public class GroupMessageResponsePacket extends Packet {

    String fromGroupId;

    Long fromUserId;

    List<String> userList;

    String message;


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getGroupId() {
        return fromGroupId;
    }

    public void setGroupId(String groupId) {
        fromGroupId = groupId;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }

    public Long getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(Long fromUserId) {
        this.fromUserId = fromUserId;
    }

    @Override
    public Byte getCommand() {
        return Command.GROUP_MESSAGE_RESPONSE;
    }
}

