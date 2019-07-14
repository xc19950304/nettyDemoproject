package NettyExample;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

import java.nio.charset.Charset;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class NettyServer {
    public static final Map<String,Channel> map = new HashMap<String, Channel>();
    public static void main(String[] args) {
        ServerBootstrap serverBootstrap =  new ServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        serverBootstrap
                .group(boss, worker)
                .channel(NioServerSocketChannel.class)
                //产生一个新连接才会进行该处理
                .childHandler(new ChannelInitializer<NioSocketChannel>() {
                    protected void initChannel(NioSocketChannel ch) {
                            ch.pipeline().addLast(new Handler1());
                    }
                });
        bind(serverBootstrap,8000);
    }
    private static void bind(ServerBootstrap sbt, int port){
        sbt.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(future.isSuccess())
                    System.out.println("端口[" + port + "]绑定成功!");
                else
                {
                    System.err.println("端口[" + port + "]绑定失败!");
                    bind(sbt, port + 1);
                }
            }
        });
    }

    //ChannelInboundHandlerAdapter，流入处理器，以流入作为响应进行io操作
    public static class Handler1 extends ChannelInboundHandlerAdapter {

        // 每次建立新连接后，服务端主动发数据
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            Channel channel = ctx.channel();
            Attribute<Object> clientName = channel.attr(AttributeKey.newInstance("clientName"));
            map.put(clientName.toString(),channel);

            Channel channel1 = map.get("nettyClient1");
            // 1. 封装数据
            String str = "你好我是服务端";
            ByteBuf buffer = getByteBuf(channel1,str);
            // 2. 写数据
            channel1.writeAndFlush(buffer);
            System.out.println(new Date() + ": 服务端主动发数据 ->" + str );
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            //接受到数据
            ByteBuf byteBuf = (ByteBuf) msg;
            System.out.println(new Date() + ": 服务端读到数据 <- " + byteBuf.toString(Charset.forName("utf-8")));
            //写入数据

            String str = "收到你消息了，感谢";
            ByteBuf byteBuf1 = getByteBuf(ctx,str);
            ctx.channel().writeAndFlush(byteBuf1);
            System.out.println(new Date() + ": 服务端写出数据 -> "  + str);
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

        private ByteBuf getByteBuf(Channel ch, String str) {
            // 1. 获取二进制抽象 ByteBuf
            ByteBuf buffer = ch.alloc().buffer();

            // 2. 准备数据，指定字符串的字符集为 utf-8
            byte[] bytes = str.getBytes(Charset.forName("utf-8"));

            // 3. 填充数据到 ByteBuf
            buffer.writeBytes(bytes);

            return buffer;
        }


    }
}
