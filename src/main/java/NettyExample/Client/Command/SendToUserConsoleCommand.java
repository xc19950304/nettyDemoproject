package NettyExample.Client.Command;

import NettyExample.packet.request.MessageRequestPacket;
import io.netty.channel.Channel;

import java.util.Scanner;

public class SendToUserConsoleCommand implements ConsoleCommand {
    @Override
    public void exec(Scanner scanner, Channel channel) {
        MessageRequestPacket packet = new MessageRequestPacket();

        String temp = scanner.nextLine();
        System.out.println("请输入传输内容: ");
        String line = scanner.nextLine();
        System.out.println("请输入消息传输对象ID: ");
        String toUserId = scanner.nextLine();

        packet.setToUserId(Long.parseLong(toUserId));
        packet.setMessage(line);
        channel.writeAndFlush(packet);

    }
}
