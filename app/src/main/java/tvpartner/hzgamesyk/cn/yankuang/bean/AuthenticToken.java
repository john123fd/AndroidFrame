package tvpartner.hzgamesyk.cn.yankuang.bean;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by zhanghuan on 2016/3/17.
 */
public class AuthenticToken implements Serializable {
    private static final long serialVersionUID = 1810881524205570133L;
    private long uid;
    private String token;
    private long expireTime;

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(long expireTime) {
        this.expireTime = expireTime;
    }

    public boolean isExpired() {
        return this.expireTime < new Date().getTime();
    }
}
