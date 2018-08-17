package com.ericsson.fdp.business.dto;

import java.util.List;

import com.ericsson.fdp.FDPCacheable;

public class ListProductDTO implements FDPCacheable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String serviceClass;

	private List<ProductDTO> productDTOList;

	public List<ProductDTO> getProductDTOList() {
		return productDTOList;
	}

	public void setProductDTOList(List<ProductDTO> productDTOList) {
		this.productDTOList = productDTOList;
	}

	public String getServiceClass() {
		return serviceClass;
	}

	public void setServiceClass(String serviceClass) {
		this.serviceClass = serviceClass;
	}
}
