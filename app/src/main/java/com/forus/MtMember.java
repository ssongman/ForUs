package com.forus;

/**
 * Created by 송양종 on 2017-04-15.
 */

public class MtMember {
    private String NickName      ;
    private String PartYN ;
    private String ParticipationDate;
    private String AuthPhotoURL ;
    private double Latitude;
    private double Longitude;

    public MtMember() {
      /*Blank default constructor essential for Firebase*/
    }
    public MtMember(String nickName, String partYN, String participationDate, String authPhotoURL) {
        NickName = nickName;
        PartYN = partYN;
        ParticipationDate = participationDate;
        AuthPhotoURL = authPhotoURL;
    }


    public String getNickName() {
        return NickName;
    }

    public String getPartYN() {
        return PartYN;
    }

    public String getParticipationDate() {
        return ParticipationDate;
    }

    public String getAuthPhotoURL() {
        return AuthPhotoURL;
    }

    public double getLatitude() {
        return Latitude;
    }

    public double getLongitude() {
        return Longitude;
    }

}
