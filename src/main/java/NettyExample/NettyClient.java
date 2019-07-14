package NettyExample;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.concurrent.TimeUnit;
public class NettyClient {

    public static final int MAX_RETRY = 5;
    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new Handler1());
                    }
                });
        connect(bootstrap,"127.0.0.1",8000,MAX_RETRY);
        bootstrap.attr(AttributeKey.newInstance("clientName"), "nettyClient1");
        /*while (true) {
            channel.writeAndFlush(new Date() + ": hello world!");
            Thread.sleep(2000);
        }*/
    }

    //实现指数退避的自动重连模式，以2的幂次进行指数重连
    private static void connect(Bootstrap bootstrap, String host, int port, int retry)
    {
        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("连接成功!");
            } else if (retry == 0) {
                System.err.println("重试次数已用完，放弃连接！");
            } else {
                // 第几次重连
                int order = (MAX_RETRY - retry) + 1;
                // 本次重连的间隔
                int delay = 1 << order;
                System.err.println(new Date() + ": 连接失败，第" + order + "次重连……");
                //定时任务，bootstrap.config()返回对引导类配置的抽象，.group返回开始配置的工作线程组
                bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit
                        .SECONDS);
            }
        });
    }

    public static class Handler1 extends ChannelInboundHandlerAdapter
    {
        // 客户端连接成功后即可调用该方法，在该方法中编写向服务端写数据的逻辑
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            String str = "你好我是客户端";
            // 1. 获取数据
            ByteBuf buffer = getByteBuf(ctx,str);
            // 2. 写数据
            ctx.channel().writeAndFlush(buffer);
            System.out.println(new Date() + ": 客户端主动发数据->"+ str );
        }


        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg){
            ByteBuf buffer  = (ByteBuf) msg;
            System.out.println(new Date() + ": 客户端读到数据 -> "+ buffer.toString(Charset.forName("utf-8")));

            /*String str = "也收到你消息了，感谢";
            ByteBuf byteBuf1 = getByteBuf(ctx,str);
            ctx.channel().writeAndFlush(byteBuf1);
            System.out.println(new Date() + ": 客户端写出数据 -> "  + str);*/

        }

        private ByteBuf getByteBuf(ChannelHandlerContext ctx, String str) {
            // 1. 获取二进制抽象 ByteBuf
            ByteBuf buffer = ctx.alloc().buffer();

            // 2. 准备数据，指定字符串的字符集为 utf-8
            byte[] bytes = str.getBytes(Charset.forName("utf-8"));

            // 3. 填充数据到 ByteBuf
            buffer.writeBytes(bytes);

            return buffer;
        }

    }

}
