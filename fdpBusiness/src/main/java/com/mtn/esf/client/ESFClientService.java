package com.mtn.esf.client;

import java.util.Map;

/**
 * 
 * @author edixche
 *
 */
public interface ESFClientService {

	/**
	 * This methods calls the ESF database web service call
	 * 
	 * @param attributes
	 * @return
	 */
	public boolean updateESF(Map<String,String> attributes,String circleCode);
}
