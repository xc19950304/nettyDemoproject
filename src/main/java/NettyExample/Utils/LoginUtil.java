package NettyExample.Utils;

import NettyExample.Attributes;
import NettyExample.entity.UserInfo;
import NettyExample.packet.request.LoginRequestPacket;
import io.netty.channel.Channel;
import io.netty.util.Attribute;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginUtil {
    public static final Map<Long, Channel> cacheMap = new HashMap<Long, Channel>();
    public static final Map<String, List<String>> chatRoomMap = new HashMap<String, List<String>>();
    public static final Map<Long,UserInfo> userMap = new HashMap<Long,UserInfo>();

    static
    {
        UserInfo user1 = new UserInfo(123L,"tom","111");
        UserInfo user2 = new UserInfo(1234L,"jack","111");
        UserInfo user3 = new UserInfo(12345L,"lily","111");
        UserInfo user4 = new UserInfo(123456L,"xc","111");
        UserInfo user5 = new UserInfo(1234567L,"lph","111");
        userMap.put(123L,user1);
        userMap.put(1234L,user2);
        userMap.put(12345L,user3);
        userMap.put(123456L,user4);
        userMap.put(1234567L,user5);
    }

    //判断用户是否登陆
    public static Boolean hasLogin(Channel channel)
    {
        //channel绑定了userinfo作为已登陆用户的session信息
        UserInfo user = channel.attr(Attributes.User).get();
        if (user == null)
            return false;
        Channel cacheChannel = cacheMap.get(user.getUserId());
        Attribute<UserInfo> cacheUser = channel.attr(Attributes.User);
        if (cacheUser == null || cacheUser.get() == null)
            return false;
        else
            return true;
    }

    public static void markAsLogin(LoginRequestPacket packet,Channel channel)
    {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserId(packet.getUserId());
        userInfo.setUserName(packet.getUsername());
        userInfo.setPassword(packet.getPassword());
        channel.attr(Attributes.User).set(userInfo);
        cacheMap.put(packet.getUserId(),channel);
    }

    public static Boolean validLoginInfo(LoginRequestPacket loginPacket)
    {
        String name = loginPacket.getUsername();
        String pwd = loginPacket.getPassword();
        Long uid = loginPacket.getUserId();
        UserInfo userInfo = userMap.get(uid);
        if(userInfo != null && userInfo.getPassword().equals(pwd)
                && userInfo.getUserName().equals(name))
            return true;
        else
            return false;
    }

    public static void removeLoginState(Channel channel)
    {
        UserInfo user = channel.attr(Attributes.User).get();
        if(user != null)
            cacheMap.remove(user.getUserId());
            channel.attr(Attributes.User).set(null);
    }


}
