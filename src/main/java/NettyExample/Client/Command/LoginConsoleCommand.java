package NettyExample.Client.Command;

import NettyExample.packet.request.LoginRequestPacket;
import io.netty.channel.Channel;

import java.util.Scanner;

public class LoginConsoleCommand implements ConsoleCommand {
    @Override
    public void exec(Scanner scanner, Channel channel) {

        System.out.println("用户名: ");
        String username = scanner.nextLine();
        System.out.println("用户id: ");
        Long userId = Long.parseLong(scanner.nextLine());
        System.out.println("用户密码: ");
        String password = scanner.nextLine();
        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();
        loginRequestPacket.setUsername(username);
        loginRequestPacket.setUserId(userId);
        loginRequestPacket.setPassword(password);
        channel.writeAndFlush(loginRequestPacket);

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }
}
