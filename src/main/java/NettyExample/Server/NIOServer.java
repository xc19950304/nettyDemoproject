package NettyExample.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/*NIO 模型中通常会有两个线程，每个线程绑定一个轮询器 selector,
本例中selector1负责轮询是否有新的连接，selector2负责轮询连接是否有数据可读
selector1监测到新的连接之后，直接将新连接绑定到selector2上，这样就不用 IO 模型中 1w 个 while 循环在死等，*/
public class NIOServer {
    public static void main(String[] args) throws Exception {
        final Selector selector1 = Selector.open();
        final Selector selector2 = Selector.open();
         new Thread(new Runnable(){
             public void run() {
                 try {
                     ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
                     serverSocketChannel.bind(new InetSocketAddress(8000));
                     serverSocketChannel.configureBlocking(false);
                     serverSocketChannel.register(selector1, SelectionKey.OP_ACCEPT);
                     while(true)
                     {
                         // 监测是否有新的连接，这里的1指的是阻塞的时间为 1ms
                         if (selector1.select(1) > 0)
                         {
                             Set<SelectionKey> set = selector1.selectedKeys();
                             Iterator<SelectionKey> keyIterator = set.iterator();

                             while (keyIterator.hasNext()) {
                                 SelectionKey key = keyIterator.next();

                                 if (key.isAcceptable()) {
                                     try {
                                         // 每来一个新连接，不需要创建一个线程，而是直接注册到selector2
                                         SocketChannel sc = ((ServerSocketChannel) key.channel()).accept();
                                         sc.configureBlocking(false);
                                         sc.register(selector2, SelectionKey.OP_READ);
                                     } finally {
                                         keyIterator.remove();
                                     }
                                 }
                             }
                         }
                     }
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }).start();

        new Thread(() -> {
            try {
                while (true) {
                    // 批量轮询是否有哪些连接有数据可读，这里的1指的是阻塞的时间为 1ms
                    if (selector2.select(1) > 0) {
                        Set<SelectionKey> set = selector2.selectedKeys();
                        Iterator<SelectionKey> keyIterator = set.iterator();

                        while (keyIterator.hasNext()) {
                            SelectionKey key = keyIterator.next();

                            if (key.isReadable()) {
                                try {
                                    SocketChannel clientChannel = (SocketChannel) key.channel();
                                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                    // (3) 面向 Buffer
                                    clientChannel.read(byteBuffer);
                                    byteBuffer.flip();
                                    System.out.println(Charset.defaultCharset().newDecoder().decode(byteBuffer)
                                            .toString());
                                } finally {
                                    keyIterator.remove();
                                    key.interestOps(SelectionKey.OP_READ);
                                }
                            }
                        }
                    }
                }
            } catch (IOException ignored) {
            }
        }).start();

    }
}