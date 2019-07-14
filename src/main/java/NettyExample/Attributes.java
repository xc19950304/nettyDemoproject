package NettyExample;

import NettyExample.entity.UserInfo;
import io.netty.util.AttributeKey;

/*封装channel的属性信息*/
public interface Attributes {
    AttributeKey<Boolean> LOGIN = AttributeKey.newInstance("login");
    AttributeKey<String> NAME = AttributeKey.newInstance("name");
    AttributeKey<UserInfo> User = AttributeKey.newInstance("user");
}
