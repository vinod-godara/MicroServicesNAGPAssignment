package com.nagp.microservices.accountservices.proxies;

import java.util.List;

public class User {

	private String userID;

	private String userAddress;

	private String userEmail;

	private List<Long> userAccounts;

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public String getUserAddress() {
		return userAddress;
	}

	public void setUserAddress(String userAddress) {
		this.userAddress = userAddress;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public List<Long> getUserAccounts() {
		return userAccounts;
	}

	public void setUserAccounts(List<Long> userAccounts) {
		this.userAccounts = userAccounts;
	}
}
