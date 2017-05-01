package com.forus;

/**
 * Created by 송양종 on 2017-04-02.
 */

public class Meeting {
    private String MtName;
    private String MtPass;
    private String MtCrdt;
    private String MtDesc;
    private String MtLeader;
    private String MtNotify_yn;
    private int  MtMembersCnt;

    public Meeting() {
      /*Blank default constructor essential for Firebase*/
    }

    public Meeting(String mtName, String mtPass, String mtCrdt, String mtDesc, String mtNotify_yn) {
        MtName = mtName;
        MtPass = mtPass;
        MtCrdt = mtCrdt;
        MtDesc = mtDesc;
        MtNotify_yn = mtNotify_yn;
    }

    public String getMtName() {
        return MtName;
    }

    public String getMtPass() {
        return MtPass;
    }

    public String getMtCrdt() {
        return MtCrdt;
    }

    public String getMtDesc() {
        return MtDesc;
    }

    public String getMtNotify_yn() {
        return MtNotify_yn;
    }

    public String getMtLeader() {
        return MtLeader;
    }

    public int getMtMembersCnt() {
        return MtMembersCnt;
    }

    public void setMtName(String mtName) {
        MtName = mtName;
    }

    public void setMtPass(String mtPass) {
        MtPass = mtPass;
    }

    public void setMtCrdt(String mtCrdt) {
        MtCrdt = mtCrdt;
    }

    public void setMtDesc(String mtDesc) {
        MtDesc = mtDesc;
    }

    public void setMtLeader(String mtLeader) {
        MtLeader = mtLeader;
    }

    public void setMtNotify_yn(String mtNotify_yn) {
        MtNotify_yn = mtNotify_yn;
    }

    public void setMtMembersCnt(int mtMembersCnt) {
        MtMembersCnt = mtMembersCnt;
    }
}
