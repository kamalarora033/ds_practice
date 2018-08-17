//TODO Move this file and it's interface to FDPWEBServices
package com.ericsson.fdp.business.sharedaccount.service.impl;

import java.util.Collections;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.sharedaccount.service.DropdownService;
import com.ericsson.fdp.dao.dto.FDPDropdownDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPDropdownDAO;

@Stateless
public class DropdownServiceImpl implements DropdownService {

	@Inject
	private FDPDropdownDAO fdpDropdownDAO;

	@Override
	public FDPDropdownDTO getDropDownForProductType() {
		FDPDropdownDTO dropdownDTO = fdpDropdownDAO.getDropDownListByName("PRODUCT_TYPE");
		Collections.sort(dropdownDTO.getFdpDropdownValueList());
		return dropdownDTO;
	}
}
