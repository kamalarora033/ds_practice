package com.ericsson.fdp.business.sharedaccount.service;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.dao.dto.sharedaccount.WebUserProductDTO;

/**
 * The Interface FDPWebCCLoginService.
 */
@Remote
public interface FDPWebCCLoginService {

	/**
	 * Authenticate customer care user with given username and password.
	 *
	 * @param username the username
	 * @param password the password
	 * @return the String
	 */
	public String authenticateUser(String username, String password);

	/**
	 * Log in with msisdn on behalf of user.
	 *
	 * @param msisdn the msisdn
	 */
	public void logInWithMSISDN(Long msisdn);

	/**
	 * Gets the user products.
	 *
	 * @param msisdn the msisdn
	 * @return the user products
	 */
	List<WebUserProductDTO> getUserProducts(Long msisdn);

	/**
	 * View history.
	 *
	 * @param ccUsername the cc username
	 * @return the list
	 */
	List<WebUserProductDTO> viewHistory(String ccUsername);
}
