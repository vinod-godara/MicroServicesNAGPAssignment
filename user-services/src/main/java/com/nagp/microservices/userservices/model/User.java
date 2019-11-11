package com.nagp.microservices.userservices.model;

import java.util.List;

import org.springframework.stereotype.Component;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

@Document(collection = "users", schemaVersion = "1.0")
@Component
public class User {

	@Id
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
