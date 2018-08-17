//TODO Move this file and it's interface to FDPWEBServices
package com.ericsson.fdp.business.sharedaccount.service.impl;

import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.sharedaccount.service.SharedProductService;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.enums.ProductType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.dao.dto.product.ProductDTO;
import com.ericsson.fdp.dao.dto.product.ProductGeneralInfoDTO;
import com.ericsson.fdp.dao.enums.StatusCodeEnum;
import com.ericsson.fdp.dao.fdpadmin.FDPProductDAO;

@Stateless
public class SharedProductServiceImpl implements SharedProductService {

	/** The Constant LOGGER. */
	private final static Logger LOGGER = LoggerFactory.getLogger(SharedProductServiceImpl.class);

	/** The fdp product dao. */
	@Inject
	private FDPProductDAO fdpProductDAO;

	@Override
	public List<ProductDTO> getFilteredProducts(final Long userMsisdn,
			final String productType) {
		List<ProductDTO> result = null;
		FDPCircle circle;
		try {
			circle = RequestUtil.getFDPCircleFromMsisdn(userMsisdn.toString());
			final ProductDTO productDTO = new ProductDTO();
			final ProductGeneralInfoDTO productGeneralInfoDTO = new ProductGeneralInfoDTO();

			if (ProductType.getProductType(productType) != null) {
				productGeneralInfoDTO.setProductType(productType);
			}
			//productGeneralInfoDTO.setProductCategory(ProductCategoryEnum.SHARED.getKey());
			productGeneralInfoDTO.setProductStatus(StatusCodeEnum.ACTIVE.getStatus());
			productGeneralInfoDTO.setFdpCircleId(circle.getCircleId());

			productDTO.setProductInfoDTO(productGeneralInfoDTO);
			result = fdpProductDAO.getFilteredProducts(productDTO);
		} catch (final NumberFormatException e) {
			LOGGER.error("Exception Occured.", e);
		} catch (final NamingException e) {
			LOGGER.error("Exception Occured.", e);
		}
		return result;
	}
}
