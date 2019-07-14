package NettyExample.Client;

import NettyExample.Attributes;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyClient2 {

    /*Bootstrap - NioEventLoopGroup - handler*/
    public static final int MAX_RETRY = 5;
    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .attr(Attributes.NAME, "铁柱")
                .attr(Attributes.LOGIN, false)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new NettyClient.LoginHandler());
                        socketChannel.pipeline().addLast(new NettyClient.MessageHandler());
                        socketChannel.pipeline().addLast(new NettyClient.PacketEncoder());
                    }
                });
        NettyClient.connect(bootstrap,"127.0.0.1",8000,MAX_RETRY);
    }
}
