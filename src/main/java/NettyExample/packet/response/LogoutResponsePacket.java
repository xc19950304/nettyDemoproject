package NettyExample.packet.response;

import NettyExample.packet.Packet;

import static NettyExample.packet.Command.LOGOUT_RESPONSE;

public class LogoutResponsePacket extends Packet {

    private boolean success;

    private String reason;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public Byte getCommand() {

        return LOGOUT_RESPONSE;
    }
}
