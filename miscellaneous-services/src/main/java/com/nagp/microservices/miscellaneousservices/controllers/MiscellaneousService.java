package com.nagp.microservices.miscellaneousservices.controllers;

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nagp.microservices.miscellaneousservices.model.Account;
import com.nagp.microservices.miscellaneousservices.util.UserServiceConstants;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import io.jsondb.InvalidJsonDbApiUsageException;
import io.jsondb.JsonDBTemplate;

/**
 * Controller for REST APIs related to issuing and blocking checque books.
 * 
 * @author vinodgodara
 *
 */
@RefreshScope
@RestController
public class MiscellaneousService {
	private final static Logger LOGGER = LoggerFactory.getLogger(MiscellaneousService.class);

	// Actual location on disk for database files, process should have read-write
	// permissions to this folder
	private String dbFilesLocation = "D:\\Workshop";

	// Java package name where POJO's are present
	private final String baseScanPackage = "com.nagp.microservices.miscellaneousservices.model";

	private final JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage, null);

	public MiscellaneousService() {
		try {
			jsonDBTemplate.createCollection(Account.class);
		} catch (InvalidJsonDbApiUsageException exc) {
			LOGGER.warn("Collection already exists.");
		}
	}

	/**
	 * REST API for ordering a checkbook.
	 * 
	 * @param accountNO The account number
	 * @return Success or error message.
	 */
	@PostMapping(path = "/orderCheckBook/{accountNO}", consumes = "application/json", produces = "application/json")
	@HystrixCommand(fallbackMethod = "orderCheckBookFallBackMethod")
	public String orderCheckBook(@PathVariable String accountNO) {
		LOGGER.debug("Ëntering method: orderCheckBook");

		// Parse the account number to long.
		final long accountNumber = Long.parseLong(accountNO);

		// If account number is 0, throw exception.
		if (accountNumber != 0) {

			// Fetch existing account from DB.
			final Account account = jsonDBTemplate.findById(accountNumber, Account.class);

			if (Objects.nonNull(account) && account.isActive()) {
				// Update DB.
				account.setInChecqueBookIssued(Boolean.TRUE);
				jsonDBTemplate.upsert(account);
				return UserServiceConstants.SUCCESS;
			} else {
				LOGGER.error("Account with provided account number does not exist.");
				throw new RuntimeException("Account does not exist.");
			}
		} else {
			LOGGER.error("Input account number is invalid.");
			throw new RuntimeException("Invalid account number.");
		}
	}

	/**
	 * REST API for blocking check book.
	 * 
	 * @param accountNO The account number.
	 * @return Success or error message.
	 */
	@PostMapping(path = "/blockCheckBook/{accountNO}", consumes = "application/json", produces = "application/json")
	@HystrixCommand(fallbackMethod = "blockCheckBookFallBackMethod")
	public String blockCheckBook(@PathVariable String accountNO) {
		LOGGER.error("Entering method: blockCheckBook.");

		// Parse account number to long.
		final long accountNumber = Long.parseLong(accountNO);

		// If account number is 0, throw exception.
		if (accountNumber != 0) {

			// Fetch account number from DB.
			final Account account = jsonDBTemplate.findById(accountNumber, Account.class);

			if (Objects.nonNull(account) && account.isActive()) {

				// Update in DB.
				account.setInChecqueBookIssued(Boolean.FALSE);
				jsonDBTemplate.upsert(account);
				return UserServiceConstants.SUCCESS;
			} else {
				LOGGER.error("Account is either closed or does not exist.");
				throw new RuntimeException("Account does not exist.");
			}
		} else {
			LOGGER.error("Input account number is invalid.");
			throw new RuntimeException("Invalid account number.");
		}
	}

	/**
	 * Fallback method for {@link MiscellaneousService#orderCheckBook(String)}.
	 * 
	 * @param accountNO The account number.
	 * @return Error message.
	 */
	public String orderCheckBookFallBackMethod(final String accountNO) {
		return UserServiceConstants.ERROR;
	}

	/**
	 * Fallback method for {@link MiscellaneousService#blockCheckBook(String)}.
	 * 
	 * @param accountNO The account number.
	 * @return Error message.
	 */
	public String blockCheckBookFallBackMethod(final String accountNO) {
		return UserServiceConstants.ERROR;
	}
}
