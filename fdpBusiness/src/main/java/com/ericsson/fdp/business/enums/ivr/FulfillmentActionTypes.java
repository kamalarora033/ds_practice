package com.ericsson.fdp.business.enums.ivr;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This enum contains mapping for different IVR service actions and their
 * success and fail response.
 * 
 * @author Ericsson
 * 
 */
public enum FulfillmentActionTypes { 

	PRODUCT_BUY("OFFLINE_PROD_BUY", IVRResponseTypes.PRODUCT_BUY_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays.asList(FulfillmentParameters.TRANSACTION_ID),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/ProductProvisioningFulFillmentServiceImpl"),

	TARIFF_ENQUIRY("Tariff Enquiry", IVRResponseTypes.TARIFF_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, null, null),

	BALANCE_CHECK("Balance Enquiry", IVRResponseTypes.BALANCE_ENQUIRY_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, null, null),

	GET_COMMANDS("Get Commands", IVRResponseTypes.COMMAND_SUCCESS_RESPONSE, IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE,
			null, null),

	RS_DEPROVISION_PRODUCT("Rs Deprovisioning", IVRResponseTypes.RECURRING_SERVICE_SUCCESS_RESPONSE,
			IVRResponseTypes.RECURRING_SERVICE_FAIL_RESPONSE, null, "java:global/fdpBusiness-ear/fdpBusiness-1.0/ProductDeProvisioningFulFillmentServiceImpl"), ADD_SHARED_CONSUMER("ADD_CONSUMER",
			IVRResponseTypes.ADD_SHARED_CONSUMER_SUCCESS_RESPONSE, IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays
					.asList(FulfillmentParameters.CONSUMER_MSISDN, FulfillmentParameters.CONSUMER_NAME),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/AddConsumerFulFillmentServiceImpl"),

	REMOVE_SHARED_CONSUMER("REMOVE_CONSUMER", IVRResponseTypes.REMOVE_SHARED_CONSUMER_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays.asList(FulfillmentParameters.CONSUMER_MSISDN,
					FulfillmentParameters.AUTO_APPROVE_SHARED),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/RemoveConsumerFulFillmentServiceImpl"),

	VIEW_ALL_SHARED_CONSUMER("VIEW_ALL_CONSUMER", IVRResponseTypes.VIEW_ALL_SHARED_CONSUMER_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, null,
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/ViewAllConsumerFulFillmentServiceImpl"),

	VIEW_SHARED_USAGE("VIEW_USAGE", IVRResponseTypes.VIEW_SHARED_USAGE_CONSUMER_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays.asList(FulfillmentParameters.CONSUMER_MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/ViewUsageFulFillmentServiceImpl"),

	ACCEPT_SHARED_CONSUMER("ACCEPT_CONSUMER", IVRResponseTypes.ACCEPT_SHARED_CONSUMER_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays.asList(FulfillmentParameters.DATABASE_ID),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/AcceptConsumerFulFillmentServiceImpl"),

	VIEW_ALL_SHARED_USAGE("VIEW_TOTAL_USAGE", IVRResponseTypes.VIEW_SHARED_USAGE_CONSUMER_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, null,
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/ViewTotalUsageFulFillmentServiceImpl"),

	DETACH_CONSUMER("DETACH_CONSUMER", IVRResponseTypes.REMOVE_SHARED_CONSUMER_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays.asList(FulfillmentParameters.PROVIDER_MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/DetachConsumerFulFillmentServiceImpl"),

	ADD_CONSUMER_ON_REQUEST("ADD_CONSUMER_ON_REQUEST", IVRResponseTypes.ADD_SHARED_CONSUMER_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays.asList(FulfillmentParameters.PROVIDER_MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/AddConsumerOnRequestFulFillmentServiceImpl"),

	VIEW_ALL_PROVIDERS("VIEW_ALL_PROVIDERS", IVRResponseTypes.VIEW_ALL_SHARED_PROVIDER_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, null,
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/ViewAllProviderFulFillmentServiceImpl"),

	SEND_OFFLINE_NOTIFICATION("SEND_NOTI", IVRResponseTypes.OFFLINE_NOTIFICAITON_SUCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, null,
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/SendOfflineNotificationServiceImpl"),
			
	VIEW_HISTORY("VIEW_HISTORY", IVRResponseTypes.VIEW_HISTORY, IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays
			.asList(FulfillmentParameters.MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/ViewHistoryServiceImpl"),

	OPT_IN("OPT_IN", IVRResponseTypes.OPT_IN_SUCCESS_RESPONSE, IVRResponseTypes.OPT_IN_FAILURE_RESPONSE, Arrays
			.asList(FulfillmentParameters.MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/OptInConsumerFulFillmentServiceImpl"),

	OPT_OUT("OPT_OUT", IVRResponseTypes.OPT_OUT_SUCCESS_RESPONSE, IVRResponseTypes.OPT_OUT_FAILURE_RESPONSE, Arrays
			.asList(FulfillmentParameters.MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/OptOutConsumerFulFillmentServiceImpl"),

	BUNDLE_PRICE("BUNDLE_PRICE", IVRResponseTypes.BUNDLE_PRICE_SUCCESS_RESPONSE,
			IVRResponseTypes.BUNDLE_PRICE_FAILURE_RESPONSE, Arrays.asList(FulfillmentParameters.MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/GetBundlePriceFulfillmentServiceImpl"),

	VIEW_SUBS_HISTORY("VIEW_SUBS_HISTORY", IVRResponseTypes.VIEW_SUBS_HISTORY_SUCCESS_RESPONSE,
			IVRResponseTypes.VIEW_SUBS_HISTORY_FAILURE_RESPONSE, Arrays.asList(FulfillmentParameters.MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/ViewSubsHistoryFulfillmentServiceImpl"),

	FAF("FAF", IVRResponseTypes.FAF_SUCCESS_RESPONSE, IVRResponseTypes.FAF_FAILURE_RESPONSE, null,
			null),
	
	/** New Action added for ME2ULIST*/
	ME2ULIST("ME2ULIST", IVRResponseTypes.ME2ULIST_SUCCESS_RESPONSE, IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays.asList(FulfillmentParameters.MSISDN),
				"java:global/fdpBusiness-ear/fdpBusiness-1.0/GetMe2uListFulfillmentServiceImpl"),
	
	/** New Action added for ABILITY*/
	ABILITY_SYNC_UP("ABILITY_SYNC_UP", IVRResponseTypes.ABILITY_SUCCESS_RESPONSE, IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays.asList(FulfillmentParameters.MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/AbilitySyncUpFulfillmentServiceImpl"), 
	
	PRODUCT_TRANSFER("PRODUCT_TRANSFER", IVRResponseTypes.PRODUCT_BUY_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays.asList(FulfillmentParameters.TRANSACTION_ID),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/ProductProvisioningFulFillmentServiceImpl"),
			
	/** New Action added for Device Change - Handset Based Charging Use Case*/
	DEVICE_CHANGE("DEVICE_CHANGE", IVRResponseTypes.DEVICE_CHANGE_SUCCESS_RESPONSE, IVRResponseTypes.DEVICE_CHANGE_FAILURE_RESPONSE, Arrays.asList(FulfillmentParameters.MSISDN, FulfillmentParameters.OLD_DEV_TYPE, FulfillmentParameters.OLD_IMEI, FulfillmentParameters.NEW_IMEI, FulfillmentParameters.NEW_DEV_TYPE),
				"java:global/fdpBusiness-ear/fdpBusiness-1.0/DeviceChangeFulfillmentServiceImpl"),
							
	/** New Action added for SIM change - Handset Based Charging Use Case*/
	SIM_CHANGE("SIM_CHANGE", IVRResponseTypes.SIM_CHANGE_SUCCESS_RESPONSE, IVRResponseTypes.SIM_CHANGE_FAILURE_RESPONSE, Arrays.asList(FulfillmentParameters.MSISDN, FulfillmentParameters.OLD_DEV_TYPE, FulfillmentParameters.OLD_IMEI, FulfillmentParameters.OLD_MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/SIMChangeFulFillmentServiceImpl"),
			
	SHARED_BONUS_BUNDLE_ADD_CONSUMER("SBB_ADD_CONSUMER",
			IVRResponseTypes.ADD_SHARED_CONSUMER_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays
				.asList(FulfillmentParameters.CONSUMER_MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/SBBAddConsumerServiceImpl"),

	SHARED_BONUS_BUNDLE_DELETE_CONSUMER("SBB_DELETE_CONSUMER",
			IVRResponseTypes.REMOVE_SHARED_CONSUMER_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, Arrays
				.asList(FulfillmentParameters.CONSUMER_MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/SBBDeleteConsumerServiceImpl"),
					
	SHARED_BONUS_BUNDLE_VIEW_CONSUMER("SBB_VIEW_CONSUMER", IVRResponseTypes.VIEW_ALL_SHARED_CONSUMER_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, null,
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/SBBViewConsumerServiceImpl"),
					
	SHARED_BONUS_BUNDLE_VIEW_PROVIDER("SBB_VIEW_PROVIDER", IVRResponseTypes.VIEW_ALL_SHARED_CONSUMER_SUCCESS_RESPONSE,
			IVRResponseTypes.FULFILLMENT_FAIL_RESPONSE, null,
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/SBBViewConsumerServiceImpl"),
	
	/** The VIEW_ACTIVE_BUNDLES Constant */
	VIEW_ACTIVE_BUNDLES("VIEW_ACTIVE_BUNDLES", IVRResponseTypes.VIEW_ACTIVE_BUNDLES_SUCCESS_RESPONSE,
			IVRResponseTypes.VIEW_ACTIVE_BUNDLES_FAILURE_RESPONSE, Arrays.asList(FulfillmentParameters.MSISDN),
			"java:global/fdpBusiness-ear/fdpBusiness-1.0/ViewActiveBundlesFulfillmentServiceImpl");
	
	/** The value. */
	private String value;

	/** The successResponse **/
	private IVRResponseTypes successResponse;

	/** The failResponse **/
	private IVRResponseTypes failResponse;

	/** The FulFillmentParameter list **/
	private List<FulfillmentParameters> parameters;

	/** The jndilookupname **/
	private String jndiLookupName;

	private static Map<String, FulfillmentActionTypes> actionMap;

	/**
	 * Instantiates a new iVR action type.
	 * 
	 * @param value
	 *            the value
	 */
	private FulfillmentActionTypes(final String value, final IVRResponseTypes successResponse,
			final IVRResponseTypes failResponse, final List<FulfillmentParameters> parameters,
			final String jndiLookupName) {
		this.value = value;
		this.successResponse = successResponse;
		this.failResponse = failResponse;
		this.parameters = parameters;
		this.jndiLookupName = jndiLookupName;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public IVRResponseTypes getSuccessResponse() {
		return successResponse;
	}

	public void setSuccessResponse(IVRResponseTypes successResponse) {
		this.successResponse = successResponse;
	}

	public IVRResponseTypes getFailResponse() {
		return failResponse;
	}

	public void setFailResponse(IVRResponseTypes failResponse) {
		this.failResponse = failResponse;
	}

	/**
	 * @return the parameters
	 */
	public List<FulfillmentParameters> getParameters() {
		return parameters;
	}

	/**
	 * @param parameters
	 *            the parameters to set
	 */
	public void setParameters(List<FulfillmentParameters> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return the jndiLookupName
	 */
	public String getJndiLookupName() {
		return jndiLookupName;
	}

	/**
	 * @param jndiLookupName
	 *            the jndiLookupName to set
	 */
	public void setJndiLookupName(String jndiLookupName) {
		this.jndiLookupName = jndiLookupName;
	}

	/**
	 * This method provides the FulfillmentActionTypes based on action Name from
	 * request.
	 * 
	 * @param actionName
	 * @return
	 */
	public static FulfillmentActionTypes getFulfillmentActionTypesByName(final String action) {
		if (null == actionMap) {
			populateActionMap();
		}
		return actionMap.get(action);
	}

	/**
	 * This method polulates the action map.
	 */
	private static void populateActionMap() {
		actionMap = new HashMap<String, FulfillmentActionTypes>();
		for (final FulfillmentActionTypes actionTypes : FulfillmentActionTypes.values()) {
			actionMap.put(actionTypes.getValue(), actionTypes);
		}
	}
}
