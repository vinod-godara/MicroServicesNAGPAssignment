package com.nagp.microservices.accountservices.controllers;

import java.util.List;
import java.util.Objects;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nagp.microservices.accountservices.model.Account;
import com.nagp.microservices.accountservices.model.Transaction;
import com.nagp.microservices.accountservices.proxies.UserServiceProxy;
import com.nagp.microservices.accountservices.util.AccountConstants;
import com.nagp.microservices.accountservices.util.AccountUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import io.jsondb.InvalidJsonDbApiUsageException;
import io.jsondb.JsonDBTemplate;

/**
 * Controller for REST APIs related to account services.
 * 
 * @author vinodgodara
 *
 */
@RefreshScope
@RestController
public class AccountService {
	private final static Logger LOGGER = LoggerFactory.getLogger(AccountService.class);

	// Actual location on disk for database files, process should have read-write
	// permissions to this folder
	private String dbFilesLocation = "C:\\";

	// Java package name where POJO's are present
	private final String baseScanPackage = "com.nagp.microservices.accountservices.model";

	// JSON DB template used for CRUD operations.
	private final JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage, null);

	@Autowired
	private UserServiceProxy userService;

	public AccountService() {
		try {
			jsonDBTemplate.createCollection(Account.class);
		} catch (InvalidJsonDbApiUsageException exc) {
			LOGGER.warn("DB already exists.");
		}
	}

	/**
	 * API to create new account.
	 * 
	 * @param account The account object to be created.
	 * @return Either success string or error string.
	 */
	@PostMapping(path = "/createNewAccount", consumes = "application/json", produces = "application/json")
//	@HystrixCommand(fallbackMethod = "createNewAccountFallBackMethod")
	public String createNewAccount(@RequestBody Account account) {
		LOGGER.debug("Entering method: createNewAccount");

		// Check is all fields if account are valid.
		if (AccountUtil.isAccountValid(account)) {

			// Check if account by the same ID already exists.
			final Account existingAccount = jsonDBTemplate.findById(account.getAccountNO(), Account.class);

			// If user already exists, throw exception.
			if (Objects.nonNull(existingAccount)) {
				LOGGER.error("Account already exists.");
				throw new RuntimeException("Account already exosts.");
			}

			// Update account list for user.
			final String response = userService.addAccount(account.getUserID(), Long.toString(account.getAccountNO()));

			if (Objects.nonNull(response) && response.equals(AccountConstants.SUCCESS)) {
				// If account does not exist, insert.
				jsonDBTemplate.insert(account);
			} else {
				LOGGER.error("Error while updating account list of user.");
				throw new RuntimeException("User account list could not be updated.");
			}

			return AccountConstants.SUCCESS;
		} else {
			LOGGER.error("Input Account is invalid.");
			throw new RuntimeException("Invalid account");
		}
	}

	/**
	 * API to update account information.
	 * 
	 * @param account Account object containing the information to be updated.
	 * @return Success string or error string.
	 */
	@PostMapping(path = "/updateAccountInfo", consumes = "application/json", produces = "application/json")
	@HystrixCommand(fallbackMethod = "updateAccountInfoFallBackMethod")
	public String updateAccountInfo(@RequestBody Account account) {
		LOGGER.debug("Ëntering method: updateAccountInfo");

		// Check if all fields of account are valid.
		if (AccountUtil.isAccountValid(account)) {

			// Fetch the existing account from DB.
			final Account existingAccount = jsonDBTemplate.findById(account.getAccountNO(), Account.class);

			// If user with user ID does not exist, throw exception.
			if (Objects.isNull(existingAccount) || !existingAccount.isActive()) {
				LOGGER.error("Account does not exist.");
				throw new RuntimeException("Account does not exists.");
			} else {
				// Update account in DB.
				jsonDBTemplate.upsert(account);
				return AccountConstants.SUCCESS;
			}
		} else {
			LOGGER.error("Input Account is invalid.");
			throw new RuntimeException("Invalid account");
		}
	}

	/**
	 * API for closing the coount.
	 * 
	 * @param accountNO Account number for the account to be closed.
	 * @return Success or error string.
	 */
	@PostMapping(path = "/closeAccount/{accountNO}", consumes = "application/json", produces = "application/json")
	@HystrixCommand(fallbackMethod = "closeAccountFallBackMethod")
	public String closeAccount(@PathVariable String accountNO) {
		LOGGER.info("Entering method: closeAccount");

		// Parse account number to long.
		final long accountNumber = Long.parseLong(accountNO);

		// If account number is 0, throw exception.
		if (accountNumber != 0) {

			// Fetch account detail from DB.
			final Account account = jsonDBTemplate.findById(accountNumber, Account.class);

			if (Objects.nonNull(account) && account.isActive()) {

				// Update account list for user.
				final String response = userService.removeAccount(account.getUserID(),
						Long.toString(account.getAccountNO()));

				if (Objects.nonNull(response) && response.equals(AccountConstants.SUCCESS)) {
					// Set isActive as false for the account and update in the DB.
					account.setActive(Boolean.FALSE);
					jsonDBTemplate.upsert(account);
				} else {
					LOGGER.error("Error while updating account list of user.");
					throw new RuntimeException("User account list could not be updated.");
				}

				return AccountConstants.SUCCESS;
			} else {
				LOGGER.error("Account is either inactive or does not exist.");
				throw new RuntimeException("Acount does not exist.");
			}
		} else {
			LOGGER.error("Input account number is invalid.");
			throw new RuntimeException("Invalid account number.");
		}
	}

	/**
	 * API to fetch transaction summary for the provided account number.
	 * 
	 * @param accountNO The account number for the account for which transaction
	 *                  summary is required.
	 * @return List of the transactions.
	 */
	@GetMapping(path = "/getTransactionSummary/{accountNO}")
	@HystrixCommand(fallbackMethod = "getTransactionSummaryFallBackMethod")
	public List<Transaction> getTransactionSummary(@PathVariable String accountNO) {
		LOGGER.info("Entering method: getTransactionSummary");

		// Parse the account number to long.
		final long accountNumber = Long.parseLong(accountNO);

		// Id account number is 0, throw exception.
		if (accountNumber != 0) {

			// Fetch account from the DB.
			final Account account = jsonDBTemplate.findById(accountNumber, Account.class);

			if (Objects.nonNull(account) && account.isActive()
					&& CollectionUtils.isNotEmpty(account.getTransactions())) {
				return account.getTransactions();
			} else {
				LOGGER.error("No transaction details for the account.");
				throw new RuntimeException("Transaction details not available.");
			}
		} else {
			LOGGER.error("Input account number is invalid.");
			throw new RuntimeException("Invalid account number.");
		}
	}

	/**
	 * Fall back method for {@link AccountService#createNewAccount(Account)}.
	 * 
	 * @param account The {@link Account} object.
	 * @return Error string.
	 */
	public String createNewAccountFallBackMethod(final Account account) {
		return AccountConstants.ERROR;
	}

	/**
	 * Fall back method for {@link AccountService#updateAccountInfo(Account)}.
	 * 
	 * @param account The {@link Account} object.
	 * @return Error string.
	 */
	public String updateAccountInfoFallBackMethod(final Account account) {
		return AccountConstants.ERROR;
	}

	/**
	 * Fall back method for {@link AccountService#closeAccount(String)}.
	 * 
	 * @param accountNO The account number.
	 * @return Error string.
	 */
	public String closeAccountFallBackMethod(final String accountNO) {
		return AccountConstants.ERROR;
	}

	/**
	 * Fall back method for {@link AccountService#getTransactionSummary(String)}.
	 * 
	 * @param accountNO The account number.
	 * @return Error string.
	 */
	public List<Transaction> getTransactionSummaryFallBackMethod(final String accountNO) {
		return null;
	}
}
