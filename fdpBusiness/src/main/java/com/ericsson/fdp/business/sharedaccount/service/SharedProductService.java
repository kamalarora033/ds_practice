//TODO Move this file and it's implementation to FDPWEBServices
package com.ericsson.fdp.business.sharedaccount.service;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.dao.dto.product.ProductDTO;

/**
 * Interface SharedProductService
 * 
 * @author Ericsson
 */
@Remote
public interface SharedProductService {

	/**
	 * Gets the filtered products.
	 * 
	 * @param userMsisdn
	 *            the user msisdn
	 * @param productType
	 *            the product type
	 * @return the filtered products
	 */
	List<ProductDTO> getFilteredProducts(Long userMsisdn, String productType);
}
