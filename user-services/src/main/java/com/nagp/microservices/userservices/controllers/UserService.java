package com.nagp.microservices.userservices.controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.nagp.microservices.userservices.model.User;
import com.nagp.microservices.userservices.util.UserConstants;
import com.nagp.microservices.userservices.util.UserUtil;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;

import io.jsondb.InvalidJsonDbApiUsageException;
import io.jsondb.JsonDBTemplate;

/**
 * Controller for REST APIs related to user operations register new user, get
 * account list for a user, update user info etc.
 * 
 * @author vinodgodara
 *
 */
@RefreshScope
@RestController
public class UserService {

	private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

	// Actual location on disk for database files, process should have read-write
	// permissions to this folder
	private String dbFilesLocation = "D:\\Workshop";

	// Java package name where POJO's are present
	private final String baseScanPackage = "com.nagp.microservices.userservices.model";

	// JSON DB template.
	private final JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, baseScanPackage, null);

	public UserService() {
		try {
			jsonDBTemplate.createCollection(User.class);
		} catch (InvalidJsonDbApiUsageException exc) {
			LOGGER.warn("DB already exists.");
		}
	}

	/**
	 * API to register new customer.
	 * 
	 * @param user The user to be created.
	 * @return Success or error string.
	 */
	@PostMapping(path = "/registerNewCustomer", consumes = "application/json", produces = "application/json")
	@HystrixCommand(fallbackMethod = "registerNewCustomerFallBackMethod")
	public String registerNewCustomer(@RequestBody User user) {
		LOGGER.debug("Entering method: registerNewCustomer");

		// Check if user is valid or not.
		if (UserUtil.isUserValid(user)) {

			// Fetch existing user from DB.
			final User existingUser = jsonDBTemplate.findById(user.getUserID(), User.class);

			// If user already exists, throw exception.
			if (Objects.nonNull(existingUser)) {
				LOGGER.error("User already exists.");
				throw new RuntimeException("User already exists.");
			}

			// Inser user in DB.
			jsonDBTemplate.insert(user);
			return UserConstants.SUCCESS;
		} else {
			LOGGER.error("Input User is invalid.");
			throw new RuntimeException("Invalid user");
		}
	}

	/**
	 * API to update customer information in DB,
	 * 
	 * @param user The user to be updated.
	 * @return Success or error string.
	 */
	@PostMapping(path = "/updateCustomerInfo", consumes = "application/json", produces = "application/json")
	@HystrixCommand(fallbackMethod = "updateCustomerInfoFallBackMethod")
	public String updateCustomerInfo(@RequestBody User user) {
		LOGGER.debug("Entering method: updateCustomerInfo");

		// Check if user object is valid or not.
		if (UserUtil.isUserValid(user)) {

			// Fetch existing user from DB.
			final Object existingUser = jsonDBTemplate.findById(user.getUserID(), User.class);

			// If user with user ID does not exist, throw exception.
			if (Objects.isNull(existingUser)) {
				LOGGER.error("User does not exist.");
				throw new RuntimeException("User does not exist.");
			} else {

				// Update user in DB.
				jsonDBTemplate.upsert(user);
				return UserConstants.SUCCESS;
			}

		} else {
			LOGGER.error("Input User is invalid.");
			throw new RuntimeException("Invalid user");
		}
	}

	/**
	 * API to get the account list for a user.
	 * 
	 * @param userID The suer ID.
	 * @return List of accounts for provided user ID.
	 */
	@GetMapping(path = "/getAccountsList/{userID}")
	@HystrixCommand(fallbackMethod = "getAccountsListFallBackMethod")
	public List<Long> getAccountsList(@PathVariable String userID) {
		LOGGER.debug("Entering method: getAccountsList");

		// Check that user ID is not empty or null.
		if (StringUtils.isNotBlank(userID)) {

			// Fetch user from DB.
			final User user = jsonDBTemplate.findById(userID, User.class);

			// If user with user ID does not exist, throw exception.
			if (Objects.nonNull(user) && Objects.nonNull((user).getUserAccounts())) {
				return user.getUserAccounts();
			} else {
				LOGGER.error("User with input user ID does not exist.");
				throw new RuntimeException("User does not exist.");
			}
		} else {
			LOGGER.error("Input user ID is invalid.");
			throw new RuntimeException("Invalid user ID.");
		}
	}

	/**
	 * API to add account for a user.
	 * 
	 * @param userID    User ID.
	 * @param accountNo The account number to be added.
	 * @return Success or error string.
	 */
	@PostMapping(path = "/addAccount/{userID}/{accountNo}", consumes = "application/json", produces = "application/json")
	@HystrixCommand(fallbackMethod = "addAccountFallBackMethod")
	public String addAccount(@PathVariable String userID, @PathVariable String accountNo) {
		LOGGER.debug("Entering method: addAccount");

		final Long accountNumber = Long.parseLong(accountNo);

		// If either user ID or account number is either empty or null, throw exception.
		if (Objects.nonNull(accountNumber) && accountNumber != 0 && Objects.nonNull(userID)) {

			// Fetch existing user from DB.
			final User existingUser = jsonDBTemplate.findById(userID, User.class);

			// If user with user ID does not exist, throw exception.
			if (Objects.isNull(existingUser)) {
				LOGGER.error("User does not exist.");
				throw new RuntimeException("User does not exist.");
			} else {

				List<Long> accounts = existingUser.getUserAccounts();

				if (Objects.isNull(accounts)) {
					accounts = new ArrayList<>();
					existingUser.setUserAccounts(accounts);
				}

				accounts.add(accountNumber);

				// Update user in DB.
				jsonDBTemplate.upsert(existingUser);
				return UserConstants.SUCCESS;
			}

		} else {
			LOGGER.error("Input User is invalid.");
			throw new RuntimeException("Invalid user");
		}
	}

	/**
	 * API to remove account for a user.
	 * 
	 * @param userID    User ID.
	 * @param accountNo The account number to be added.
	 * @return Success or error string.
	 */
	@PostMapping(path = "/removeAccount/{userID}/{accountNo}", consumes = "application/json", produces = "application/json")
	@HystrixCommand(fallbackMethod = "removeAccountFallBackMethod")
	public String removeAccount(@PathVariable String userID, @PathVariable String accountNo) {
		LOGGER.debug("Entering method: removeAccount");

		final Long accountNumber = Long.parseLong(accountNo);

		// If either user ID or account number is either empty or null, throw exception.
		if (Objects.nonNull(accountNumber) && accountNumber != 0 && Objects.nonNull(userID)) {

			// Fetch existing user from DB.
			final User existingUser = jsonDBTemplate.findById(userID, User.class);

			// If user with user ID does not exist, throw exception.
			if (Objects.isNull(existingUser)) {
				LOGGER.error("User does not exist.");
				throw new RuntimeException("User does not exist.");
			} else {

				List<Long> accounts = existingUser.getUserAccounts();

				if (Objects.isNull(accounts)) {
					accounts = new ArrayList<>();
					existingUser.setUserAccounts(accounts);
				}

				if (accounts.contains(accountNumber)) {
					accounts.remove(accountNumber);
				}

				// Update user in DB.
				jsonDBTemplate.upsert(existingUser);
				return UserConstants.SUCCESS;
			}

		} else {
			LOGGER.error("Input User is invalid.");
			throw new RuntimeException("Invalid user");
		}
	}

	/**
	 * Fallback method for {@link UserService#registerNewCustomer(User)}.
	 * 
	 * @param user The user object.
	 * @return Error message.
	 */
	public String registerNewCustomerFallBackMethod(final User user) {
		return UserConstants.USER_ID_ALREADY_EXISTS + " OR " + UserConstants.REQUIRED_FIELDS_NULL;
	}

	/**
	 * Fallback method for {@link UserService#updateCustomerInfo(User)}.
	 * 
	 * @param user The user object.
	 * @return Error message.
	 */
	public String updateCustomerInfoFallBackMethod(final User user) {
		return UserConstants.USER_ID_DOES_NOT_EXIST + " OR " + UserConstants.REQUIRED_FIELDS_NULL;
	}

	/**
	 * Fallback method for {@link UserService#getAccountsList(String)}.
	 * 
	 * @param userID User ID.
	 * @return List of transactions for provided e=user ID.
	 */
	public List<Long> getAccountsListFallBackMethod(final String userID) {
		final List<Long> errors = new ArrayList<>();
		return errors;
	}

	/**
	 * Fallback method for {@link UserService#addAccount(String, String)}.
	 * 
	 * @param userID    User ID.
	 * @param accountNo Account number.
	 * @return Error message.
	 */
	public String addAccountFallBackMethod(String userID, String accountNo) {
		return UserConstants.USER_ID_DOES_NOT_EXIST;
	}

	/**
	 * Fallback method for {@link UserService#removeAccount(String, String)}.
	 * 
	 * @param userID    User ID.
	 * @param accountNo Account number.
	 * @return Error message.
	 */
	public String removeAccountFallBackMethod(String userID, String accountNo) {
		return UserConstants.USER_ID_DOES_NOT_EXIST;
	}
}
