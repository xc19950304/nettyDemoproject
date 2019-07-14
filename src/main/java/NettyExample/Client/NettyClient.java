package NettyExample.Client;

import NettyExample.Attributes;
import NettyExample.Client.Command.ConsoleCommand;
import NettyExample.Client.Command.ConsoleCommandManager;
import NettyExample.Client.Command.LoginConsoleCommand;
import NettyExample.Utils.PacketUtil;
import NettyExample.packet.*;
import NettyExample.packet.request.LoginRequestPacket;
import NettyExample.packet.request.MessageRequestPacket;
import NettyExample.packet.response.*;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.Attribute;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.*;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

public class NettyClient {

    /*Bootstrap - NioEventLoopGroup - handler*/
    public static final int MAX_RETRY = 5;
    public static void main(String[] args) throws InterruptedException {
        //ExecutorService executorService = Executors.newCachedThreadPool();
        //AbstractQueuedSynchronizer
        //ReentrantLock
        //ConcurrentHashMap
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .attr(Attributes.NAME, "铁柱")
                .attr(Attributes.LOGIN, false)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        socketChannel.pipeline().addLast(new LoginHandler());
                        socketChannel.pipeline().addLast(new MessageHandler());
                        socketChannel.pipeline().addLast(new PacketEncoder());
                    }
                });
        connect(bootstrap,"127.0.0.1",8000,MAX_RETRY);
    }


    public static class LoginHandler extends ChannelInboundHandlerAdapter
    {
        // 客户端连接成功后调用，在该方法中编写向服务端写数据的逻辑
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            /*LoginRequestPacket loginData = new LoginRequestPacket();
            loginData.setUsername("熊畅");
            loginData.setPassword("xc199534");
            loginData.setUserId((long)123456);

            ByteBuf buffer = PacketCode.encode(loginData);
            ctx.channel().writeAndFlush(buffer);*/
            /*ctx.fireChannelActive();*/
        }
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg){

            ByteBuf buffer  = (ByteBuf) msg;
            buffer.markReaderIndex();
            Packet packet  = PacketUtil.decode(buffer);
            buffer.resetReaderIndex();
            if(packet instanceof LoginResponsePacket) {
                LoginResponsePacket loginResponsePacket = (LoginResponsePacket) packet;
                String str = loginResponsePacket.getInfo();
                Boolean isSuccess = loginResponsePacket.getSuccess();
                if(isSuccess)
                    ctx.channel().attr(Attributes.LOGIN).set(true);
                System.out.println(new Date() +" " + str);
            }
            else
                ctx.fireChannelRead(buffer);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            ctx.channel().attr(Attributes.LOGIN).set(false);
            super.channelInactive(ctx);
        }
    }

    public static class MessageHandler extends ChannelInboundHandlerAdapter
    {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg){

            ByteBuf byteBuf  = (ByteBuf) msg;
            Packet packetData = PacketUtil.decode(byteBuf);

            if (packetData instanceof MessageResponsePacket)
            {
                MessageResponsePacket messagePacket = (MessageResponsePacket) packetData;
                String message = messagePacket.getMessage();
                String fromUserName = messagePacket.getUserName();
                System.out.println(new Date() + fromUserName +" : "  + message);
            }
            else if (packetData instanceof CreateGroupResponsePacket)
            {
                CreateGroupResponsePacket createGroupResponsePacket = (CreateGroupResponsePacket) packetData;
                System.out.print("群创建成功，id 为[" + createGroupResponsePacket.getGroupId() + "], ");
                System.out.println("群里面有：" + createGroupResponsePacket.getUserNameList());
            }
            else if (packetData instanceof LogoutResponsePacket)
            {
                LogoutResponsePacket logoutResponsePacket = (LogoutResponsePacket) packetData;
                if(logoutResponsePacket.isSuccess()) {
                    ctx.channel().attr(Attributes.LOGIN).set(false);
                    System.out.println(logoutResponsePacket.getReason());
                }
                else
                    System.out.println("登出操作失败");
            }
            else if (packetData instanceof GroupMessageResponsePacket)
            {
                GroupMessageResponsePacket messagePacket = (GroupMessageResponsePacket) packetData;
                System.out.print("群聊id 为[" + messagePacket.getGroupId() + "], ");
                System.out.print("群聊用户 为[" + messagePacket.getUserList() + "], ");
                System.out.print("用户[" + messagePacket.getFromUserId() + "]");
                System.out.println("发送消息为：" + messagePacket.getMessage());
            }
            else
                System.out.println("数据并非定义类型");
        }
    }

    public static class PacketEncoder extends MessageToByteEncoder<Packet> {

        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf byteBuf) throws Exception {
            System.out.println( " PacketEncoder-encode ");
            PacketUtil.encode(packet,byteBuf);
        }
    }

    private static ByteBuf getByteBuf(ChannelHandlerContext ctx, String str) {
        // 1. 获取二进制抽象 ByteBuf
        ByteBuf buffer = ctx.alloc().buffer();


        // 2. 准备数据，指定字符串的字符集为 utf-8
        byte[] bytes = str.getBytes(Charset.forName("utf-8"));

        // 3. 填充数据到 ByteBuf
        buffer.writeBytes(bytes);

        return buffer;
    }

    //实现指数退避的自动重连模式，以2的幂次进行指数重连
    public static void connect(Bootstrap bootstrap, String host, int port, int retry)
    {
        bootstrap.connect(host, port).addListener(future -> {
            if (future.isSuccess()) {
                Channel channel = ((ChannelFuture) future).channel();
                System.out.println(new Date() +" 连接成功!");
                startCommandThread(channel);
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


    public static void startCommandThread(Channel channel)
    {
        ConsoleCommandManager consoleCommandManager = new ConsoleCommandManager();
        LoginConsoleCommand loginConsoleCommand = new LoginConsoleCommand();
        Scanner scanner = new Scanner(System.in);
        new Thread(() -> {
        while (!Thread.interrupted()){
            //客户端的登陆标志就通过channel的attr属性来判断
            Attribute<Boolean> tag = channel.attr(Attributes.LOGIN);
            if(tag == null || tag.get() == false)
            {
                loginConsoleCommand.exec(scanner,channel);
            }
            else {
                consoleCommandManager.exec(scanner,channel);
            }
        }
    }).start();
    }
}
