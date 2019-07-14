package NettyExample.serialize;

import com.alibaba.fastjson.serializer.JSONSerializer;

public interface Serializer {

    //Serializer DEFAULT = new JSONSerializer();

    byte[] seralize(Object object);

    <T> T deserilaze(byte[] data,Class<T> clazz);

    byte getSerializeMethod();

}
