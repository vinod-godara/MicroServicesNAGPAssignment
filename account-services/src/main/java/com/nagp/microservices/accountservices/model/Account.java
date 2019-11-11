package com.nagp.microservices.accountservices.model;

import java.util.List;

import org.springframework.stereotype.Component;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

@Document(collection = "accounts", schemaVersion = "1.0")
@Component
public class Account {

	private String userID;

	@Id
	private long accountNO;

	private String branch;

	private boolean isActive;

	private Long balance;

	private boolean isChecqueBookIssued;

	private List<Transaction> transactions;

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public long getAccountNO() {
		return accountNO;
	}

	public void setAccountNO(long accountNO) {
		this.accountNO = accountNO;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public Long getBalance() {
		return balance;
	}

	public void setBalance(Long balance) {
		this.balance = balance;
	}

	public boolean isInChecqueBookIssued() {
		return isChecqueBookIssued;
	}

	public void setInChecqueBookIssued(boolean inChecqueBookIssued) {
		this.isChecqueBookIssued = inChecqueBookIssued;
	}

	public List<Transaction> getTransactions() {
		return transactions;
	}

	public void setTransactions(List<Transaction> transactions) {
		this.transactions = transactions;
	}

}
