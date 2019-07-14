package NettyExample.packet;

import com.alibaba.fastjson.annotation.JSONField;

public abstract class Packet {
    /**
     * 协议版本
     */
    @JSONField(deserialize = false, serialize = false)
    private Byte version = 1;

    @JSONField(serialize = false)
    public Byte getVersion(){
        return version;
    }

    @JSONField(serialize = false)
    public abstract Byte getCommand();
}
