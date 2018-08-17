package com.ericsson.fdp.business.dto;

import java.util.List;

import com.ericsson.fdp.FDPCacheable;

public class ListMe2uDTO implements FDPCacheable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 53453455345L;
	private List<Me2uProductDTO> me2uList;
	
	public List<Me2uProductDTO> getMe2uList() {
		return me2uList;
	}
	public void setMe2uList(List<Me2uProductDTO> me2uList) {
		this.me2uList = me2uList;
	}

	

}
