package com.ericsson.fdp.business.batchjob.productconfigurationsexport;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.common.dto.ProductExcelDTO;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;

@Remote
public interface ProductConfigurationsExportService {

	/**
	 * Gets the product excel dt os.
	 *
	 * @param fdpCircle the fdp circle
	 * @return the product excel dt os
	 * @throws FDPServiceException
	 */
	List<ProductExcelDTO> getProductExcelDTOs(final FDPCircle fdpCircle) throws FDPServiceException;
}
