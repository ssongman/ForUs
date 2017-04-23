package com.forus;

/**
 * Created by 송양종 on 2017-04-02.
 */

public class Meeting {
    private String MtName;
    private String MtPass;
    private String MtFrdt;
    private String MtFrtm;
    private String MtDesc;
    private String MtLeader;
    private String MtNotify_yn;

    public Meeting() {
      /*Blank default constructor essential for Firebase*/
    }

    public Meeting(String mtName, String mtPass, String mtFrdt, String mtFrtm, String mtTodt, String mtTotm, String mtDesc, String mtNotify_yn) {
        MtName = mtName;
        MtPass = mtPass;
        MtFrdt = mtFrdt;
        MtFrtm = mtFrtm;
        MtDesc = mtDesc;
        MtNotify_yn = mtNotify_yn;
    }

    public String getMtName() {
        return MtName;
    }

    public String getMtPass() {
        return MtPass;
    }

    public String getMtFrdt() {
        return MtFrdt;
    }

    public String getMtFrtm() {
        return MtFrtm;
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
}
