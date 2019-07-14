package NettyExample.packet.response;

import NettyExample.packet.Packet;

import static NettyExample.packet.Command.LOGIN_RESPONSE;

public class LoginResponsePacket extends Packet {

    private String info;

    public Boolean isSuccess;
    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public Boolean getSuccess() {
        return isSuccess;
    }

    public void setSuccess(Boolean success) {
        isSuccess = success;
    }

    @Override
    public Byte getCommand() {
        return LOGIN_RESPONSE;
    }
}
