package com.ericsson.fdp.business.batchjob.mass.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.naming.NamingException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.transaction.TransactionSequenceGeneratorService;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.dao.dto.mass.MassDTO;
import com.ericsson.fdp.dao.entity.FDPMassLoad;

/**
 * This class provisions mass data by sending http 
 * request with fulfillment url and parse the xml 
 * response to generate response data. 
 * 
 * @author evasaty
 *
 */
public class MassProvisionWorker implements Runnable{
	
	private FDPMassLoad fdpMassLoad;
	private Map<String, List<MassDTO>> responseMap = null;
	private FDPRequest fdpRequest=null;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(MassProvisionWorker.class);
	private static JAXBContext jaxbContext=null;
	static{
		try {
			jaxbContext = JAXBContext.newInstance(FulfillmentService.class);
		} catch (JAXBException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public MassProvisionWorker(final FDPMassLoad massLoad, Map<String, List<MassDTO>> responseMap,FDPRequest fdpRequest) {
		this.fdpMassLoad = massLoad;
		this.responseMap = responseMap;
		this.fdpRequest = fdpRequest;
	}
	
	@Override
	public void run() {
		try {
			
			this.postFulfillmentUrl(fdpMassLoad,responseMap,fdpRequest);
		} catch (Exception e) {
			LOGGER.error("Mass provision post request failed ",e);
		}
	}
	
	/**
	 * This method executes provisioning by constructing fulfillment url and sending it as an http request.
	 * 
	 * @param fdpMassLoad
	 * @return
	 * @throws IOException
	 * @throws JAXBException
	 * @throws ExecutionFailedException 
	 * @throws NamingException 
	 */
	private void postFulfillmentUrl(FDPMassLoad fdpMassLoad, Map<String, List<MassDTO>> responseMap,FDPRequest fdpRequest) throws IOException, JAXBException, ExecutionFailedException{
		
		//change for product name mapping by eagarsh
		LOGGER.info("INSIDE  postFulfillmentUrl************");
		String productName = URLEncoder.encode(fdpMassLoad.getProductCode(), "UTF-8");
		FulfillmentService fulfillmentResponse=null;
		String strFileNo =  null;
		List<MassDTO> listMassDTOObj = null;
		MassDTO massDTOObj = null;
		
		String url = (PropertyUtils.getProperty("mass.fulfillment.url"));
		LOGGER.info("mass.fulfillment.url = "+url);
		
		if(null != url){
			url = url.concat("input="+productName+"&MSISDN="+fdpMassLoad.getMsisdn()+"&circlecode="+fdpMassLoad.getCircleId().getCircleCode()+"&username=c39929de831bbe6b494e45dd5eb2926d&password=2cc935d0922c88fcbc5180b573040968&iname="+fdpMassLoad.getIname());
		}
		//url = url.concat("input="+fdpMassLoad.getProductCode()+"&MSISDN="+fdpMassLoad.getMsisdn()+"&circlecode="+fdpMassLoad.getCircleId()+"&username=c39929de831bbe6b494e45dd5eb2926d&password=2cc935d0922c88fcbc5180b573040968&iname="+fdpMassLoad.getIname());
		
		
		if(null!= fdpMassLoad.getProductCost()){
			url = url.concat("&productCost="+fdpMassLoad.getProductCost());
		}
		if(null!= fdpMassLoad.getBeneficiaryMsisdn()){
			url = url.concat("&benMsisdn="+fdpMassLoad.getBeneficiaryMsisdn());
		}
		if(null!= fdpMassLoad.getAction()){
			url = url.concat("&action="+fdpMassLoad.getAction());
		}
		if(null!= fdpMassLoad.getChargingMode()){
			url = url.concat("&paySrc="+fdpMassLoad.getChargingMode());
		}
		if(null!= fdpMassLoad.getSendSMS()){
			url = url.concat("&sendsms="+fdpMassLoad.getSendSMS());
		}
		if(null!= fdpMassLoad.getSkipCharging()){
			url = url.concat("&skipcharging="+ fdpMassLoad.getSkipCharging());
		}
		if(null!= fdpMassLoad.getSplitNo()){
			url = url.concat("&splitno="+ fdpMassLoad.getSplitNo());
		}
//		LOGGER.trace("fulfillment http request url:" + url);
		BufferedReader in=null;
		try{
			URL obj = new URL(url);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setDoOutput(true);
			LOGGER.info("\nSending 'POST' request to URL : " + url);
			LOGGER.trace("Response Code : " + con.getResponseCode());
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			fulfillmentResponse = parseResponseXml(response.toString());
		}catch(IOException e){
			LOGGER.error("Exception for hitting mass url :: "+e.getMessage());
		}finally{
			in.close();
			in.close();
		}
		
				
		massDTOObj = populateMassDto(fdpMassLoad,fulfillmentResponse);
		
		strFileNo = fdpMassLoad.getFileNo();
		
		if(responseMap.containsKey(strFileNo)){
			listMassDTOObj = responseMap.get(strFileNo);
			listMassDTOObj.add(massDTOObj);
		} else{
			listMassDTOObj = new ArrayList<MassDTO>();
			listMassDTOObj.add(massDTOObj);
			responseMap.put(strFileNo, listMassDTOObj);
		}
		LOGGER.info("Executed URL Reposne with STRFIleNo."+strFileNo+"Response size="+responseMap.size());
	}
	
	/**
	 * This method parses the xml response received after product provisioning.
	 * @param file
	 * @return
	 * @throws JAXBException
	 */
	private static FulfillmentService parseResponseXml(String file) throws JAXBException {
		FulfillmentService response = new FulfillmentService();
		 try {    
		        StringReader reader = new StringReader(file);
		        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();  
		        response= (FulfillmentService) jaxbUnmarshaller.unmarshal(reader);  
		        LOGGER.trace("Mass provision post response "+response.getRequestId()+" "+response.getStatus());  
		      } catch (JAXBException e) {  
		    	  LOGGER.error("Mass provision post request failed ",e);
		      }
		 return response;
	}
	
	
	/**
	 * Prepares MassDTO object from fdpMassLoad and fulfillmentResponse.
	 * @param fdpMassLoad
	 * @param fulfillmentResponse
	 * @return
	 */
	private static MassDTO populateMassDto(FDPMassLoad fdpMassLoad, FulfillmentService fulfillmentResponse) {
		
		MassDTO massDTOObj = new MassDTO();
		if(null !=fdpMassLoad){
			
			massDTOObj.setMassId(fdpMassLoad.getMassId().toString());
			massDTOObj.setMsisdn(fdpMassLoad.getMsisdn().toString());
			massDTOObj.setProductCode(fdpMassLoad.getProductCode().toString());
			massDTOObj.setChargingMode(fdpMassLoad.getChargingMode());
			
			if ( fdpMassLoad.getSplitNo() != null) {
				massDTOObj.setSplitNumber(fdpMassLoad.getSplitNo().toString());
			}
			if ( fdpMassLoad.getBeneficiaryMsisdn() != null) {
				massDTOObj.setBeneficiaryMsisdn(fdpMassLoad.getBeneficiaryMsisdn()
						.toString());
			}
			if ( null != fdpMassLoad.getProductCost()) {
				massDTOObj.setProductCost(fdpMassLoad.getProductCost().toString());
			}
			if ( fdpMassLoad.getSkipCharging()) {
				massDTOObj.setSkipCharging("true");
			} else {
				massDTOObj.setSkipCharging("false");
			}
			if ( fdpMassLoad.getAction() != null) {
				massDTOObj.setAction(fdpMassLoad.getAction());
			}
			if (fulfillmentResponse == null) {
				massDTOObj.setStatus(Status.FAILURE
						.getStatusText());
				massDTOObj.setResultCodeExtension("Service Provisioning failed.");
				LOGGER.info("Service Provisioning failed");
			} else {
				massDTOObj.setStatus(fulfillmentResponse.getStatus());
				if (fulfillmentResponse.getStatus()
						.equalsIgnoreCase(
								Status.FAILURE.getStatusText())) {
					massDTOObj.setResultCodeExtension(fulfillmentResponse.getResponseDescription());
					LOGGER.debug("Service Provisioning failed");
				}
			}
			massDTOObj.setFileNo(fdpMassLoad.getFileNo());
			if (fdpMassLoad.getSendSMS()) {
				massDTOObj.setSendSMS("Y");
			} else {
				massDTOObj.setSendSMS("N");
			}
		}
		return massDTOObj;
	}
	
}
