package com.patelheggere.tripplanner.model;

import android.os.Parcel;
import android.os.Parcelable;

public class TalukByAcModel implements Parcelable {
    String KGISTalukCode, KGISTalukName, KGISHobliCode, KGISTHobliName, ASBLY_CSTNY_NUM;
    String PS_NO, PS_Name, BoothCode, AC_CODE;
    String polyGon;


    public TalukByAcModel() {
        super();
    }



    public TalukByAcModel(String kGISTalukCode, String kGISTalukName, String kGISHobliCode, String kGISTHobliName,
                          String aSBLY_CSTNY_NUM, String pS_NO, String pS_Name, String boothCode, String aC_CODE, String polyGon) {
        super();
        KGISTalukCode = kGISTalukCode;
        KGISTalukName = kGISTalukName;
        KGISHobliCode = kGISHobliCode;
        KGISTHobliName = kGISTHobliName;
        ASBLY_CSTNY_NUM = aSBLY_CSTNY_NUM;
        PS_NO = pS_NO;
        PS_Name = pS_Name;
        BoothCode = boothCode;
        AC_CODE = aC_CODE;
        this.polyGon = polyGon;
    }


    protected TalukByAcModel(Parcel in) {
        KGISTalukCode = in.readString();
        KGISTalukName = in.readString();
        KGISHobliCode = in.readString();
        KGISTHobliName = in.readString();
        ASBLY_CSTNY_NUM = in.readString();
        PS_NO = in.readString();
        PS_Name = in.readString();
        BoothCode = in.readString();
        AC_CODE = in.readString();
        polyGon = in.readString();
    }

    public static final Creator<TalukByAcModel> CREATOR = new Creator<TalukByAcModel>() {
        @Override
        public TalukByAcModel createFromParcel(Parcel in) {
            return new TalukByAcModel(in);
        }

        @Override
        public TalukByAcModel[] newArray(int size) {
            return new TalukByAcModel[size];
        }
    };

    public String getKGISTalukCode() {
        return KGISTalukCode;
    }


    public void setKGISTalukCode(String kGISTalukCode) {
        KGISTalukCode = kGISTalukCode;
    }


    public String getKGISTalukName() {
        return KGISTalukName;
    }


    public void setKGISTalukName(String kGISTalukName) {
        KGISTalukName = kGISTalukName;
    }


    public String getKGISHobliCode() {
        return KGISHobliCode;
    }


    public void setKGISHobliCode(String kGISHobliCode) {
        KGISHobliCode = kGISHobliCode;
    }


    public String getKGISTHobliName() {
        return KGISTHobliName;
    }


    public void setKGISTHobliName(String kGISTHobliName) {
        KGISTHobliName = kGISTHobliName;
    }


    public String getASBLY_CSTNY_NUM() {
        return ASBLY_CSTNY_NUM;
    }


    public void setASBLY_CSTNY_NUM(String aSBLY_CSTNY_NUM) {
        ASBLY_CSTNY_NUM = aSBLY_CSTNY_NUM;
    }


    public String getPolyGon() {
        return polyGon;
    }


    public void setPolyGon(String polyGon) {
        this.polyGon = polyGon;
    }





    public String getPS_NO() {
        return PS_NO;
    }





    public void setPS_NO(String pS_NO) {
        PS_NO = pS_NO;
    }





    public String getPS_Name() {
        return PS_Name;
    }





    public void setPS_Name(String pS_Name) {
        PS_Name = pS_Name;
    }





    public String getBoothCode() {
        return BoothCode;
    }





    public void setBoothCode(String boothCode) {
        BoothCode = boothCode;
    }





    public String getAC_CODE() {
        return AC_CODE;
    }





    public void setAC_CODE(String aC_CODE) {
        AC_CODE = aC_CODE;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(KGISTalukCode);
        parcel.writeString(KGISTalukName);
        parcel.writeString(KGISHobliCode);
        parcel.writeString(KGISTHobliName);
        parcel.writeString(ASBLY_CSTNY_NUM);
        parcel.writeString(PS_NO);
        parcel.writeString(PS_Name);
        parcel.writeString(BoothCode);
        parcel.writeString(AC_CODE);
        parcel.writeString(polyGon);
    }
}