package com.authServer.vo;

/**
 * user:lufei
 * DATE:2021/12/1
 **/
public class SocialUser {

    private String access_token;
    private String remind_in;
    private long expires_in;
    private String uid;
    private String isRealName;
    public void setAccess_token(String access_token) {
        this.access_token = access_token;
    }
    public String getAccess_token() {
        return access_token;
    }

    public void setRemind_in(String remind_in) {
        this.remind_in = remind_in;
    }
    public String getRemind_in() {
        return remind_in;
    }

    public void setExpires_in(long expires_in) {
        this.expires_in = expires_in;
    }
    public long getExpires_in() {
        return expires_in;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    public String getUid() {
        return uid;
    }

    public void setIsRealName(String isRealName) {
        this.isRealName = isRealName;
    }
    public String getIsRealName() {
        return isRealName;
    }

}
