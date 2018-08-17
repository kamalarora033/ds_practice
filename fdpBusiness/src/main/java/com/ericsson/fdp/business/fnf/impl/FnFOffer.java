package com.ericsson.fdp.business.fnf.impl;

import java.io.FileNotFoundException;

import com.ericsson.fdp.business.exception.RuleException;
import com.ericsson.fdp.business.fnf.Offer;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.dto.sharebaseoffer.FDPFnFOfferDetailsDTO;

public class FnFOffer implements Offer{
	private static final long serialVersionUID = 1L;
	
	private FDPFnFOfferDetailsDTO fnFOfferDTO;
	
	public FnFOffer(FDPFnFOfferDetailsDTO fnFOfferDTO) {
		super();
		this.fnFOfferDTO = fnFOfferDTO;
	}


	@Override
	public FDPResponse execute(FDPRequest fdpRequest) throws RuleException,
			FileNotFoundException {
		return null;
	}


	/**
	 * @return the fnFOfferDTO
	 */
	public FDPFnFOfferDetailsDTO getFnFOfferDTO() {
		return fnFOfferDTO;
	}


	/**
	 * @param fnFOfferDTO the fnFOfferDTO to set
	 */
	public void setFnFOfferDTO(FDPFnFOfferDetailsDTO fnFOfferDTO) {
		this.fnFOfferDTO = fnFOfferDTO;
	}

	@Override
	public String toString() {
		return "FnFOffer [fnFOfferDTO=" + fnFOfferDTO + "]";
	}
	
	
}
