package NettyExample.entity;

import java.util.Objects;

public class UserInfo {
    private String userName;
    private String password;
    private Long userId;

    public  UserInfo() {}
    public UserInfo(Long userId, String userName,String password)
    {
        this.userId = userId;
        this.userName = userName;
        this.password = password;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return Objects.equals(userName, userInfo.userName) &&
                Objects.equals(password, userInfo.password) &&
                Objects.equals(userId, userInfo.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, password, userId);
    }
}
