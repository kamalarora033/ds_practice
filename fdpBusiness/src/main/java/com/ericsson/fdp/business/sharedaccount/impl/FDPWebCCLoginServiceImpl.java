package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.sharedaccount.service.FDPWebCCLoginService;
import com.ericsson.fdp.dao.dto.sharedaccount.WebUserProductDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPUserDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPWebUserProductDAO;

@Stateless
public class FDPWebCCLoginServiceImpl implements FDPWebCCLoginService {

	@Inject
	private FDPUserDAO userDAO;

	@Inject
	FDPWebUserProductDAO webUserProductDAO;

	@Override
	public String authenticateUser(final String username, final String password) {
		return userDAO.authenticateuser(username, password);
	}

	@Override
	public void logInWithMSISDN(final Long msisdn) {
		// TODO Auto-generated method stub
	}

	@Override
	public List<WebUserProductDTO> getUserProducts(final Long msisdn) {
		return webUserProductDAO.getUserProducts(msisdn);
	}

	@Override
	public List<WebUserProductDTO> viewHistory(final String ccUsername) {
		return webUserProductDAO.viewHistory(ccUsername);
	}
}