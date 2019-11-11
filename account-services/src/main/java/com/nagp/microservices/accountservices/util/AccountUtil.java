package com.nagp.microservices.accountservices.util;

import java.util.Objects;

import org.apache.commons.lang.StringUtils;

import com.nagp.microservices.accountservices.model.Account;

/**
 * Utility class.
 * 
 * @author vinodgodara
 *
 */
public class AccountUtil {

	/**
	 * Check if account is valid or not.
	 * 
	 * @param account {@link Account} object.
	 * @return If account is valid or invalid.
	 */
	public static boolean isAccountValid(final Account account) {
		return Objects.nonNull(account) && account.getAccountNO() != 0 && StringUtils.isNotBlank(account.getUserID());
	}
}
