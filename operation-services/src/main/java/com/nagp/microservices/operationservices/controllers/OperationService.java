package com.nagp.microservices.operationservices.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nagp.microservices.operationservices.model.Account;
import com.nagp.microservices.operationservices.model.Transaction;
import com.nagp.microservices.operationservices.util.Constants;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import io.jsondb.InvalidJsonDbApiUsageException;
import io.jsondb.JsonDBTemplate;

/**
 * Controller for REST APIs related to various operations like money deposit,
 * money withdrawal, money transfer etc.
 * 
 * @author vinodgodara
 *
 */
@RefreshScope
@RestController
public class OperationService {
	private static final Logger LOGGER = LoggerFactory.getLogger(OperationService.class);

	// Actual location on disk for database files, process should have read-write
	// permissions to this folder
	private String dbFilesLocation = "C:\\";

	// Java package name where POJO's are present
	private final String baseScanPackage = "com.nagp.microservices.operationservices.model";

	// JSON DB template for CRUD operations.
	private final JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage, null);

	public OperationService() {
		try {
			jsonDBTemplate.createCollection(Account.class);
		} catch (InvalidJsonDbApiUsageException exc) {
			LOGGER.warn("Collection already exists.");
		}
	}

	/**
	 * Rest API for depositing money in the account.
	 * 
	 * @param accountNO Account number.
	 * @param amount    Amount to be deposited.
	 * @return Success or error message.
	 */
	@PostMapping(path = "/withdrawMoney/{accountNO}/{amount}", consumes = "application/json", produces = "application/json")
	@HystrixCommand(fallbackMethod = "withdrawMoneyFallBackMethod")
	public String withdrawMoney(@PathVariable String accountNO, @PathVariable String amount) {
		LOGGER.error("Entering method: withdrawMoney");

		// Parse account number and amount to long.
		final long accountNumber = Long.parseLong(accountNO);
		final long amountNumber = Long.parseLong(amount);

		// If account number or amount are 0, throw exception.
		if (accountNumber != 0 && amountNumber != 0) {

			// Fetch account from DB.
			final Account account = jsonDBTemplate.findById(accountNumber, Account.class);

			// If account is null or inactive, throw exception.
			if (Objects.nonNull(account) && account.isActive()) {

				// Check if account has enough balance.
				if (account.getBalance() > amountNumber) {
					account.setBalance(account.getBalance() - amountNumber);

					final Transaction transaction = new Transaction();
					transaction.setAmmount(amountNumber);
					transaction.setTransactionType("Debit");

					List<Transaction> transactions = account.getTransactions();

					if (Objects.isNull(transactions)) {
						transactions = new ArrayList<>();
						account.setTransactions(transactions);
					}

					transactions.add(transaction);
				} else {
					LOGGER.error("Account does not have enough balance.");
					throw new RuntimeException("Insufficient Balance.");
				}

				// Update transaction detail and balance in DB.
				jsonDBTemplate.upsert(account);
				return Constants.SUCCESS;
			} else {
				LOGGER.error("Account with provided number is either closed or does not exist.");
				throw new RuntimeException("Account does not exist.");
			}
		} else {
			LOGGER.error("Input account number is invalid.");
			throw new RuntimeException("Invalid account number.");
		}
	}

	/**
	 * API for depositing money in DB.
	 * 
	 * @param accountNO Account number.
	 * @param amount    The amount to be withdrawn.
	 * @return Success or error message.
	 */
	@PostMapping(path = "/depositMoney/{accountNO}/{amount}", consumes = "application/json", produces = "application/json")
	@HystrixCommand(fallbackMethod = "depositMoneyFallBackMethod")
	public String depositMoney(@PathVariable String accountNO, @PathVariable String amount) {
		LOGGER.error("Entering method: depositMoney");

		// Parse account number and amount to long.
		final long accountNumber = Long.parseLong(accountNO);
		final long amountNumber = Long.parseLong(amount);

		// If any of the account number or amount is 0, throw exception.
		if (accountNumber != 0 && amountNumber != 0) {

			// Fetch account from DB.
			final Account account = jsonDBTemplate.findById(accountNumber, Account.class);

			// If account is null or inactive, throw exception.
			if (Objects.nonNull(account) && account.isActive()) {
				account.setBalance(account.getBalance() + amountNumber);

				final Transaction transaction = new Transaction();
				transaction.setAmmount(amountNumber);

				List<Transaction> transactions = account.getTransactions();
				transaction.setTransactionType("Credit");

				if (Objects.isNull(transactions)) {
					transactions = new ArrayList<>();
					account.setTransactions(transactions);
				}

				transactions.add(transaction);

				// Update balance and transaction detail in DB.
				jsonDBTemplate.upsert(account);
				return Constants.SUCCESS;
			} else {
				LOGGER.error("Account either does not exist or is closed.");
				throw new RuntimeException("Account does not exist.");
			}
		} else {
			LOGGER.error("Input account number is invalid.");
			throw new RuntimeException("Invalid account number.");
		}
	}

	/**
	 * API for transferring money from one account to another.
	 * 
	 * @param accountNoFrom Account number from which money is to be transferred.
	 * @param accountNoTo   Account number to which money is to be transferred.
	 * @param amount        The amount to be transferred.
	 * @return
	 */
	@PostMapping(path = "/transferMoney/{accountNoFrom}/{accountNoTo}/{amount}", consumes = "application/json", produces = "application/json")
	@HystrixCommand(fallbackMethod = "transferMoneyFallBackMethod")
	public String transferMoney(@PathVariable String accountNoFrom, @PathVariable String accountNoTo,
			@PathVariable String amount) {
		LOGGER.error("Entering method: transferMoney");

		final long accountFromNumber = Long.parseLong(accountNoFrom);
		final long accountToNumber = Long.parseLong(accountNoTo);
		final long amountNumber = Long.parseLong(amount);

		// If any of the account numbers or amount is 0, throw exception.
		if (accountFromNumber != 0 && accountToNumber != 0 && amountNumber != 0) {

			// Fetch accounts from DB.
			final Account accountFrom = jsonDBTemplate.findById(accountFromNumber, Account.class);
			final Account accountTo = jsonDBTemplate.findById(accountToNumber, Account.class);

			// Check if both accounts are active and user account has enough balance to
			// transfer.
			if (Objects.nonNull(accountFrom) && accountFrom.isActive() && Objects.nonNull(accountTo)
					&& accountTo.isActive() && accountFrom.getBalance() >= amountNumber) {
				accountFrom.setBalance(accountFrom.getBalance() - amountNumber);

				final Transaction transaction = new Transaction();
				transaction.setAmmount(amountNumber);

				List<Transaction> transactions = accountFrom.getTransactions();
				transaction.setTransactionType("Debit");

				if (Objects.isNull(transactions)) {
					transactions = new ArrayList<>();
					accountFrom.setTransactions(transactions);
				}

				transactions.add(transaction);

				accountTo.setBalance(accountTo.getBalance() + amountNumber);

				final Transaction transactionTo = new Transaction();
				transactionTo.setAmmount(amountNumber);
				transactionTo.setTransactionType("Credit");

				List<Transaction> transactionsTo = accountTo.getTransactions();

				if (Objects.isNull(transactionsTo)) {
					transactionsTo = new ArrayList<>();
					accountTo.setTransactions(transactionsTo);
				}

				transactionsTo.add(transactionTo);

				// Update balance and transaction details in DB.
				jsonDBTemplate.upsert(accountFrom);
				jsonDBTemplate.upsert(accountTo);

				return Constants.SUCCESS;
			} else {
				throw new RuntimeException();
			}
		} else {
			LOGGER.error("Input account number is invalid.");
			throw new RuntimeException();
		}
	}

	/**
	 * Fall back method for {@link OperationService#withdrawMoney(String, String)}.
	 * 
	 * @param accountNO Account number.
	 * @param amount    Amount to be withdrawn.
	 * @return Error message.
	 */
	public String withdrawMoneyFallBackMethod(final String accountNO, final String amount) {
		return Constants.ERROR;
	}

	/**
	 * Fall back method for {@link OperationService#depositMoney(String, String)}.
	 * 
	 * @param accountNO Account number.
	 * @param amount    Amount to be deposited.
	 * @return Error message.
	 */
	public String depositMoneyFallBackMethod(final String accountNO, final String amount) {
		return Constants.ERROR;
	}

	/**
	 * Fall back method for
	 * {@link OperationService#transferMoney(String, String, String)}.
	 * 
	 * @param accountNoFrom Account number from which money is to be transferred.
	 * @param accountNoTo   Amount to be withdrawn to which money is to be
	 *                      transferred.
	 * @param amount        The amount to be transferred.
	 * @return Error message.
	 */
	public String transferMoneyFallBackMethod(final String accountNoFrom, final String accountNoTo,
			final String amount) {
		return Constants.ERROR;
	}
}
