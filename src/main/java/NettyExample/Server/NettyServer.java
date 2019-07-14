package NettyExample.Server;

import NettyExample.Attributes;
import NettyExample.Utils.IDUtil;
import NettyExample.entity.UserInfo;
import NettyExample.Utils.LoginUtil;
import NettyExample.Utils.PacketUtil;
import NettyExample.packet.*;
import NettyExample.packet.request.*;
import NettyExample.packet.response.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.charset.Charset;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

public class NettyServer {
    public static void main(String[] args) {
        ServerBootstrap serverBootstrap =  new ServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        serverBootstrap
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                //产生一个新连接才会进行该处理
                .childHandler(new ChildchannelHandler())
                //.attr(Attributes.NAME, "服务器")
                .attr(Attributes.LOGIN, false);
        bind(serverBootstrap,8000);

        Queue
    }
    private static void bind(ServerBootstrap sbt, int port){
        sbt.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(future.isSuccess()) {
                    System.out.println("端口[" + port + "]绑定成功!");
                }
                else
                {
                    System.err.println("端口[" + port + "]绑定失败!");
                    bind(sbt, port + 1);
                }
            }
        });
    }

    public static class ChildchannelHandler extends ChannelInitializer<NioSocketChannel>{
        @Override
        protected void initChannel(NioSocketChannel nioSocketChannel) throws Exception {
            nioSocketChannel.pipeline().addLast(new PacketDecoder());
            nioSocketChannel.pipeline().addLast(new LoginHandler());
            nioSocketChannel.pipeline().addLast(new LogoutHandler());
            nioSocketChannel.pipeline().addLast(new AuthenticationHandler());
            nioSocketChannel.pipeline().addLast(new MessageHandler());
            nioSocketChannel.pipeline().addLast(new CreateGroupHandler());
            nioSocketChannel.pipeline().addLast(new GroupMessageHandler());
            nioSocketChannel.pipeline().addLast(new PacketEncoder());
            //pipeline.addLast("httpServerCodec", new HttpServerCodec());
        }
    }

    public static class PacketDecoder extends ByteToMessageDecoder{

        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println( " PacketDecoder-channelActive ");
            super.channelActive(ctx);
        }

        @Override
        protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
            //System.out.println( " PacketDecoder-decode ");
            Packet packetData = PacketUtil.decode(byteBuf);
            list.add(packetData);
        }
    }
    //手动登出操作
    public static class LogoutHandler extends SimpleChannelInboundHandler<LogoutRequestPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, LogoutRequestPacket loginPacket) throws Exception {
            //System.out.println( " LoginHandler-channelRead0 ");
            LoginUtil.removeLoginState(ctx.channel());
            LogoutResponsePacket logoutResponsePacket = new LogoutResponsePacket();
            logoutResponsePacket.setSuccess(true);
            logoutResponsePacket.setReason("登出成功！");
            //byteBuf = PacketCode.encode(loginResponsePacket);
            ctx.channel().writeAndFlush(logoutResponsePacket);
        }

        //客户端断开tcp连接
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println( " LogoutHandler-channelInactive ");
            super.channelInactive(ctx);
        }
    }


    public static class LoginHandler extends SimpleChannelInboundHandler<LoginRequestPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, LoginRequestPacket loginPacket) throws Exception {
            //System.out.println( " LoginHandler-channelRead0 ");
            String str = "";
            Boolean isSuccess = false;
            if(LoginUtil.validLoginInfo(loginPacket)) {
                str = "登陆成功";
                isSuccess = true;
                LoginUtil.markAsLogin(loginPacket,ctx.channel());
                System.out.println( "["+loginPacket.getUsername() +"]:登陆成功！");
            }
            else {
                str = "信息有误，登陆失败";
                isSuccess = false;

            }
            LoginResponsePacket loginResponsePacket = new LoginResponsePacket();
            loginResponsePacket.setInfo(str);
            loginResponsePacket.setSuccess(isSuccess);
            //byteBuf = PacketCode.encode(loginResponsePacket);
            ctx.channel().writeAndFlush(loginResponsePacket);
        }

        //客户端断开tcp连接
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            System.out.println( " LoginHandler-channelInactive ");
            LoginUtil.removeLoginState(ctx.channel());
            super.channelInactive(ctx);
        }
    }

    //服务端作为消息接收和转发中心
    public static class MessageHandler extends SimpleChannelInboundHandler<MessageRequestPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext chx, MessageRequestPacket messagePacket) throws Exception {
            //System.out.println( " MessageHandler-channelRead0 ");
            UserInfo fromUserInfo = chx.channel().attr(Attributes.User).get();
            Channel channel = LoginUtil.cacheMap.get(messagePacket.getToUserId());
            MessageResponsePacket messageResponsePacket = new MessageResponsePacket();
            if(channel == null) {
                messageResponsePacket.setUserName("服务器消息");
                messageResponsePacket.setMessage("用户未连接，拒绝消息传输");
                chx.channel().writeAndFlush(messageResponsePacket);
            }
            else{
                messageResponsePacket.setFromUserId(fromUserInfo.getUserId());
                messageResponsePacket.setUserName(fromUserInfo.getUserName());
                messageResponsePacket.setMessage(messagePacket.getMessage());
                channel.writeAndFlush(messageResponsePacket);
            }
            String message = messagePacket.getMessage();
            System.out.println(new Date() + " 客户端: "  + message);
        }
    }

    public static class GroupMessageHandler extends SimpleChannelInboundHandler<GroupMessageRequestPacket>{

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, GroupMessageRequestPacket groupMessageRequestPacket) throws Exception {
            UserInfo fromUserInfo = ctx.channel().attr(Attributes.User).get();
            String groupId = groupMessageRequestPacket.getGroupId();
            String message = groupMessageRequestPacket.getMessage();
            List<String> userList = LoginUtil.chatRoomMap.get(groupId);
            List<String> userNameList = new ArrayList<>();

            ChannelGroup channelGroup = new DefaultChannelGroup(ctx.executor());
            for(String userId:userList){
                Channel channel = LoginUtil.cacheMap.get(Long.parseLong(userId));
                if (channel != null) {
                    channelGroup.add(channel);
                    userNameList.add(channel.attr(Attributes.User).get().getUserName());
                }
            }

            GroupMessageResponsePacket gmrPacket = new GroupMessageResponsePacket();
            gmrPacket.setMessage(message);
            gmrPacket.setUserList(userNameList);
            gmrPacket.setFromUserId(fromUserInfo.getUserId());
            gmrPacket.setGroupId(groupId);

            channelGroup.writeAndFlush(gmrPacket);

            System.out.print("群聊id 为[" + groupId + "], ");
            System.out.print("用户id 为[" + fromUserInfo.getUserId() + "], ");
            System.out.println("消息为：" + message);

        }
    }

    public static class CreateGroupHandler extends SimpleChannelInboundHandler<CreateGroupRequestPacket> {

        @Override
        protected void channelRead0(ChannelHandlerContext ctx, CreateGroupRequestPacket createGroupRequestPacket) {
            UserInfo fromUserInfo = ctx.channel().attr(Attributes.User).get();
            Channel fromChannel = LoginUtil.cacheMap.get(fromUserInfo.getUserId());
            List<String> userIdList = createGroupRequestPacket.getUserIdList();
            List<String> userNameList = new ArrayList<>();
            // 1. 创建一个 channel 分组
            ChannelGroup channelGroup = new DefaultChannelGroup(ctx.executor());

            // 2.1. 筛选出待加入群聊的用户的 channel 和 userName
            for (String userId : userIdList) {
                Channel channel = LoginUtil.cacheMap.get(Long.parseLong(userId));
                if (channel != null) {
                    if(userId.equals(String.valueOf(fromUserInfo.getUserId())))
                        continue;
                    channelGroup.add(channel);
                    userNameList.add(channel.attr(Attributes.User).get().getUserName());
                }
            }
            //2.2.将发起群聊者拉入群聊
            channelGroup.add(fromChannel);
            userIdList.add(String.valueOf(fromUserInfo.getUserId()));
            userNameList.add(fromChannel.attr(Attributes.User).get().getUserName());

            //2.3. 保存聊天室
            String groupID = IDUtil.randomId();
            LoginUtil.chatRoomMap.put(groupID,userIdList);

            // 3. 创建群聊创建结果的响应
            CreateGroupResponsePacket createGroupResponsePacket = new CreateGroupResponsePacket();
            createGroupResponsePacket.setSuccess(true);
            createGroupResponsePacket.setGroupId(groupID);
            createGroupResponsePacket.setUserNameList(userNameList);

            // 4. 给每个客户端发送拉群通知
            channelGroup.writeAndFlush(createGroupResponsePacket);

            System.out.print("群创建成功，id 为[" + createGroupResponsePacket.getGroupId() + "], ");
            System.out.println("群里面有：" + createGroupResponsePacket.getUserNameList());

        }
    }

    public static class PacketEncoder extends MessageToByteEncoder<Packet> {

        @Override
        protected void encode(ChannelHandlerContext channelHandlerContext, Packet packet, ByteBuf byteBuf) throws Exception {
            System.out.println( " PacketEncoder-encode ");
            PacketUtil.encode(packet,byteBuf);
        }
    }

    //接受消息前做一次判断
    public static class AuthenticationHandler extends ChannelInboundHandlerAdapter
    {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg){
            //System.out.println( " AuthenticationHandler-channelRead ");
            if(LoginUtil.hasLogin(ctx.channel())){
                ctx.pipeline().remove(this);
                ctx.fireChannelRead(msg);
            }
            else {
                ctx.channel().close();
            }
        }

        @Override
        public void handlerRemoved(ChannelHandlerContext ctx) {
            //System.out.println( " AuthenticationHandler-handlerRemoved ");
            if (LoginUtil.hasLogin(ctx.channel())) {
                System.out.println("当前连接登录验证完毕，无需再次验证, AuthHandler 被移除");
            } else {
                System.out.println("无登录验证，强制关闭连接!");
            }
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

    private static ByteBuf getByteBuf(Channel ch, String str) {
        // 1. 获取二进制抽象 ByteBuf
        ByteBuf buffer = ch.alloc().buffer();

        // 2. 准备数据，指定字符串的字符集为 utf-8
        byte[] bytes = str.getBytes(Charset.forName("utf-8"));

        // 3. 填充数据到 ByteBuf
        buffer.writeBytes(bytes);

        return buffer;
    }

    private static void startCommandThread(Channel channel) {
        new Thread(() -> {
            while (!Thread.interrupted()) {
                //发送信息前会判断当前channel是否建立了连接,但每次都要判断不合理
                //if(LoginUtil.hasLogin(channel))
                {
                    System.out.println("输入消息发送至客户端: ");
                    Scanner sc = new Scanner(System.in);
                    String line = sc.nextLine();

                    MessageRequestPacket packet = new MessageRequestPacket();
                    packet.setMessage(line);
                    ByteBuf byteBuf = PacketUtil.encode(packet);
                    channel.writeAndFlush(byteBuf);
                }
            }
        }).start();
    }

    //ChannelInboundHandlerAdapter，流入处理器，以流入作为响应进行io操作
    public static class Handler1 extends ChannelInboundHandlerAdapter {
        // 每次建立新连接后(客户端到服务端)，服务端主动发数据
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            /*Channel channel = ctx.channel();
            Attribute<Object> clientName = channel.attr(AttributeKey.newInstance("clientName"));
            map.put(clientName.toString(),channel);

            Channel channel1 = map.get("nettyClient1");
            // 1. 封装数据
            String str = "你好我是服务端";
            ByteBuf buffer = getByteBuf(channel1,str);
            // 2. 写数据
            channel1.writeAndFlush(buffer);
            System.out.println(new Date() + ": 服务端主动发数据 ->" + str );.*/
        }

        //接受数据并进行io读写
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            //接受到数据
            ByteBuf byteBuf = (ByteBuf) msg;
            String str ="";
            Packet packetData = PacketUtil.decode(byteBuf);
            if (packetData instanceof LoginRequestPacket) {
                LoginRequestPacket loginPacket = (LoginRequestPacket) packetData;
                if(LoginUtil.validLoginInfo(loginPacket)) {
                    str = "登陆成功";
                    ctx.channel().attr(Attributes.LOGIN).set(true);
                    startCommandThread(ctx.channel());
                }
                else
                    str ="信息有误，登陆失败";
                LoginResponsePacket loginResponsePacket = new LoginResponsePacket();
                loginResponsePacket.setInfo(str);
                byteBuf = PacketUtil.encode(loginResponsePacket);
                ctx.channel().writeAndFlush(byteBuf);
                //System.out.println(new Date()  + str);
            }
            else if (packetData instanceof MessageRequestPacket)
            {
                MessageRequestPacket messagePacket = (MessageRequestPacket) packetData;
                String message = messagePacket.getMessage();
                System.out.println(new Date() + " 客户端: "  + message);
            }
        }
    }

}
