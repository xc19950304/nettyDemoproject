package NettyExample.Client.Command;

import io.netty.channel.Channel;

import java.util.Scanner;

//使用策略模式定义详细的命令
public interface ConsoleCommand {
    void exec(Scanner scanner, Channel channel);
}
