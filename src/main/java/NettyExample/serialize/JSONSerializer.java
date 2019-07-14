package NettyExample.serialize;

import com.alibaba.fastjson.JSON;

public class JSONSerializer implements Serializer {
    @Override
    public byte[] seralize(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserilaze(byte[] data, Class<T> clazz) {
        return JSON.parseObject(data, clazz);
    }

    @Override
    public byte getSerializeMethod() {
        return SerializeMethod.JSON;
    }
}
