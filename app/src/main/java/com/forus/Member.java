package com.forus;

/**
 * Created by 송양종 on 2017-04-14.
 */

public class Member {
    private String Uid        ;
    private String Email      ;
    private String DisplayName;
    private String NickName   ;
    private String CreateDate ;

    public Member() {
      /*Blank default constructor essential for Firebase*/
    }

    public Member(String uid, String email, String displayName, String nickName, String createDate) {
        Uid = uid;
        Email = email;
        DisplayName = displayName;
        NickName = nickName;
        CreateDate = createDate;
    }

    public String getUid() {
        return Uid;
    }

    public String getEmail() {
        return Email;
    }

    public String getDisplayName() {
        return DisplayName;
    }

    public String getNickName() {
        return NickName;
    }

    public String getCreateDate() {
        return CreateDate;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public void setEmail(String email) {
        Email = email;
    }

    public void setDisplayName(String displayName) {
        DisplayName = displayName;
    }

    public void setNickName(String nickName) {
        NickName = nickName;
    }

    public void setCreateDate(String createDate) {
        CreateDate = createDate;
    }
}
