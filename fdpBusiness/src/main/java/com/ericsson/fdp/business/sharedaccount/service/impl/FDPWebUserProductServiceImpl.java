package com.ericsson.fdp.business.sharedaccount.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.sharedaccount.service.FDPWebUserProductService;
import com.ericsson.fdp.dao.dto.sharedaccount.WebUserProductDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPWebUserProductDAO;

@Stateless(mappedName = "webUserProductService")
public class FDPWebUserProductServiceImpl implements FDPWebUserProductService {

	@Inject
	FDPWebUserProductDAO webUserProductDAO;

	@Override
	public List<WebUserProductDTO> getUserProducts(Long msisdn) {
		return webUserProductDAO.getUserProducts(msisdn);
	}


}
