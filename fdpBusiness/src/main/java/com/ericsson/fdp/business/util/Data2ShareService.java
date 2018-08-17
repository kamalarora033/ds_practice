package com.ericsson.fdp.business.util;

import java.util.List;

import javax.ejb.Remote;

import com.ericsson.fdp.business.dto.Me2uProductDTO;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.request.FDPRequest;

@Remote
public interface Data2ShareService {
	
	public List<Me2uProductDTO> getData2ShareProducts(FDPCommand fdpCommandGBAD, FDPRequest fdpRequest);

	public Integer getOfferIdOnProductId(String productId, String data2ShareOfferId);
}
