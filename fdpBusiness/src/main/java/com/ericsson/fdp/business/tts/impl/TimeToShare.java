package com.ericsson.fdp.business.tts.impl;

import java.io.FileNotFoundException;

import com.ericsson.fdp.business.exception.RuleException;
import com.ericsson.fdp.business.fnf.Offer;
import com.ericsson.fdp.business.tts.TimeToShareOffer;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.dto.sharebaseoffer.FDPFnFOfferDetailsDTO;
import com.ericsson.fdp.dao.dto.sharebaseoffer.ShareBaseOfferDTO;

public class TimeToShare implements TimeToShareOffer{
	private static final long serialVersionUID = 1L;
	
	private ShareBaseOfferDTO shareBaseOfferDTO;
	
	public TimeToShare(ShareBaseOfferDTO shareBaseOfferDTO) {
		super();
		this.shareBaseOfferDTO = shareBaseOfferDTO;
	}


	@Override
	public FDPResponse execute(FDPRequest fdpRequest) throws RuleException,
			FileNotFoundException {
		return null;
	}


	/**
	 * @return the shareBaseOfferDTO
	 */
	public ShareBaseOfferDTO getShareBaseOfferDTO() {
		return shareBaseOfferDTO;
	}


	/**
	 * @param shareBaseOfferDTO the shareBaseOfferDTO to set
	 */
	public void setShareBaseOfferDTO(ShareBaseOfferDTO shareBaseOfferDTO) {
		this.shareBaseOfferDTO = shareBaseOfferDTO;
	}


	
	
	
}
