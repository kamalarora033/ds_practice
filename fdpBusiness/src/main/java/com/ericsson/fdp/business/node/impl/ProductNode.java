package com.ericsson.fdp.business.node.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.node.AbstractServiceProvisioningNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.product.impl.SharedAccountProduct;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.PostLogsUtil;
import com.ericsson.fdp.business.util.ProductUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.DynamicMenuAdditionalInfoKey;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.enums.LanguageType;
import com.ericsson.fdp.dao.util.ServiceProvDataConverterUtil;

/**
 * This class defines the nodes which are of type product.
 * 
 * @author Ericsson
 * 
 */
public class ProductNode extends AbstractServiceProvisioningNode {

	/**
	 * Instantiates a new product node.
	 */
	public ProductNode() {
	}

	/** The alias code list. */
	List<String> aliasCodeList = null;

	// TODO: Hariom please set the service prov sub type.
	/** The service prov sub type. */
	private FDPServiceProvSubType serviceProvSubType;

	@Override
	public String getEntityIdForCache(final RequestMetaValuesKey key) {
		String value = null;
		switch (key) {
		case PRODUCT:
			value = getEntityId().toString();
			break;
		case SERVICE_PROVISIONING:
			value = ServiceProvDataConverterUtil.getKeyForServiceProvMetaBag(getEntityId(), FDPServiceProvType.PRODUCT,
					serviceProvSubType);
		default:
			break;
		}
		return value;
	}

	/**
	 * Instantiates a new product node.
	 * 
	 * @param displayNameToSet
	 *            the display name to set
	 * @param shortCodeToSet
	 *            the short code to set
	 * @param fullyQualifiedPathToSet
	 *            the fully qualified path to set
	 * @param channelToSet
	 *            the channel to set
	 * @param circleToSet
	 *            the circle to set
	 * @param priorityToSet
	 *            the priority to set
	 * @param parentToSet
	 *            the parent to set
	 * @param childrenToSet
	 *            the children to set
	 * @param entityIdToSet
	 *            the entity id to set
	 * @param entityNameToSet
	 *            the entity name to set
	 * @param subType
	 *            the sub type
	 * @param aliasCodes
	 *            the alias Codes
	 * 
	 */
	public ProductNode(final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet, final FDPNode parentToSet, final List<FDPNode> childrenToSet,
			final Long entityIdToSet, final String entityNameToSet, final FDPServiceProvSubType subType,
			final List<String> aliasCodes, final Map<String, Object> additionalInfo,final Visibility visibility,final State state) {
		super.setAbstractServiceProvisioningNodeNode(displayNameToSet, shortCodeToSet, fullyQualifiedPathToSet,
				channelToSet, circleToSet, priorityToSet, parentToSet, childrenToSet, entityIdToSet, entityNameToSet,
				additionalInfo,visibility,state);
		this.serviceProvSubType = subType;
		if (aliasCodes != null && !aliasCodes.isEmpty()) {
			aliasCodeList = new ArrayList<String>(2);
			aliasCodeList.addAll(aliasCodes);
		}
		if(additionalInfo.get(DynamicMenuAdditionalInfoKey.FRENCH_DISPLAY_NAME.name())!=null){
			Map<LanguageType, String> otherLanguageMap = new HashMap<LanguageType, String> ();
			otherLanguageMap.put(LanguageType.FRENCH, additionalInfo.get(DynamicMenuAdditionalInfoKey.FRENCH_DISPLAY_NAME.name()).toString());
			this.setOtherLangMap(otherLanguageMap);
		}
	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2587382091237740496L;

	@Override
	public boolean evaluateNode(final FDPRequest fdpRequest) throws EvaluationFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		// return true;
		final Product product = RequestUtil.updateProductAndSPInRequest(fdpRequest,
				getEntityIdForCache(RequestMetaValuesKey.PRODUCT),
				getEntityIdForCache(RequestMetaValuesKey.SERVICE_PROVISIONING));
		boolean executedStatus = false;
		boolean evaluateValue = true;
		try {
			if (State.ACTIVE_FOR_TEST.equals(product.getState())) {
				FDPLogger.debug(circleLogger, getClass(), "evaluateNode()", LoggerUtil.getRequestAppender(fdpRequest)
						+ "Product active for test only. Checking for whitelisted users");
				evaluateValue = false;
				// Product is active for testing only. Proceed only for white
				// listed users.

				final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
				final String whiteListedUsers = configurationMap.get(ConfigurationKey.WHITELISTED_NUMBER_TESTING
						.getAttributeName());
				final List<Long> whiteListedUsersList = new ArrayList<Long>();

				try {
					if (!(whiteListedUsers == null || whiteListedUsers.isEmpty())) {
						for (final String whiteListesUser : Arrays.asList(whiteListedUsers.split(FDPConstant.COMMA))) {
							whiteListedUsersList.add(Long.valueOf(whiteListesUser));
						}
					}
				} catch (final NumberFormatException e) {
					FDPLogger.error(circleLogger, getClass(), "evaluateNode()",
							LoggerUtil.getRequestAppender(fdpRequest)
									+ "Error !! While loading white listed user in cache.");
					throw new EvaluationFailedException("Error !! While loading white listed user in cache.", e);
				}
				if (whiteListedUsersList.contains(fdpRequest.getSubscriberNumber())) {
					FDPLogger.debug(circleLogger, getClass(), "evaluateNode()",
							LoggerUtil.getRequestAppender(fdpRequest) + "User qualified to product");
					// Evaluate only if it is a white listed user.
					evaluateValue = true;
				}
			}
			if (evaluateValue) {
				Expression expression = product.getConstraintForChannel(fdpRequest.getChannel());
				expression = (null == expression && (ChannelType.IVR.equals(fdpRequest.getChannel())) || ChannelType.ABILITY
						.equals(fdpRequest.getChannel())) ? product.getConstraintForChannel(ChannelType.WEB)
						: expression;
				executedStatus = expression != null ? expression.evaluateExpression(fdpRequest) : true;
			}
		} catch (final ExpressionFailedException e) {
			FDPLogger.error(circleLogger, getClass(), "evaluateNode()",
					"The constraint for the product could not be evaluated.", e);
			throw new EvaluationFailedException("The constraint for the product could not be evaluated.", e);
		}
		return executedStatus;
	}

	@Override
	public String generateNodeInfo(final FDPRequest fdpRequest) {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		final StringBuilder userBehaviourLoggString = new StringBuilder();
		Product product = null;
		final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (fdpCacheable instanceof Product) {
			product = (Product) fdpCacheable;
		}
		userBehaviourLoggString.append("Product").append(FDPConstant.LOGGER_DELIMITER).append("PRODUCT")
				.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(serviceProvSubType.getText())
				.append(FDPConstant.LOGGER_DELIMITER).append("PID").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER);
		if (product == null) {
			try {
				product = RequestUtil.updateProductAndSPInRequest(fdpRequest,
						getEntityIdForCache(RequestMetaValuesKey.PRODUCT),
						getEntityIdForCache(RequestMetaValuesKey.SERVICE_PROVISIONING));
			} catch (final EvaluationFailedException e) {
				userBehaviourLoggString.append("Undefined");
				FDPLogger.error(circleLogger, getClass(), "generateNodeInfo()",
						"Log for the product could not be generated.", e);
				return userBehaviourLoggString.toString();
			}
		}

		if (product == null) {
			userBehaviourLoggString.append("Undefined");
		} else {
			userBehaviourLoggString.append(product.getProductId()).append(FDPConstant.LOGGER_DELIMITER).append("PNAME")
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER).append(product.getProductName())
					.append(FDPConstant.LOGGER_DELIMITER).append("PCNAME")
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductNameForChannel(fdpRequest.getChannel()))
					.append(FDPConstant.LOGGER_DELIMITER).append("PTYPE").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					//.append(FDPConstant.LOGGER_DELIMITER).append("PTP").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductType()).append(FDPConstant.LOGGER_DELIMITER).append("PUSNAME")
					.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductNameForChannel(ChannelType.USSD)).append(FDPConstant.LOGGER_DELIMITER)
					.append("PSMSNAME").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductNameForChannel(ChannelType.SMS)).append(FDPConstant.LOGGER_DELIMITER)
					.append("PWBNAME").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(product.getProductNameForChannel(ChannelType.WEB)).append(FDPConstant.LOGGER_DELIMITER)
					.append("PCNSTRNT").append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
					.append(PostLogsUtil.getProductConstraint(fdpRequest, product));
			if (product instanceof SharedAccountProduct) {
				final SharedAccountProduct sharedAccountProduct = (SharedAccountProduct) product;
				userBehaviourLoggString
						.append(FDPConstant.LOGGER_DELIMITER)
						.append("PMETA")
						.append(FDPConstant.LOGGER_KEY_VALUE_DELIMITER)
						.append(StringUtil.convertMapToStringWithDelimiter(sharedAccountProduct.getProductMetaData(),
								FDPConstant.LOGGER_KEY_VALUE_VALUE_DELIMITER));
			}
		}
		return userBehaviourLoggString.toString();
	}

	/**
	 * Checks if is shared account type.
	 * 
	 * @return true, if is shared account type
	 */
	public boolean isSharedAccountType() {
		return serviceProvSubType.isSharedAccountType();
	}

	@Override
	public FDPResponse executePolicy(final FDPRequest fdpRequest) throws ExecutionFailedException {
		if (!fdpRequest.isPolicyExecution()
				&& (FDPServiceProvSubType.PRODUCT_BUY.equals(serviceProvSubType)
						|| FDPServiceProvSubType.TARIFF_ENQUIRY.equals(serviceProvSubType)
						|| FDPServiceProvSubType.PAM_DEPROVISION_PRODUCT.equals(serviceProvSubType) || FDPServiceProvSubType.RS_DEPROVISION_PRODUCT
							.equals(serviceProvSubType)  || FDPServiceProvSubType.PRODUCT_BUY_SPLIT.equals(serviceProvSubType)
							|| FDPServiceProvSubType.PRODUCT_BUY_RECURRING.equals(serviceProvSubType))) {
			try {
				ProductUtil.updateForProductBuyPolicy(fdpRequest, getEntityIdForCache(RequestMetaValuesKey.PRODUCT),
						getEntityIdForCache(RequestMetaValuesKey.SERVICE_PROVISIONING),
						serviceProvSubType.getChargingType());
			} catch (final EvaluationFailedException e) {
				throw new ExecutionFailedException("Could not update policy ", e);
			}
		}
		return super.executePolicy(fdpRequest);
	}

	@Override
	public List<String> getAliasCode() {
		return aliasCodeList;
	}

	public FDPServiceProvSubType getServiceProvSubType() {
		return serviceProvSubType;
	}

}
