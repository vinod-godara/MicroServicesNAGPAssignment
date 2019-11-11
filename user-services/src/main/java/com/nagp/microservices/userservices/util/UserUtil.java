package com.nagp.microservices.userservices.util;

import org.apache.commons.lang.StringUtils;

import com.nagp.microservices.userservices.model.User;

/**
 * Utility class.
 * 
 * @author vinodgodara
 *
 */
public class UserUtil {

	/**
	 * Verify if user object is valid.
	 * 
	 * @param user The user object to be verified.
	 * @return If user object os valid or not.
	 */
	public static boolean isUserValid(final User user) {
		return StringUtils.isNotBlank(user.getUserID()) && StringUtils.isNotBlank(user.getUserAddress())
				&& StringUtils.isNotBlank(user.getUserEmail());
	}
}
