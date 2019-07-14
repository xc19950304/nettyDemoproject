package NettyExample;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

public class BytebufTest {
    public static void main(String[] args)
    {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(9,100);
        buffer.writeBytes(new byte[]{1,2,3,4});
        ByteBuf slice = buffer.retainedSlice();
        buffer.readBytes(1);
        System.out.println(buffer);
        System.out.println(slice);
    }

}
