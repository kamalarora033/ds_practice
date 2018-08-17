//TODO Move this file and it's implementation to FDPWEBServices
package com.ericsson.fdp.business.sharedaccount.service;

import javax.ejb.Remote;

import com.ericsson.fdp.dao.dto.FDPDropdownDTO;

/**
 * Interface DropdownService
 * 
 * @author Ericsson
 */
@Remote
public interface DropdownService {
	
	/**
	 * Gets the drop down for product type.
	 *
	 * @return the drop down for product type
	 */
	public FDPDropdownDTO getDropDownForProductType();
}
