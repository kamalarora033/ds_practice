package com.mtn.esf.client;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.dao.dto.FDPAIRConfigDTO;
import com.ericsson.fdp.dao.fdpadmin.FDPAIRConfigDAOImpl;


public class ESFClientServiceImpl implements Serializable {
	//private static final String esfURL = PropertyUtils.getProperty("esf.URL");

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(ESFClientServiceImpl.class);
	
	private static int abilityServerTimeOut = 300;
	
	/**
	 * 
	 * @param attributes
	 * @param circleCode
	 * @param url
	 * @return
	 */
	public boolean updateAbility(Map<String, String> attributes,String circleCode,String url) {
		boolean callSuccessful = false;
		try {
			callSuccessful = callESFService(attributes,circleCode,url);
		} catch (Exception e) {
			LOGGER.debug("Error in connection with ESF server"+e);
			callSuccessful = false;
		}
		return callSuccessful;
	}

	/**
	 * 
	 * @param attributes
	 * @param circleCode
	 * @param esfURL
	 * @return
	 * @throws Exception
	 */
	private boolean callESFService(Map<String, String> attributes,String circleCode,String esfURL)
			throws Exception {

		boolean callSuccessful = false;
		try {
			for(String key : attributes.keySet()) {
				if(esfURL.endsWith("abilityUpdate"))
					esfURL= esfURL +FDPConstant.QUESTIONMARK;
				else		
					esfURL= esfURL +FDPConstant.AMPERSAND;
					esfURL=esfURL+key+FDPConstant.EQUALSTO+attributes.get(key);
			}
			URL url=new URL(esfURL);
			HttpURLConnection ht = (HttpURLConnection) url.openConnection();
			ht.setConnectTimeout(abilityServerTimeOut);
			int responseCode = ht.getResponseCode();
			if (responseCode == 200) {
				callSuccessful = true;
			}
			System.out.println("esfURL formed is :: :: "+esfURL);
		
		} catch (Exception e) {
			LOGGER.debug("Error in connection with Ability server",e);
		}

		return callSuccessful;
	}
	
	private String getURLForESF(String circleCode) {
		
		FDPAIRConfigDAOImpl fdpESFConfigDAOimpl =new FDPAIRConfigDAOImpl();
		List<FDPAIRConfigDTO> fdpESFConfigDTOList = fdpESFConfigDAOimpl.getAIREndpointByCircleCode(circleCode);
		FDPAIRConfigDTO fdpESFConfigDTO=null;
		if(fdpESFConfigDTOList.size()==1){
			fdpESFConfigDTO = (FDPAIRConfigDTO) fdpESFConfigDTOList.get(0);
		}
		final String ESFUrl = BusinessConstants.HTTP_COMPONENT_TYPE + fdpESFConfigDTO.getIpAddress().getValue()
				+ BusinessConstants.COLON + fdpESFConfigDTO.getPort() +"/" +fdpESFConfigDTO.getContextPath();
		
		LOGGER.debug("ESF Url:" + ESFUrl);
		
		return ESFUrl;
	}

}

