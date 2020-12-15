package com.patelheggere.tripplanner.model;

import android.os.Parcel;
import android.os.Parcelable;

public class BeneficiaryModel implements Parcelable {
    private String sl_no;
    private String name;
    private String purpose;
    private String mobile;
    private String village;
    private String status;
    private String designation;

    public BeneficiaryModel() {
    }

    public BeneficiaryModel(String sl_no, String name, String purpose, String mobile, String village, String status) {
        this.sl_no = sl_no;
        this.name = name;
        this.purpose = purpose;
        this.mobile = mobile;
        this.village = village;
        this.status = status;
    }

    protected BeneficiaryModel(Parcel in) {
        sl_no = in.readString();
        name = in.readString();
        purpose = in.readString();
        mobile = in.readString();
        village = in.readString();
        status = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(sl_no);
        dest.writeString(name);
        dest.writeString(purpose);
        dest.writeString(mobile);
        dest.writeString(village);
        dest.writeString(status);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BeneficiaryModel> CREATOR = new Creator<BeneficiaryModel>() {
        @Override
        public BeneficiaryModel createFromParcel(Parcel in) {
            return new BeneficiaryModel(in);
        }

        @Override
        public BeneficiaryModel[] newArray(int size) {
            return new BeneficiaryModel[size];
        }
    };

    public String getSl_no() {
        return sl_no;
    }

    public void setSl_no(String sl_no) {
        this.sl_no = sl_no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getVillage() {
        return village;
    }

    public void setVillage(String village) {
        this.village = village;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }
}
