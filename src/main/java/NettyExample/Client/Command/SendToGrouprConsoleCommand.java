package NettyExample.Client.Command;

import NettyExample.packet.request.GroupMessageRequestPacket;
import NettyExample.packet.request.MessageRequestPacket;
import io.netty.channel.Channel;

import java.util.Scanner;

public class SendToGrouprConsoleCommand implements ConsoleCommand {
    @Override
    public void exec(Scanner scanner, Channel channel) {
        GroupMessageRequestPacket packet = new GroupMessageRequestPacket();
        System.out.println("请输入传输内容: ");
        String line = scanner.nextLine();
        System.out.println("请输入消息传输群组ID: ");
        String toGroupId = scanner.nextLine();
        packet.setGroupId(toGroupId);
        packet.setMessage(line);
        channel.writeAndFlush(packet);
    }
}
