package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.ericsson.fdp.business.enums.ivr.FulfillmentActionTypes;
import com.ericsson.fdp.business.enums.ivr.FulfillmentResponseTypes;

@XmlRootElement(name = "responseData")
@XmlAccessorType(XmlAccessType.FIELD)
public class ResponseData implements Serializable {

	/**
	 * The class serial version UID
	 */
	private static final long serialVersionUID = 2472845039979466839L;

	/** The response type **/
	@XmlElement(name = "responseType", required = true)
	private FulfillmentResponseTypes responseType;

	/** The action **/
	@XmlElement(name = "action", required = true)
	private FulfillmentActionTypes action;

	/** The input Code **/
	@XmlElement(name = "inputCode", required = true)
	private String inputCode;

	/** The circle Code **/
	@XmlElement(name = "circleCode", required = true)
	private String circleCode;

	@XmlElement(name = "serviceClass")
	private String serviceClass;

	/** The tariff enquiry **/
	@XmlElement(name = "tariffEnquiry")
	private TariffEnquiry tariffEnquiry;

	/** The balance enquiry **/
	@XmlElement(name = "balanceEnquiry")
	private BalanceEnquiry balanceEnquiry;

	/** the product buy **/
	@XmlElement(name = "productBuy")
	private ProductBuy productBuy;

	/** The get command response **/
	@XmlElement(name = "getCommandResponse")
	private GetCommandResponse getCommandResponse;

	@XmlElement(name = "sharedAccount")
	private SharedAccount sharedAccount;

	@XmlElement(name = "response")
	private Response response;

	@XmlElement(name = "ProductDetail")
	private ProductDetail productDetail;

	@XmlElement(name = "products")
	private Products products;

	@XmlElement(name = "msisdn")
	private String msisdn;

	@XmlElement(name = "fafmsisdn")
	private String fafMsisdn;

	@XmlElement(name = "notification")
	private String notification;
	
	@XmlElement(name="activebundles")
	private ActiveBundles activeBundles;

	public String getMsisdn() {
		return msisdn;
	}

	public void setMsisdn(String msisdn) {
		this.msisdn = msisdn;
	}

	public String getFafMsisdn() {
		return fafMsisdn;
	}

	public void setFafMsisdn(String fafMsisdn) {
		this.fafMsisdn = fafMsisdn;
	}

	public String getNotification() {
		return notification;
	}

	public void setNotification(String notification) {
		this.notification = notification;
	}

	public String getServiceClass() {
		return serviceClass;
	}

	public void setServiceClass(String serviceClass) {
		this.serviceClass = serviceClass;
	}

	public Products getProducts() {
		return products;
	}

	public void setProducts(Products products) {
		this.products = products;
	}

	/**
	 * @return the bundlePrice
	 */
	public ProductDetail getproductDetail() {
		return productDetail;
	}

	/**
	 * @param bundlePrice
	 *            the bundlePrice to set
	 */
	public void setProductDetail(ProductDetail productDetail) {
		this.productDetail = productDetail;
	}

	/**
	 * @return the responseType
	 */
	public FulfillmentResponseTypes getResponseType() {
		return responseType;
	}

	/**
	 * @param responseType
	 *            the responseType to set
	 */
	public void setResponseType(FulfillmentResponseTypes responseType) {
		this.responseType = responseType;
	}

	/**
	 * @return the action
	 */
	public FulfillmentActionTypes getAction() {
		return action;
	}

	/**
	 * @param action
	 *            the action to set
	 */
	public void setAction(FulfillmentActionTypes action) {
		this.action = action;
	}

	/**
	 * @return the inputCode
	 */
	public String getInputCode() {
		return inputCode;
	}

	/**
	 * @param inputCode
	 *            the inputCode to set
	 */
	public void setInputCode(String inputCode) {
		this.inputCode = inputCode;
	}

	/**
	 * @return the circleCode
	 */
	public String getCircleCode() {
		return circleCode;
	}

	/**
	 * @param circleCode
	 *            the circleCode to set
	 */
	public void setCircleCode(String circleCode) {
		this.circleCode = circleCode;
	}

	/**
	 * @return the tariffEnquiry
	 */
	public TariffEnquiry getTariffEnquiry() {
		return tariffEnquiry;
	}

	/**
	 * @param tariffEnquiry
	 *            the tariffEnquiry to set
	 */
	public void setTariffEnquiry(TariffEnquiry tariffEnquiry) {
		this.tariffEnquiry = tariffEnquiry;
	}

	/**
	 * @return the balanceEnquiry
	 */
	public BalanceEnquiry getBalanceEnquiry() {
		return balanceEnquiry;
	}

	/**
	 * @param balanceEnquiry
	 *            the balanceEnquiry to set
	 */
	public void setBalanceEnquiry(BalanceEnquiry balanceEnquiry) {
		this.balanceEnquiry = balanceEnquiry;
	}

	/**
	 * @return the productBuy
	 */
	public ProductBuy getProductBuy() {
		return productBuy;
	}

	/**
	 * @param productBuy
	 *            the productBuy to set
	 */
	public void setProductBuy(ProductBuy productBuy) {
		this.productBuy = productBuy;
	}

	/**
	 * @return the getCommandResponse
	 */
	public GetCommandResponse getGetCommandResponse() {
		return getCommandResponse;
	}

	/**
	 * @param getCommandResponse
	 *            the getCommandResponse to set
	 */
	public void setGetCommandResponse(GetCommandResponse getCommandResponse) {
		this.getCommandResponse = getCommandResponse;
	}

	/**
	 * @return the sharedAccount
	 */
	public SharedAccount getSharedAccount() {
		return sharedAccount;
	}

	/**
	 * @param sharedAccount
	 *            the sharedAccount to set
	 */
	public void setSharedAccount(SharedAccount sharedAccount) {
		this.sharedAccount = sharedAccount;
	}

	/**
	 * @return the response
	 */
	public Response getResponse() {
		return response;
	}

	/**
	 * @param response
	 *            the response to set
	 */
	public void setResponse(Response response) {
		this.response = response;
	}

	/**
	 * @return the activeBundles
	 */
	public ActiveBundles getActiveBundles() {
		return activeBundles;
	}

	/**
	 * @param activeBundles the activeBundles to set
	 */
	public void setActiveBundles(ActiveBundles activeBundles) {
		this.activeBundles = activeBundles;
	}
	
	

}
