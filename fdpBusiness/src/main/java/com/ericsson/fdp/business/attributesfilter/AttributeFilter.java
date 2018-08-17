package com.ericsson.fdp.business.attributesfilter;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.common.enums.TariffEnquiryOption;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This interface to filter the attribute based on expression.
 * 
 * @author Ericsson.
 * 
 */
@Remote
public interface AttributeFilter {

	/**
	 * This method will filter the attribute based on expression configured.
	 * 
	 * @param expression
	 * @return
	 */
	public List<String> filter(List<String> tariffEnquiryValuesForUser, TariffEnquiryOption tariffEnquiryOption,
			FDPRequest fdpRequest) throws ExecutionFailedException;
}
