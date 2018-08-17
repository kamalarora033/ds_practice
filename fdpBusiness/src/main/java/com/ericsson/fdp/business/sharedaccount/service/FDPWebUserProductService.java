package com.ericsson.fdp.business.sharedaccount.service;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.dao.dto.sharedaccount.WebUserProductDTO;

/**
 * The Interface FDPWebUserProductService.
 */
@Remote
public interface FDPWebUserProductService {

	List<WebUserProductDTO> getUserProducts(Long msisdn);
}
