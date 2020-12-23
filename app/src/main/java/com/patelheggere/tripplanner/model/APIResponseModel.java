package com.patelheggere.tripplanner.model;

import java.util.List;

public class APIResponseModel {
	boolean status;
	int statusCode;
	String message;
	String name;
	String id;
	String phone;

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

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public List<TalukByAcModel> getTalukByAcList() {
		return talukByAcList;
	}
	public void setTalukByAcList(List<TalukByAcModel> talukByAcList) {
		this.talukByAcList = talukByAcList;
	}

	
	
}
