package com.patelheggere.tripplanner.model;

import java.util.List;

public class APIResponseModel {
	boolean status;
	int statusCode;
	String message;

	List<TalukByAcModel> talukByAcList;


	public APIResponseModel() {
		super();
	}

	public APIResponseModel(boolean status, int statusCode, String message, List<TalukByAcModel> talukByAcList) {
		this.status = status;
		this.statusCode = statusCode;
		this.message = message;
		this.talukByAcList = talukByAcList;
	}

	public boolean isStatus() {
		return status;
	}
	public void setStatus(boolean status) {
		this.status = status;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public void setStatusCode(int statusCode) {
		this.statusCode = statusCode;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}

	public List<TalukByAcModel> getTalukByAcList() {
		return talukByAcList;
	}
	public void setTalukByAcList(List<TalukByAcModel> talukByAcList) {
		this.talukByAcList = talukByAcList;
	}
	
	
	
	
}
