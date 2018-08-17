package com.ericsson.fdp.business.request.impl;

import java.util.HashMap;
import java.util.Map;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.ivr.FulfillmentActionTypes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentParameters;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseTypes;
import com.ericsson.fdp.business.enums.ivr.commandservice.IVRCommandEnum;
import com.ericsson.fdp.core.request.impl.FDPSMPPRequestImpl;

/**
 * The Class FullfillmentRequestImpl used create request object from
 * Fullfillment.
 */
public class FulfillmentRequestImpl extends FDPSMPPRequestImpl {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 5460842759820371115L;

	/** The Action Type **/
	private FulfillmentActionTypes actionTypes;

	/** The Response Type **/
	private FulfillmentResponseTypes responseTypes;
	
	/** The IVR Command Enum Type **/
	private IVRCommandEnum ivrCommandEnum;
	
	/** The Action **/
	private String requestAction;
	
	/** The consumerMsisdn **/
	private String consumerMsisdn;
	
	/** The Consumern Name **/
	private String consumerName;
	
	/** The autoApprove **/
	private Boolean autoApprove = true;
	
	/** The dbId **/
	private Integer dbId;
	
	/** The providerMsisdn **/
	private String providerMsisdn;
	
	private Map<FulfillmentParameters,String> commandInputParams;
	
	private String iname;
	
	private String transaction_id;

	/**
	 * @return the actionTypes
	 */
	public FulfillmentActionTypes getActionTypes() {
		return actionTypes;
	}

	/**
	 * @param actionTypes
	 *            the actionTypes to set
	 */
	public void setActionTypes(FulfillmentActionTypes actionTypes) {
		this.actionTypes = actionTypes;
	}

	/**
	 * @return the responseTypes
	 */
	public FulfillmentResponseTypes getResponseTypes() {
		return responseTypes;
	}

	/**
	 * @param responseTypes
	 *            the responseTypes to set
	 */
	public void setResponseTypes(FulfillmentResponseTypes responseTypes) {
		this.responseTypes = responseTypes;
	}

	/**
	 * @return the ivrCommandEnum
	 */
	public IVRCommandEnum getIvrCommandEnum() {
		return ivrCommandEnum;
	}

	/**
	 * @param ivrCommandEnum the ivrCommandEnum to set
	 */
	public void setIvrCommandEnum(IVRCommandEnum ivrCommandEnum) {
		this.ivrCommandEnum = ivrCommandEnum;
	}

	/**
	 * @return the requestAction
	 */
	public String getRequestAction() {
		return requestAction;
	}

	/**
	 * @param requestAction the requestAction to set
	 */
	public void setRequestAction(String requestAction) {
		this.requestAction = requestAction;
	}

	/**
	 * @return the consumerMsisdn
	 */
	public String getConsumerMsisdn() {
		return consumerMsisdn;
	}

	/**
	 * @param consumerMsisdn the consumerMsisdn to set
	 */
	public void setConsumerMsisdn(String consumerMsisdn) {
		this.consumerMsisdn = consumerMsisdn;
	}

	/**
	 * @return the consumerName
	 */
	public String getConsumerName() {
		return consumerName;
	}

	/**
	 * @param consumerName the consumerName to set
	 */
	public void setConsumerName(String consumerName) {
		this.consumerName = consumerName;
	}

	/**
	 * @return the autoApprove
	 */
	public Boolean getAutoApprove() {
		return autoApprove;
	}

	/**
	 * @param autoApprove the autoApprove to set
	 */
	public void setAutoApprove(Boolean autoApprove) {
		this.autoApprove = autoApprove;
	}

	/**
	 * @return the dbId
	 */
	public Integer getDbId() {
		return dbId;
	}

	/**
	 * @param dbId the dbId to set
	 */
	public void setDbId(Integer dbId) {
		this.dbId = dbId;
	}
	
	/**
	 * This method gets all param map in request
	 * @return
	 */
	public Map<AuxRequestParam, Object> getAuxReqParamMap() {
		return auxiliaryRequestParameters;
	}

	/**
	 * @return the providerMsisdn
	 */
	public String getProviderMsisdn() {
		return providerMsisdn;
	}

	/**
	 * @param providerMsisdn the providerMsisdn to set
	 */
	public void setProviderMsisdn(String providerMsisdn) {
		this.providerMsisdn = providerMsisdn;
	}

		/**
	 * @return the commandInputParams
	 */
	public String getCommandInputParams(final FulfillmentParameters fulfillmentParameters) {
		return (null != commandInputParams ? commandInputParams.get(fulfillmentParameters) : null);
	}

	/**
	 * @param commandInputParams the commandInputParams to set
	 */
	public void setCommandInputParams(final FulfillmentParameters parameters, final String value) {
		if(null == this.commandInputParams) {
			this.commandInputParams = new HashMap<FulfillmentParameters, String>();
		}
		this.commandInputParams.put(parameters, value);
	}
	
	public String getIname() {
		return iname;
	}

	public void setIname(String iname) {
		this.iname = iname;
	}
	
  	/**
	 * @return the transaction_id 
	 */

	public String getTransaction_id() {
		return transaction_id;
	}

        /**
	 * @param transaction_id to set
	 */
	public void setTransaction_id(String transaction_id) {
		this.transaction_id = transaction_id;
	}
	
}
