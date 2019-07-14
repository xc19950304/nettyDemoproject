package NettyExample.packet;

/*
* 声明各数据包对应的指令(登陆，查询，校验等)
* */
public interface Command {
    byte OTHER_COMMAND = 0;

    byte LOGIN_REQUEST = 1;

    byte LOGIN_RESPONSE = 2;

    byte MESSAGE_REQUEST = 3;

    byte MESSAGE_RESPONSE = 4;

    byte LOGOUT_REQUEST = 5;

    byte LOGOUT_RESPONSE = 6;

    byte CREATE_GROUP_REQUEST = 7;

    byte CREATE_GROUP_RESPONSE = 8;

    byte GROUP_MESSAGE_REQUEST = 9;

    byte GROUP_MESSAGE_RESPONSE = 10;
}
