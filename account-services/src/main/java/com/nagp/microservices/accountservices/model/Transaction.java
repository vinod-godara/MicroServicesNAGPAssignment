package com.nagp.microservices.accountservices.model;

import org.springframework.stereotype.Component;

@Component
public class Transaction {

	private long ammount;

	private String transactionType;

	public long getAmmount() {
		return ammount;
	}

	public void setAmmount(long ammount) {
		this.ammount = ammount;
	}

	public String getTransactionType() {
		return transactionType;
	}

	public void setTransactionType(String transactionType) {
		this.transactionType = transactionType;
	}

}
