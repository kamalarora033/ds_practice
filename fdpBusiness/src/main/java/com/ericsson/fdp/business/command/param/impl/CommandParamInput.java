
package com.ericsson.fdp.business.command.param.impl;

import java.lang.reflect.Field;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ch.qos.logback.classic.Logger;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.charging.value.ChargingValue;
import com.ericsson.fdp.business.charging.value.ChargingValueImpl;
import com.ericsson.fdp.business.command.param.AbstractCommandParam;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.ConfigurationKey;
import com.ericsson.fdp.business.enums.Function;
import com.ericsson.fdp.business.enums.ParamTransformationType;
import com.ericsson.fdp.business.enums.ProvisioningTypeEnum;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.node.impl.SpecialMenuNode;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.step.impl.ServiceStep;
import com.ericsson.fdp.business.util.ClassUtil;
import com.ericsson.fdp.business.util.CommandParamInputUtil;
import com.ericsson.fdp.business.util.CommandParamUtil;
import com.ericsson.fdp.business.util.CommandUtil;
import com.ericsson.fdp.business.util.FnfUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.MCarbonCommandUtil;
import com.ericsson.fdp.business.util.MVELUtil;
import com.ericsson.fdp.business.util.Me2uUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.TransformationUtil;
import com.ericsson.fdp.business.vo.ProductAttributeMapCacheDTO;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ExternalSystem;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.SPServices;
import com.ericsson.fdp.common.util.DateUtil;
import com.ericsson.fdp.common.util.FDPCommonValidationUtil;
import com.ericsson.fdp.common.util.StringUtil;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.entity.ExternalSystemDetail;
import com.ericsson.fdp.core.entity.MCarbonSystemDetail;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.impl.FDPRequestImpl;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationCacheUtil;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.serviceprov.AbstractServiceProvDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvProductDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ValidityValueDTO;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.LanguageType;
import com.ericsson.fdp.dao.enums.ProductAdditionalInfoEnum;
import com.ericsson.fdp.dao.enums.ValidityTypeEnum;

/**
 * This class defines the input parameter to the command.
 * 
 * @author Ericsson
 * 
 */
public class CommandParamInput extends AbstractCommandParam {

	private long origantransactionid;

	/**
	 * 
	 */
	private static final long serialVersionUID = 2263085472787546987L;

	/** The source of the parameter from which the value is to be derived. */
	private final ParameterFeedType commandParameterSource;

	/** The defined value of the parameter. */
	private final Object definedValue;

	/**
	 * The transformation to be applied to the input.
	 */
	private ParamTransformationType paramTransformationType = ParamTransformationType.NONE;

	/**
	 * The circle logger.
	 */
	private Logger logger;

	/**
	 * The constructor for the command param input.
	 * 
	 * @param commandParameterSourceToSet
	 *            The command parameter source value to set.
	 * @param definedValueToSet
	 *            The defined value of the parameter.
	 */
	public CommandParamInput(final ParameterFeedType commandParameterSourceToSet, final Object definedValueToSet) {
		this.commandParameterSource = commandParameterSourceToSet;
		this.definedValue = definedValueToSet;
	}

	/**
	 * This method is used to evaluate the value of the input parameter.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @exception EvaluationFailedException
	 *                Exception, in case of evaluation fails.
	 */
	public void evaluateValue(final FDPRequest fdpRequest) throws EvaluationFailedException {
		logger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);

		FDPLogger.debug(logger, getClass(), "evaluateValue()",
				LoggerUtil.getRequestAppender(fdpRequest) + "Evaluating parameter " + getName());

		switch (super.getType()) {
		case ARRAY:
			evaluateComplexValue(fdpRequest);
			break;
		case PRIMITIVE:
		case COMMAND_IDENTIFIER:
		case PARAM_IDENTIFIER:
			evaluatePrimitiveValue(fdpRequest);
			break;
		case STRUCT:
			evaluateComplexValue(fdpRequest);
			break;
		/*
		 * case MVEL_IDENTIFIER: evaluateMvelIndentifier(fdpRequest); break;
		 */

		default:
			FDPLogger.error(logger, getClass(), "evaluateValue()", LoggerUtil.getRequestAppender(fdpRequest)
					+ "The type of the parameter could not be recognized. Type found is " + super.getType());
			throw new EvaluationFailedException(
					"The type of the parameter could not be recognized. Type found is " + super.getType());
		}
	}

	/**
	 * This method is used to evaluate Mvel parameters
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @throws EvaluationFailedException
	 *             Exception, in case of evaluation fails.
	 */

	/*
	 * private void evaluateMvelIndentifier(final FDPRequest fdpRequest) throws
	 * EvaluationFailedException{ >>>>>>> .r526
	 * System.out.println("shanks===========>>"); } <<<<<<< .mine
	 * 
	 * =======
	 */

	/**
	 * This method is used to evaluate parameters which are of type array or
	 * struct.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @throws EvaluationFailedException
	 *             Exception, in case of evaluation fails.
	 */
	private void evaluateComplexValue(final FDPRequest fdpRequest) throws EvaluationFailedException {
		for (final CommandParam childParam : super.getChilderen()) {
			if (childParam instanceof CommandParamInput) {
				((CommandParamInput) childParam).evaluateValue(fdpRequest);
			} else {
				throw new EvaluationFailedException("The child parameter is not of type input");
			}
		}
	}

	/**
	 * This method is used to evaluate parameters which are primitive types.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @throws EvaluationFailedException
	 *             Exception, in case of evaluation fails.
	 */
	private void evaluatePrimitiveValue(final FDPRequest fdpRequest) throws EvaluationFailedException {
		Object parameterValue = null;
		try {
			// System.out.println("command param source ::
			// "+commandParameterSource);
			switch (commandParameterSource) {
			case INPUT:
				parameterValue = definedValue;
				break;
			case COMMAND_OUTPUT:
				parameterValue = getParamValueForCommandOutput(fdpRequest);
				break;
			case REQUEST:
				parameterValue = getValueFromObject(fdpRequest.getClass(), (String) definedValue, fdpRequest);
				break;
			case FUNCTION:
				parameterValue = evaluateFunction((Function) definedValue, fdpRequest);
				break;
			case PRODUCT:
				final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
				parameterValue = getValueFromObjectForProduct(product.getClass(), definedValue, (Product) product);
				break;
			case CHARGING:
				final FDPCacheable charging = fdpRequest.getValueFromRequest(RequestMetaValuesKey.CHARGING_STEP);
				parameterValue = getValueFromObject(charging.getClass(), (String) definedValue, charging);
				// Remove the negative sign from chargingValue in case the
				// external system is EVDS
				if (this.getCommand().getSystem().toString().equals(ExternalSystem.EVDS.toString())
						|| this.getCommand().getSystem().toString().equals(ExternalSystem.MM.toString())) {
					if (Long.valueOf(parameterValue.toString()) < 0) {

						String conversionFactor = getConfigurationMapValue(fdpRequest,
								ConfigurationKey.CS_CONVERSION_FACTOR);
						if (conversionFactor != null) {
							parameterValue = -1 * Float.valueOf(parameterValue.toString())
									/ Long.parseLong(conversionFactor);
							// * parameterValue = Long.valueOf(conversionFactor)
							// * Long.valueOf(parameterValue.toString()); }
						} else {
							parameterValue = -1 * Long.valueOf(parameterValue.toString());
						}
					}
				}
				// Multiplies charging amount with CS conversion factor if
				// Charging system is CS
				/*
				 * if (this.getCommand().getSystem().toString().equals(
				 * ExternalSystem.AIR.toString())) { String conversionFactor =
				 * getConfigurationMapValue(fdpRequest,
				 * ConfigurationKey.CS_CONVERSION_FACTOR); if (conversionFactor
				 * != null) { parameterValue = Long.valueOf(conversionFactor) *
				 * Long.valueOf(parameterValue.toString()); } }
				 */
				break;
			case GLOBAL:
				parameterValue = ApplicationConfigUtil.getApplicationConfigCache()
						.getValue(new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP, definedValue));
				break;
			case AUX_REQUEST_PARAM:
				parameterValue = fdpRequest.getAuxiliaryRequestParameter((AuxRequestParam) definedValue);
				if (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OPTINOPTOUT_TYPE)
						&& FDPConstant.OPT_IN_SERVICE.equals(
								fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OPTINOPTOUT_TYPE).toString()))
					parameterValue = FDPConstant.ADHOC;
				else if (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OPTINOPTOUT_TYPE)
						&& FDPConstant.OPT_OUT_SERVICE.equals(
								fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OPTINOPTOUT_TYPE).toString()))
					parameterValue = FDPConstant.RECURRING;
				else if (definedValue.toString().equals(FDPConstant.DistinctProduct) && parameterValue == null)
					parameterValue = true;
				else if (definedValue.toString().equals(FDPConstant.ProvisioningTypeStr) && parameterValue == null)
					parameterValue = FDPConstant.PROVISIONING_TYPE_BOTH;
				
				if (parameterValue == null) {
					parameterValue = "";
				}

				break;
			case STEP_OUTPUT:
				final String definedString = (String) definedValue;
				final String stepName = definedString.substring(0,
						definedString.indexOf(FDPConstant.PARAMETER_SEPARATOR));
				final String key = definedString.substring(definedString.indexOf(FDPConstant.PARAMETER_SEPARATOR) + 1,
						definedString.length());
				parameterValue = fdpRequest.getValueFromStep(stepName, key);
				break;
			case VALIDITY:
				parameterValue = CommandParamInputUtil.evaluateValidity((ValidityValueDTO) definedValue);
				break;
			case APP_CACHE:
				parameterValue = CommandParamInputUtil.getValueFromCircleConfiguration(definedValue.toString(),
						fdpRequest);
				break;
			case MVEL:
				parameterValue = MVELUtil.evaluateMvelExpression(fdpRequest, definedValue.toString(),
						this.getPrimitiveValue());
				break;

			case CSSUCCESS:
			case CSFAILURE:
				parameterValue = definedValue;
				break;
			case INPUT_CHECK:
				parameterValue = definedValue;
				break;
			default:
				FDPLogger.error(logger, getClass(), "evaluatePrimitiveValue()",
						"The source of the parameter could not be recognized. " + commandParameterSource);
				throw new EvaluationFailedException(
						"The source of the parameter could not be recognized. " + commandParameterSource);
			}
		} catch (final ExecutionFailedException e) {
			FDPLogger.error(logger, getClass(), "evaluatePrimitiveValue()", "Could not find the cache instance.", e);
			throw new EvaluationFailedException("Could not find the cache instance.", e);
		}
		FDPLogger.debug(logger, getClass(), "evaluatePrimitiveValue()",
				"Parameter source type " + commandParameterSource.name() + " value :- " + parameterValue);
		if (parameterValue == null) {
			FDPLogger.error(logger, getClass(), "evaluatePrimitiveValue()",
					"The value of the parameter could not be evaluated.");
			throw new EvaluationFailedException("The value of the parameter could not be evaluated.");
		}
		super.setValue(TransformationUtil.evaluateTransformation(parameterValue, paramTransformationType));
	}

	/**
	 * This method is used to find the value from the object using reflection.
	 * 
	 * @param clazz
	 *            The class of the object from which the value is to be found.
	 * @param fieldName
	 *            The field for which the value is to be required.
	 * @return The value of the field.
	 * @throws EvaluationFailedException
	 *             Exception, if the object could not be evaluated.
	 */
	private Object getValueFromObjectForProduct(final Class<?> clazz, final Object fieldName, final Product product)
			throws EvaluationFailedException {
		Object value = null;
		if (fieldName instanceof ProductAdditionalInfoEnum) {
			value = product.getAdditionalInfo((ProductAdditionalInfoEnum) fieldName);
		}
		if (value == null) {
			value = getValueFromObject(clazz, (String) fieldName, product);
		}
		return value;
	}

	/**
	 * This method is used to find the value from the object using reflection.
	 * 
	 * @param clazz
	 *            The class of the object from which the value is to be found.
	 * @param fieldName
	 *            The field for which the value is to be required.
	 * @return The value of the field.
	 * @throws EvaluationFailedException
	 *             Exception, if the object could not be evaluated.
	 */
	private Object getValueFromObject(final Class<?> clazz, final String fieldName, final Object object)
			throws EvaluationFailedException {
		Object value = null;
		try {
			final Field declaredFeild = clazz.getDeclaredField(fieldName);
			declaredFeild.setAccessible(true);
			value = declaredFeild.get(object);
		} catch (final NoSuchFieldException e) {
			if (!clazz.getSuperclass().equals(Object.class)) {
				return getValueFromObject(clazz.getSuperclass(), fieldName, object);
			}
			FDPLogger.error(logger, getClass(), "getValueFromObject()",
					"The value of the parameter could not be evaluated.", e);
			throw new EvaluationFailedException("The value of the parameter could not be evaluated.", e);
		} catch (final IllegalArgumentException e) {
			FDPLogger.error(logger, getClass(), "getValueFromObject()",
					"The value of the parameter could not be evaluated.", e);
			throw new EvaluationFailedException("The value of the parameter could not be evaluated.", e);
		} catch (final IllegalAccessException e) {
			FDPLogger.error(logger, getClass(), "getValueFromObject()",
					"The value of the parameter could not be evaluated.", e);
			throw new EvaluationFailedException("The value of the parameter could not be evaluated.", e);
		} catch (final SecurityException e) {
			FDPLogger.error(logger, getClass(), "getValueFromObject()",
					"The value of the parameter could not be evaluated.", e);
			throw new EvaluationFailedException("The value of the parameter could not be evaluated.", e);
		}
		return value;
	}

	/**
	 * This method is used to evaluate inputs of type function.
	 * 
	 * @param definedValueToUse
	 *            The defined value of the function.
	 * @param fdpRequest
	 *            the request parameter.
	 * @return The parameter value.
	 * @throws ExecutionFailedException
	 *             Exception in evaluation.
	 * @throws EvaluationFailedException
	 *             Exception in evaluation
	 */
	private Object evaluateFunction(final Function definedValueToUse, final FDPRequest fdpRequest)
			throws ExecutionFailedException, EvaluationFailedException {
		Object value = null;
		switch (definedValueToUse) {
		case NOW:
			value = DateUtil.convertCalendarDateToString(Calendar.getInstance(), FDPConstant.FDP_DB_SAVE_DATE_PATTERN);
			break;
		case VALIDATE_DATE:
			value = evaluateExpiryOffer(fdpRequest);
			;
			break;
		case VENDORID_SERVICEID:
			final FDPCacheable charging = fdpRequest.getValueFromRequest(RequestMetaValuesKey.CHARGING_STEP);
			final Object parameterValue = getValueFromObject(charging.getClass(), "chargingValue", charging);
			value = ApplicationConfigUtil.getApplicationConfigCache()
					.getValue(new FDPAppBag(AppCacheSubStore.CONFIGURATION_MAP, "VENDOR_ID")) + FDPConstant.UNDERSCORE
					+ parameterValue.toString();
			break;
		case PRODUCT_CHARGING_VALUE:
			final FDPCacheable chargingAmount = fdpRequest.getValueFromRequest(RequestMetaValuesKey.CHARGING_STEP);
			value = 0;
			ExternalSystem externalSystem = null;
			if (chargingAmount instanceof ChargingValue) {
				final ChargingValue chargingVal = (ChargingValue) chargingAmount;
				externalSystem = chargingVal.getExternalSystemToUse();
				value = getValueForExternalSystem(externalSystem, chargingVal.getChargingValue(),
						fdpRequest.getCircle());
			}
			break;
		case PRODUCT_CHARGING_VALUE_DEC:
			final Float floatValue = Float.valueOf(
					((Long) ClassUtil.getPrimitiveValue(evaluateFunction(Function.PRODUCT_CHARGING_VALUE, fdpRequest),
							Long.class)).toString());
			value = floatValue / 100;
			break;
		case PRODUCT_CHARGING_VALUE_DEC_EXTDATA2:
			Long longValueExData2 = Long.valueOf(
					((Long) ClassUtil.getPrimitiveValue(evaluateFunction(Function.PRODUCT_CHARGING_VALUE, fdpRequest),
							Long.class)).toString());
			longValueExData2 = (longValueExData2 < 0) ? -longValueExData2 : longValueExData2;
			value = longValueExData2 / 100;
			break;
		case REFILLID_3G:
			value = getValueFromCache(fdpRequest, FDPConstant.REFILL_PROFILE_ID_THREEG);
			break;
		case VOUCHERID_3G:
			value = getValueFromCache(fdpRequest, FDPConstant.VOUCHER_GROUP_ID_THREEG);
			break;
		case REFILLID_2G:
			value = getValueFromCache(fdpRequest, FDPConstant.REFILL_PROFILE_ID_TWO_G);
			break;
		case VOUCHERID_2G:
			value = getValueFromCache(fdpRequest, FDPConstant.VOUCHER_GROUP_ID_TWO_G);
			break;
		case MCARBON_VALUE:
			value = getMcarbonValueFromCache(fdpRequest);
			break;
		case RENEWAL_COUNT:
			value = getRenewalCount(fdpRequest);
			break;
		case ORIGIN_HOST_NAME:
			value = getOriginHostName(fdpRequest);
			break;
		case GET_MANHATTAN_CIRCLEID:
			value = getManhattanCircleId(fdpRequest);
			break;
		case GET_SYSTEM_IP:
			value = getSystemIp();
			break;
		case EXTERNAL_DATA_ABSAMT:
			value = getExtrenalDataFieldValue(AuxRequestParam.EXTERNAL_DATA_VALUE_FOR_EXTRA_ABSAMT, fdpRequest);
			break;
		case EXTERNAL_DATA_PERCAMT:
			value = getExtrenalDataFieldValue(AuxRequestParam.EXTERNAL_DATA_VALUE_FOR_EXTRA_PERCAMT, fdpRequest);
			break;
		case EXTERNAL_DATA_LOAN_PURCHASE_INFO:
			value = getExtrenalDataFieldValue(AuxRequestParam.MC_LOAN_PURCHASE_INPUT, fdpRequest);
			break;
		case SUBSCRIBER_CIRCLE_CODE:
		case GET_CIS_CIRCLE_CODE:
			value = getFDPCircleCode(fdpRequest);
			break;
		case GET_CIS_PRODUCT_CODE:
			value = getFDPProductCode(fdpRequest);
			break;
		case SERVICE_ID_EMA:
			value = getEMAServiceID(fdpRequest);
			break;
		case PROVISION_ACTION_EMA:
			value = getEMAProvisionAction(fdpRequest);
			break;
		case GET_CIS_EXPIRE_DATE:
			value = getExpiryDateRS(fdpRequest);
			break;
		case GET_CIS_PRODUCT_TYPE:
			value = getFDPProductTypeForRS(fdpRequest);
			break;

		case GET_CIS_PRODUCT_EXPIRY:
			value = getFDPProductExpiryForRS(fdpRequest);
			break;
			
		case GET_CIS_EXPIRY_ONE_DAY_MINUS:
			value = getFDPProductExpiryForRSOneDayExtra(fdpRequest);
			break;
			
		case GET_CIS_PROVISION_TYPE:
			value = getFDPProductProvisionType(fdpRequest);
			break;
		case GET_CIS_SERVICE_NAME:
			/* this is description of the product */
			value = getFDPServiceName(fdpRequest);
			break;
		case GET_PRODUCT_DESCRIPTION:
			value = getFDPProductDescription(fdpRequest);
			break;
		case PROVIDER_FRI_NUMBER:
			value = getProviderNumberFRI();
			break;
		case GET_EXPIRY_BITS:
			value = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.EXPIRED_BITS);
			if (value == null)
				value = "0";
			break;
		case RENEWAL_VALUE:
			value = getRenewalValue(fdpRequest);
			break;
		case IS_SPLIT:
			value = getIsSplit(fdpRequest);
			break;
		case RECURRING_GRACE_PERIOD:
			value = getRecurringGracePeriod(fdpRequest);
			break;
		case RETRY_LIMIT:
			value = getRetryLimit(fdpRequest);
			break;
		case EXPIRY_NOTIFICATION_FLAG:
			value = getExpiryNotificationFlag(fdpRequest);
			break;
		case SUBSCRIBER_FRI_NUMBER:
			value = getSubscriberFriNumber(fdpRequest);
			break;
		case PRODUCTID:
			value = getFDPProductId(fdpRequest);
			break;

		case PAYSRC:
			value = getPaysrc(fdpRequest);
			break;

		case BEN_MSISDN:
			value = getBenMsisdn(fdpRequest);
			break;

		case SEND_SMS:
			value = sendSMS(fdpRequest);
			break;

		case ACTION:
			value = getAction(fdpRequest);
			break;
		case PAM_ID:
			value = getPamId(fdpRequest);
			break;
		case OPTIN_OPTOUT_ACTION:
			final FDPServiceProvSubType fdpServiceProvSubType = RequestUtil.getFDPServiceProvSubType(fdpRequest);
			switch (fdpServiceProvSubType) {
			// OPTIN -- > Normal(Adhoc) to Renewal.
			// Normal Product for Product Purchase, then PRODUCT_BUY_RECURRING
			// SP with SingleProvRequest to OPTOUT.
			case OPT_IN_SERVICE:
				value = FDPConstant.OPT_IN;
				break;
			// OPTOUT --> Renewal to Normal(Adhoc).
			case OPT_OUT_SERVICE:
				value = FDPConstant.OPT_OUT;
				break;
			default:
				value = "";
			}
			break;
		case SPLIT_NO:
			value = getSplitNo(fdpRequest);
			break;

		case PRODUCT_COST:
			value = getProductCost(fdpRequest);
			break;
		case MM_TRANSACTION_ID:
			value = getTransactionID(fdpRequest);
			break;
		case RIMSUB_HLR_ID:
			value = getImsiID(fdpRequest);
			break;
		case BEN_MSISDN_EMA:
			value = getBeneficiaryMsisdn(fdpRequest);
			break;
		case EVDS_TRANS_TYPE_ID:
			// EVDS transaction code for money debit
			value = "113";
			break;
		case EVDS_COMMENTS:
			// EVDS comment for money debit
			value = "Adjustment for Bundle Purchase";
			break;
		case PAM_ERROR_MSG:
			value = ApplicationCacheUtil.getValueFromApplicationCache(AppCacheSubStore.CONFIGURATION_MAP,
					"PAM_ERROR_MSG");
			break;
		case SRC_CHANNEL:
			value = getSrcChannel(fdpRequest);
			break;

		case GET_SRC_PAY_CHL:
			value = getChannelAndPaySrc(fdpRequest);
			break;

		case PCRF_TRANSACTIONID:
			value = getTransactionID(fdpRequest);
			break;

		case PCRF_START_DATE:
			value = getPCRFStartDDate(fdpRequest);
			break;

		case PCRF_END_DATE:
			value = getPCRFENDDDate(fdpRequest);
			break;

		case BALANCE_CHECK:
			value = (String) fdpRequest.getIncomingSubscriberNumber().toString();
			break;

		case FAF_NUMBER:
			value = getFaFNumber(fdpRequest, this.getCommand());
			break;

		case FAF_CHARGING_INDICATOR:
			value = getFaFChargingIndicator(fdpRequest);
			break;

		case INTERNATIONAL:
			value = getFAFIndicatorInternational(fdpRequest);
			break;

		case OFFNET:
			value = getFAFIndicatorOffnet(fdpRequest);
			break;

		case ONNET:
			value = getFAFIndicatorOnnet(fdpRequest);
			break;

		case ME2U_DA2DEBIT_ID:
			value = getDaIdToDebitFrom(fdpRequest);
			break;

		case ME2U_DA2CREDIT_ID:
			value = getDaIdToCreditTo(fdpRequest);
			break;

		case ME2U_AMTTRANSFER_DATA2SHARE:
			value = getTMAmtToTransfer(fdpRequest, definedValueToUse);
			// value = Me2uUtil.convertNgweeToCSAmt(fdpRequest, value);
			break;
		case ME2U_AMT2TRANSFER:
		case ME2U_MAIN2DEBIT_AMT:
		case ME2U_MAIN2CREDIT_AMT:
		case ME2U_DA2DEBIT_AMT:
		case ME2U_DA2CREDIT_AMT:
			value = getTMAmtToTransfer(fdpRequest, definedValueToUse);
	//		value = Me2uUtil.convertNgweeToCSAmt(fdpRequest, value);
			break;

		case ME2U_MAIN2DEBIT_AMT_TRANS:
		case ME2U_DA2DEBIT_AMT_TRANS:
		case ME2U_MAIN2CREDIT_AMT_TRANS:
		case ME2U_DA2CREDIT_AMT_TRANS:
			Long amtToTransfer = getTMAmtToTransfer(fdpRequest, definedValueToUse);
			value = amtToTransfer != 0L ? getTMAmtAfterTrans(fdpRequest, amtToTransfer) : amtToTransfer;
			value = Me2uUtil.convertNgweeToCSAmt(fdpRequest, value);

			break;

		case ME2U_AMT2TRANSFER_IN_KWACHA:
			Object amtToShareObj = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER);
			value = Me2uUtil.getAmtNgweeTokwacha(fdpRequest, Long.parseLong(amtToShareObj.toString()));
			break;

		case PAM_ID_ZM:
			value = getPamIdZM(fdpRequest);
			break;

		case OFFER_ID_ZM:
			value = getOfferIdZM(fdpRequest);
			break;

		case BUNDLE_NAME:
			value = getBundleName(fdpRequest);
			break;

		case PRODUCT_CATEGORY:
			value = getProductCategory(fdpRequest);
			break;

		case USAGE_PERIOD:
			value = getUsagePeriod(fdpRequest);
			break;

		case GET_FAF_LIST:
			value = getFafList(fdpRequest);
			break;
		case FAF_OFFER_ID:
			value = getFafOfferId(fdpRequest);
			break;

		case FAF_MSISDN_ADD:
			value = getFafMsisdnAddOrDelete(fdpRequest);
			break;

		case FAF_MSISDN_MODIFY_DELETE:
			value = getFafMsisdnModifyDelete(fdpRequest);
			break;

		case SUBSCRIBER_NUMBER:
			value = getSubscriberNumberToGAD(fdpRequest);
			break;
		case GET_DATA2SHARE_EXPIRY:
			Calendar calendar = Calendar.getInstance();
			calendar.add(Calendar.DATE, 7); // Adding 7 days
			String valueP = DateUtil.convertCalendarDateToString(calendar, FDPConstant.DATE_PATTERN_WITH_FULL_TIME);
			try {
				value = DateUtil.getFdpFormatDateWithHoursMinutesANDSeconds(valueP);
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case Get_DATA_AMT:
			value = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER);
			break;

		case GET_RECEIVER_NUMBER:
			value = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_SUBSCRIBER_TO_CREDIT);
			break;

		case GET_SENDER_NUMBER:
			value = fdpRequest.getIncomingSubscriberNumber();
			break;
		case GET_FDP_CIRCLE_CODE:
			value = getFDPCircleCode(fdpRequest);
			break;
		case GET_SBB_ACTION:
			value = getSbbAction(fdpRequest);
			break;
		case GET_SBB_DEL_ALLOWED:
			value = getSbbDelAction(fdpRequest);
			break;
		case GET_BENEFICIARY_MSISDN:
			value = getBeneficiaryMsisdn(fdpRequest);
			break;
		case GET_HANDSET_BASED_OFFER_ID:
			value = getOfferIdFromIMEI(fdpRequest);
			FDPLogger.debug(logger, getClass(), "GET_HANDSET_BASED_OFFER_ID",
					"offer id from GET_HANDSET_BASED_OFFER_ID " + value);
			break;
		case GET_SUBSCRIBER_FRI_NUMBER:
			value = getSubscriberFRINumber(fdpRequest);
			break;

		case GET_CIS_ACTION_TYPE: // commandParamInput
			value = getCisActionType(fdpRequest);
			break;
		case GET_ORIGIN_OPERATOR_ID:
			value = FDPConstant.ARRAY_CHILD_SEQUENCE_PATH_SYMBOL + fdpRequest.getChannel().toString() + FDPConstant.ZERO
					+ FDPConstant.ZERO + FDPConstant.ZERO + FDPConstant.ARRAY_CHILD_SEQUENCE_PATH_SYMBOL
					+ getPaysrc(fdpRequest);
			break;

		case FROM_DATE_MAGIC_NUMBER:
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
				DateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy");
				value = fmt.format(new Date());
				break;
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case TO_DATE_MAGIC_NUMBER:
			try {
				Calendar cal = Calendar.getInstance();
				cal.setTime(new Date());
				cal.add(Calendar.DAY_OF_YEAR, 180);
				DateFormat fmt = new SimpleDateFormat("MMMM dd, yyyy");
				value = fmt.format(cal.getTime());
				/* value=DateUtil.getDateInGUIFormatDate(180); */
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			break;

		case SIM_LANGUAGE_ID:
			value = getSimLanguageId(fdpRequest);
			break;
		case CHARGE_AMOUNT:
			value = getChargingAmount(fdpRequest);
			break;

		case RENEWAL_DATE:
			value = getRenewalDate(fdpRequest);
			break;

		case GET_CIS_CUM_PRODUCT_CODE:
			String currentProductId = null;
			ProductAttributeMapCacheDTO fdpCacheableObject = null;
			String dependentProductID = null;
			String productIds[] = null;
			currentProductId = getFDPProductCode(fdpRequest);
			StringBuilder key = new StringBuilder(FDPConstant.DEPENDENT_SCENARIO);
			key.append(FDPConstant.UNDERSCORE).append(FDPConstant.CURRENT_PRODUCTID).append(FDPConstant.UNDERSCORE)
					.append(currentProductId);
			//System.out.println("Key is:" + key);
			try {
				fdpCacheableObject = (ProductAttributeMapCacheDTO) ApplicationConfigUtil.getMetaDataCache()
						.getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.PRODUCT_ATTRIBUTE_MAP, key));
			} catch (ExecutionFailedException e) {
				FDPLogger.info(logger, CommandParamInput.class, "evaluateFunction()",
						"Error occured while fetching from cache, key " + key);
			}

			if (null != fdpCacheableObject) {
				for (Map.Entry<Long, Map<String, String>> entry : fdpCacheableObject.getValueMap().entrySet()) {
					Map<String, String> valueMap = entry.getValue();
					String dependentProductIds = valueMap.get(FDPConstant.DEPENDENT_GROUP_PRODUCTID);
					// System.out.println("Product Ids:" + dependentProductIds);
					if (null != dependentProductIds && !dependentProductIds.isEmpty())
						productIds = dependentProductIds.split("\\s+");
				}
			}

			FDPCommand fdpCommand = fdpRequest
					.getExecutedCommand(Command.GET_SERVICES_CUM_DETAILS_REQUEST.getCommandDisplayName());

			if (null == fdpCommand) {
				final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache()
						.getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND,
								Command.GET_SERVICES_CUM_DETAILS_REQUEST.getCommandDisplayName()));
				if (fdpCommandCached instanceof FDPCommand) {
					fdpCommand = (FDPCommand) fdpCommandCached;
					fdpCommand = CommandUtil.getExectuableFDPCommand(fdpCommand);
				} else {
					throw new EvaluationFailedException("Could not evaluate value");
				}

				fdpCommand.execute(fdpRequest);
				fdpRequest.addExecutedCommand(fdpCommand);
			}
			List<String> rsProductIds = parseCommandResponse(fdpRequest, fdpCommand);

			// System.out.println("ProductIDs in Response:" + rsProductIds +
			// "size of list " + rsProductIds.size());
			if (null != productIds) {
				for (String productID : productIds) {
					if (rsProductIds.contains(productID)) {
						dependentProductID = productID;
						// System.out.println("Dependent Product id is:" +
						// productID);
						break;
					}

				}
			}

			if (null != dependentProductID && !dependentProductID.isEmpty()) {
				value = dependentProductID;
			} else {
				value = currentProductId;
			}
			// System.out.println("Value is :" + value);
			break;

		case GET_CIS_CUM_PRODUCT_EXPIRY:
			value = getFDPCumProductExpiryForRS(fdpRequest);
			break;
		case GET_HANDSET_ATTRIBUTE_NAME:
			value = AuxRequestParam.IMEI.name();
			break;
		case GET_HANDSET_ATTRIBUTE_VALUE:
			setDeviceBasedValuesInRequest(fdpRequest);
			value = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.IMEI);
			break;
		case GET_DA_START_DATE:
			value = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.DA_START_DATE);
			break;
		case GET_DA_EXPIRY_DATE:
			value = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.DA_EXPIRY_DATE);
			break;
		case GET_OFFER_START_DATE:
			value = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OFFER_START_DATE);
			break;
		case GET_OFFER_EXPIRY_DATE:
			value = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OFFER_EXPIRY_DATE);
			break;
		// Added by eagarsh
		case WITH_CC_ADD_MAGICNO:
			value = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.WITH_CC_ADD_MAGICNO);
			break;

		case WITHOUT_CC_ADD_MAGICNO:
			value = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.WITHOUT_CC_ADD_MAGICNO);
			break;

		case WITH_ZERO_ADD_MAGICNO:
			value = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.WITH_ZERO_ADD_MAGICNO);
			break;

		case ACTIVATION_START_DATE:
			value = getActivationStartDate(fdpRequest);
			break;

		case ACTIVATION_END_DATE:
			String activationStartDate = getActivationStartDate(fdpRequest).toString();
			try {
				final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
				String activationEndDateInterval = configurationMap
						.get(ConfigurationKey.ACTIVATION_ENDDATE_INTERVAL.getAttributeName());
				value = DateUtil.getNextIntervalDate(FDPConstant.FDP_DB_SAVE_DATE_PATTERN, activationStartDate,
						Integer.parseInt(activationEndDateInterval));
			} catch (ParseException e) {
				FDPLogger.error(logger, getClass(), LoggerUtil.getRequestAppender(fdpRequest),
						"Error while adding interval date to activationDate :" + e);
			}
			break;


		case ORIGIN_OPERATOR_ID:
			value = getOriginOperatorId(fdpRequest);
			break;

		case LOYALTY_ITEM_CODE:
			value = getLoyaltyItemCode(fdpRequest); 
			break;

		case MSISDN_WITHOUT_COUNTRY_CODE:
			value = getMSISDNWithoutCountryCode(fdpRequest); 
			break;
			
		case LOYALTY_POINTS:
			value = getLoyaltyPoints(fdpRequest); 
			break;
			
		case BUNDLE_FLAG:
			value = getBundleFlag(fdpRequest);
			break;
		case GET_CIS_PRODUCT_NAME:
			value = getCisProductName(fdpRequest);
			break;
		case VIEW_PRODUCT_TYPE:
			value = getViewProductType(fdpRequest);
			break;
		case GET_PIN:
			value = getMe2uPin(fdpRequest);
			break;
		case SHORT_CODE_OR_CHANNEL:
			value = getShortCodeOrChannel(fdpRequest);
			break;
		default:
			break;
		}
		return value;
	}
	
	/**
	 * The method is use to fetch Item Code from product cache
	 * 
	 * @param fdpRequest
	 * @return
	 */

	private Object getLoyaltyPoints(FDPRequest fdpRequest) throws ExecutionFailedException {
		return ((Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT)).getLoyaltyPoints();
	}
	
	/**
	 * The getBundleFlag return ProductId for MPR, Request comes 1st for User consent and 2nd time return Product Bundle Value
	 * @param fdpRequest
	 * @return
	 */
	private Object getBundleFlag(FDPRequest fdpRequest){
	//	String subscriberAutoRenewalInput = ((FDPSMPPRequestImpl)fdpRequest).getRequestString();
		//String autoRenewalConfimationInput = RequestUtil.getConfigurationKeyValue(fdpRequest, ConfigurationKey.AUTO_RENEWAL_CONFIRMATION);
		if(ChannelType.USSD.equals(fdpRequest.getChannel()) && null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.AUTO_RENEWAL_SESSION_TERMINATE) && 
				FDPConstant.TRUE.equals(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.AUTO_RENEWAL_SESSION_TERMINATE).toString())){
			return ((Product)fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT)).getAdditionalInfo(ProductAdditionalInfoEnum.BUNDLE_VALUE);
		}
			
		else
		return ((Product)fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT)).getProductId();
		
	}
	
	/**
	 * The method is use to fetch subscriber number from fdpRequest
	 * 
	 * @param fdpRequest
	 * @return
	 */
	
	private String getMSISDNWithoutCountryCode(FDPRequest fdpRequest){
		return fdpRequest.getSubscriberNumber().toString().replaceFirst(PropertyUtils.getProperty("COUNTRY_CODE"), "");
	}
	/**
	 * The method is use to fetch Item Code from product cache
	 * 
	 * @param fdpRequest
	 * @return
	 */

	private Object getLoyaltyItemCode(FDPRequest fdpRequest) throws ExecutionFailedException {
		return ((Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT)).getLoyaltyItemCode();
	}

	/**
	 * The method is use to fetch activationDate from GAD command/
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private Object getActivationStartDate(FDPRequest fdpRequest) {
		String strActivationDate = null;
		FDPCommand fdpCommandOutput = fdpRequest.getExecutedCommand(Command.GETACCOUNTDETAILS.getCommandDisplayName());
		if (null != fdpCommandOutput && null != fdpCommandOutput.getOutputParams()) {
			strActivationDate = DateUtil.gregorianCalendarToSimpleDateConvertoer(
					((GregorianCalendar) fdpCommandOutput.getOutputParam(FDPConstant.ACTIVATION_DATE).getValue())
							.getTime().getTime(),
					FDPConstant.FDP_DB_SAVE_DATE_PATTERN);
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.ACTIVATION_DATE,
					strActivationDate);
		}

		return strActivationDate;

	}

	/**
	 * This method will calulcate the action param in sbb add subscriber
	 * details.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private String getSbbAction(final FDPRequest fdpRequest) {
		final String actionvalue = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ACTION).toString();
		if (fdpRequest instanceof FDPRequestImpl) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.ACTION, null);
		}
		return (null == actionvalue) ? FDPConstant.SHARED_BONUS_BUNDLE_PROVIDER_TYPE.get(1) : actionvalue;
	}

	/**
	 * This method will calculate the deleteAllowed Param in Get_Details command
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private String getSbbDelAction(final FDPRequest fdpRequest) {
		boolean isSbbDelAction = false;
		final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
		if (null != fdpNode && fdpNode instanceof SpecialMenuNode) {
			final SpecialMenuNode specialMenuNode = (SpecialMenuNode) fdpNode;
			isSbbDelAction = FDPServiceProvSubType.SHARED_BUNDLE_DELETE_CONSUMER
					.equals(specialMenuNode.getSpecialMenuNodeType());
			if (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SBB_DELETE_ALLOWED)
					&& fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SBB_DELETE_ALLOWED) instanceof Boolean) {
				isSbbDelAction = (Boolean) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SBB_DELETE_ALLOWED);
			}
		}
		return isSbbDelAction ? Boolean.TRUE.toString().toUpperCase() : Boolean.FALSE.toString().toUpperCase();
	}

	private Object getFafOfferId(FDPRequest fdpRequest) throws ExecutionFailedException {
		FnfUtil fnfUtilObj = new FnfUtil();
		Integer fnfOfferId = fnfUtilObj.getFafOfferIdForUpdateOffer(fdpRequest);
		return fnfOfferId;
	}

	/**
	 * This method is use to set FAF msisdn for UpdateFAFList command for action
	 * add and delete
	 **/
	private Object getFafMsisdnAddOrDelete(FDPRequest fdpRequest) {
		Object normalizedMsisdn = null;
		Object fafMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD);
		normalizedMsisdn = normalizaFafMsisdn(fafMsisdn);
		return normalizedMsisdn;
	}

	/**
	 * This method is use to set FAF msisdn for UpdateFAFList command for Modify
	 * to delete faf action.
	 **/
	private Object getFafMsisdnModifyDelete(FDPRequest fdpRequest) {
		Object normalizedMsisdn = null;
		Object fafMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE);
		normalizedMsisdn = normalizaFafMsisdn(fafMsisdn);
		return normalizedMsisdn;
	}

	private Object getFafList(FDPRequest fdpRequest) {
		int i = 0;
		final String parameterName = "fafInformationList";
		final String fafNumber = "fafNumber";
		String fafNumberList = new String();
		FDPCommand executedCommand = fdpRequest.getExecutedCommand(Command.GET_FAF_LIST.getCommandDisplayName());
		while (null != fdpRequest.getExecutedCommand(Command.GET_FAF_LIST.getCommandDisplayName()).getOutputParam(
				parameterName + FDPConstant.PARAMETER_SEPARATOR + i + FDPConstant.PARAMETER_SEPARATOR + fafNumber)) {
			if (null != executedCommand.getOutputParam(
					parameterName + FDPConstant.PARAMETER_SEPARATOR + i + FDPConstant.PARAMETER_SEPARATOR + fafNumber)
					.getValue()) {
				fafNumberList = fafNumberList + FDPConstant.COMMA
						+ executedCommand.getOutputParam(parameterName + FDPConstant.PARAMETER_SEPARATOR + i
								+ FDPConstant.PARAMETER_SEPARATOR + fafNumber).getValue().toString();
				i++;
			}
		}
		if (StringUtil.isNullOrEmpty(fafNumberList)) {
			fafNumberList = FDPConstant.FAF_LIST_EMPTY;
		}
		fafNumberList = fafNumberList.endsWith(FDPConstant.COMMA)
				? fafNumberList.substring(0, fafNumberList.lastIndexOf(FDPConstant.COMMA)) : fafNumberList;
		fafNumberList = fafNumberList.startsWith(FDPConstant.COMMA) ? fafNumberList.substring(1, fafNumberList.length())
				: fafNumberList;
		return fafNumberList;
	}

	/**
	 * This method returns the DA id to credit to for time2share
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private Object getDaIdToCreditTo(FDPRequest fdpRequest) {
		return fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_TO);
	}

	/**
	 * This method returns the DA id to debit from for time2share
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private Object getDaIdToDebitFrom(FDPRequest fdpRequest) {
		return fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_FROM);
	}

	/**
	 * This method returns the amount that needs to be debited/credited for
	 * sender/recipient in time2share use case
	 * 
	 * @param fdpRequest
	 * @param time2shareTransType
	 * @return
	 */
	private Long getTMAmtToTransfer(FDPRequest fdpRequest, Function time2shareTransType) {
		Long amtToTransfer = 0l;
		Object amtToShareObj = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_TRANSFER);
		Object amtToRecieveObj = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_AMT_TO_BE_RECIEVED);
		Object accToShareFrom = fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_FROM);
		Object accToShareTo = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TIME2SHARE_ACCOUNT_TO_SHARE_TO);
		Long amtToShare = 0l;
		Long amtToRecieve = 0l;
		Integer accToShareFromValue = 0;
		Integer accToShareToValue = 0;
		if (null != amtToShareObj) {
			amtToShare = Long.parseLong(amtToShareObj.toString());
		}
		if (null != amtToRecieveObj) {
			amtToRecieve = Long.parseLong(amtToRecieveObj.toString());
		}
		if (null != accToShareFrom) {
			accToShareFromValue = Integer.parseInt(accToShareFrom.toString());
		}
		if (null != accToShareTo)
			accToShareToValue = Integer.parseInt(accToShareTo.toString());

		switch (time2shareTransType) {
		case ME2U_AMTTRANSFER_DATA2SHARE:
			final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
			String amountConversionFactor = configurationMap
					.get(ConfigurationKey.Me2U_DATA2SHARE_AMOUNT_CONVERSION_FACTOR.getAttributeName());
			if (null == amountConversionFactor)
				amountConversionFactor = "0";
			amtToTransfer = amtToShare * Long.parseLong(amountConversionFactor);
			break;
		case ME2U_AMT2TRANSFER:
			amtToTransfer = amtToShare;
			break;

		case ME2U_MAIN2DEBIT_AMT:
		case ME2U_MAIN2DEBIT_AMT_TRANS:
			amtToTransfer = accToShareFromValue == 0 ? (-1) * amtToShare : 0l;
			break;

		case ME2U_MAIN2CREDIT_AMT:
		case ME2U_MAIN2CREDIT_AMT_TRANS:
			amtToTransfer = accToShareToValue == 0 ? amtToRecieve : 0l;
			break;

		case ME2U_DA2DEBIT_AMT:
		case ME2U_DA2DEBIT_AMT_TRANS:
			amtToTransfer = accToShareFromValue != 0 ? (-1) * amtToShare : 0l;
			break;

		case ME2U_DA2CREDIT_AMT:
		case ME2U_DA2CREDIT_AMT_TRANS:
			amtToTransfer = accToShareToValue != 0 ? amtToRecieve : 0l;
			break;

		default:
			break;
		}

		return amtToTransfer;
	}

	/**
	 * This method returns the amount of transaction charges applicable for Me2u
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String getTMAmtAfterTrans(final FDPRequest fdpRequest, Long amtToTransfer) throws ExecutionFailedException {
		Long transCharges = Me2uUtil.getTransCharges(fdpRequest, amtToTransfer);
		amtToTransfer += (-1) * transCharges;
		return amtToTransfer.toString();
	}

	/**
	 * Gets the channel from fdp request.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private Object getSrcChannel(FDPRequest fdpRequest) {
		return fdpRequest.getChannel();
	}

	private Object getFaFNumber(FDPRequest fdpRequest, FDPCommand fdpCommand) {
		

		if (fdpCommand.getCommandDisplayName().equals(Command.GETCISDETAILS.getCommandName())) {

			if (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER)) {
				FDPServiceProvSubType fdpServiceProvSubType = ((ServiceProvProductDTO) ((ServiceProvisioningRule) fdpRequest
						.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING)).getServiceProvDTO()).getSpSubType();
				// For modify delete faf NO details send first
				if (FDPServiceProvSubType.FAF_MODIFY.equals(fdpServiceProvSubType)) {
					// GETCISDEtails to delete faf for modify.
					if (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER_GAD)) {
						Object fafNumberGad = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER_GAD);
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER_GAD,
								null);
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER,
								fafNumberGad);
						return fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
					} else {
						// GETCISDetails to add faf for modify
						Object fafMsisdnAdd = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD);
						((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER,
								fafMsisdnAdd);
						return fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
					}

				} else {
					return fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
				}

			} else {
				return fdpRequest.getSubscriberNumber();
			}
		}
		return fdpRequest.getSubscriberNumber();
		/*
		 * FDPServiceProvSubType fdpServiceProvSubType =
		 * ((ServiceProvProductDTO) ((ServiceProvisioningRule) fdpRequest
		 * .getValueFromRequest
		 * (RequestMetaValuesKey.SERVICE_PROVISIONING)).getServiceProvDTO
		 * ()).getSpSubType(); if (null !=
		 * fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER))
		 * {
		 * 
		 * if (FDPServiceProvSubType.FAF_MODIFY.equals(fdpServiceProvSubType)) {
		 * return getFafNumberToModify(fdpRequest); } else { Object fafMsisdn =
		 * fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
		 * Object normalizedMsisdn = normalizaFafMsisdn(fafMsisdn);
		 * ((FDPRequestImpl) fdpRequest)
		 * .putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER,
		 * normalizedMsisdn); return
		 * fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
		 * } //return fdpRequest.getSubscriberNumber(); } else { return
		 * fdpRequest.getSubscriberNumber(); }
		 */}

	private Object getSubscriberNumberToGAD(FDPRequest fdpRequest) {
		/*
		 * if(fdpCommand.getCommandDisplayName().equals(Command.GETCISDETAILS.
		 * getCommandName())){ if(null !=
		 * fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER)){
		 * return
		 * fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
		 * }else{ return fdpRequest.getSubscriberNumber(); } }
		 */
		FDPServiceProvSubType fdpServiceProvSubType = null;
		if (fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE) instanceof FDPNode) {
			final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
			if (fdpNode instanceof ProductNode) {
				final ProductNode productNode = (ProductNode) fdpNode;
				fdpServiceProvSubType = productNode.getServiceProvSubType();
			}
		}
		/*
		 * FDPServiceProvSubType fdpServiceProvSubType =
		 * ((ServiceProvProductDTO) ((ServiceProvisioningRule) fdpRequest
		 * .getValueFromRequest
		 * (RequestMetaValuesKey.SERVICE_PROVISIONING)).getServiceProvDTO
		 * ()).getSpSubType();
		 */
		if (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER)) {
			if (FDPServiceProvSubType.FAF_DELETE.equals(fdpServiceProvSubType)) {
				Object deleteFafMsisdnobj = fdpRequest
						.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE_INTERNATIONAL);
				if (null != deleteFafMsisdnobj && "1".equals(deleteFafMsisdnobj.toString())) {
					return fdpRequest.getSubscriberNumber();
				}

			}

			if (FDPServiceProvSubType.FAF_MODIFY.equals(fdpServiceProvSubType)) {

				// GETCISDEtails to delete faf for modify.
				if (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE)) {
					Object fafMsisdnDelete = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE);
					Object normalizedMsisdn = normalizaFafMsisdn(fafMsisdnDelete);
					return normalizedMsisdn;
				} else {
					// GETCISDetails to add faf for modify
					Object fafMsisdnAdd = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD);
					Object normalizedMsisdn = normalizaFafMsisdn(fafMsisdnAdd);
					return normalizedMsisdn;
				}
			} else if (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SUBSCRIBER_NUMBER)) {
				Object subscriberNo = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SUBSCRIBER_NUMBER);
				Object normalizedsubscriberNo = normalizaFafMsisdn(subscriberNo);
				((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.SUBSCRIBER_NUMBER, null);
				return normalizedsubscriberNo;
			}

			else {
				Object fafMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
				Object normalizedMsisdn = normalizaFafMsisdn(fafMsisdn);
				((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER,
						normalizedMsisdn);
				return fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
			}
		} else {
			return fdpRequest.getSubscriberNumber();
		}

	}

	private Object getFafNumberToModify(FDPRequest fdpRequest) {
		Object fafMsisdn = null;
		Object normalizedMsisdn = null;
		if (fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE) != null) {
			fafMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE);
			normalizedMsisdn = normalizaFafMsisdn(fafMsisdn);
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER, normalizedMsisdn);
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_DELETE, null);
		} else {
			fafMsisdn = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_MSISDN_ADD);
			normalizedMsisdn = normalizaFafMsisdn(fafMsisdn);
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER, normalizedMsisdn);
		}
		return fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.FAF_NUMBER);
	}

	/*
	 * Normalized faf no. Remove 00 for international and CC for Onnet, Offnet
	 * number.
	 */
	private Object normalizaFafMsisdn(Object fafMsisdn) {
		Object normalizeMsisdn = null;
		if (fafMsisdn.toString().startsWith(FDPConstant.FAF_INTERNATIONAL_PREFIX)) {
			normalizeMsisdn = fafMsisdn.toString().substring(2);
		} else {
			normalizeMsisdn = FnfUtil.normalizeFafMsisdnForCommand(fafMsisdn.toString());
		}
		return normalizeMsisdn;
	}

	private Object getFaFChargingIndicator(FDPRequest fdpRequest) {
		Map<String, String> configurationKeyValueMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return configurationKeyValueMap.get(ConfigurationKey.FAF_CHARGING_INDICATOR.getAttributeName());
	}

	private Object getFAFIndicatorInternational(FDPRequest fdpRequest) {
		Map<String, String> configurationKeyValueMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return configurationKeyValueMap.get(ConfigurationKey.FAF_INDICATOR_INTERNATIONAL.getAttributeName());
	}

	private Object getFAFIndicatorOnnet(FDPRequest fdpRequest) {
		Map<String, String> configurationKeyValueMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return configurationKeyValueMap.get(ConfigurationKey.FAF_INDICATOR_ONNET.getAttributeName());
	}

	private Object getFAFIndicatorOffnet(FDPRequest fdpRequest) {
		Map<String, String> configurationKeyValueMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return configurationKeyValueMap.get(ConfigurationKey.FAF_INDICATOR_OFFNET.getAttributeName());
	}

	private Object getTransactionID(FDPRequest fdpRequest) {

		return fdpRequest.getRequestId().replaceAll("\\" + FDPConstant.DOT, "");
	}

	/**
	 * From FRI for MM Note we have to remove the hardcoded MSISDN
	 * 
	 */
	private String getSubscriberFriNumber(final FDPRequest fdpRequest) {

		// FRI:919910117560/MSISDN
		return "FRI" + FDPConstant.COLON + fdpRequest.getSubscriberNumber() + "/" + "MSISDN";

		// old hardcoded FRI:919910117560/MSISDN
		// return "FRI:919899923705/MSISDN";
	}

	private String getFDPProductDescription(FDPRequest fdpRequest) {
		final Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (product.getProductDescription() != null) {
			return product.getProductDescription();
		}
		return "";
	}

	private Object getProviderNumberFRI() throws ExecutionFailedException {
		return ApplicationCacheUtil.getValueFromApplicationCache(AppCacheSubStore.CONFIGURATION_MAP,
				"MOBILE_MONEY_FRI");
	}

	private Object getExpiryDateRS(FDPRequest fdpRequest) {
		DateUtil.convertCalendarDateToString(Calendar.getInstance(), "YYYY-MM-DD HH:mm:ss");
		final Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);

		return product;
	}

	private Object getFDPServiceName(FDPRequest fdpRequest) {
		final Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		/*
		 * if(product.getProductDescription()!=null) { return
		 * product.getProductDescription(); }
		 */
		return product.getProductName();
	}

	private Object getFDPProductProvisionType(FDPRequest fdpRequest) {
		final Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		final ServiceProvisioningRule serviceProvisionRule = (ServiceProvisioningRule) fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);
		// change by eagarsh
		String actionName = null;
		if (serviceProvisionRule.getServiceProvDTO() instanceof AbstractServiceProvDTO) {
			AbstractServiceProvDTO abstractServiceProvDTO = (AbstractServiceProvDTO) serviceProvisionRule
					.getServiceProvDTO();
			FDPLogger.debug(logger, getClass(), "getFDPProductProvisionType()",
					LoggerUtil.getRequestAppender(fdpRequest) + "ActionName "
							+ abstractServiceProvDTO.getSpSubType().name());
			actionName = abstractServiceProvDTO.getSpSubType().name();
		}
		if (null == fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.TYPE))
			((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.TYPE,
					FDPConstant.NORMAL_PRODUCT_TYPE);
		if (product.getRenewalCount() != 0) {
			if (product.getAdditionalInfo(ProductAdditionalInfoEnum.RECURRING_PAM_ID) != null) {
				return ProvisioningTypeEnum.PAM.getType();
			} else {
				// check for provisioningType -> R , A
				final FDPServiceProvSubType fdpServiceProvSubType = RequestUtil.getFDPServiceProvSubType(fdpRequest);
				if (null != fdpServiceProvSubType
						&& fdpServiceProvSubType.equals(FDPServiceProvSubType.OPT_IN_SERVICE)) {
					return ProvisioningTypeEnum.ADHOC.getType();
				} else if (null != fdpServiceProvSubType
						&& fdpServiceProvSubType.equals(FDPServiceProvSubType.OPT_OUT_SERVICE)) {
					return ProvisioningTypeEnum.RS.getType();
				}
				if (FDPServiceProvSubType.PRODUCT_BUY_RECURRING.name().equals(actionName)
						|| FDPServiceProvSubType.PRODUCT_BUY_SPLIT.name().equals(actionName) || FDPServiceProvSubType.RS_DEPROVISION_PRODUCT.name().equals(actionName)) {
					return (null != fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.provisioningType))
							? fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.provisioningType).toString()
							: ProvisioningTypeEnum.RS.getType();
				} else {
					if (FDPServiceProvSubType.PRODUCT_BUY.name().equals(actionName)) {
						return ProvisioningTypeEnum.ADHOC.getType();
					} else {
						return ProvisioningTypeEnum.ADHOC.getType();
					}
				}
			}

		} else {
			return ProvisioningTypeEnum.ADHOC.getType();
		}

	}

	private Object getFDPProductTypeForRS(FDPRequest fdpRequest) {

		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		String recurringOrValidityUnit = null;
		if (product instanceof Product) {
			recurringOrValidityUnit = ((Product) product).getRecurringOrValidityUnit();
			if ("Days".equalsIgnoreCase(recurringOrValidityUnit)) {
				recurringOrValidityUnit = "DAILY";
			} else if ("Hours".equalsIgnoreCase(recurringOrValidityUnit)) {
				recurringOrValidityUnit = "HOURLY";
			}
		}
		return recurringOrValidityUnit;
	}

	private Object getEMAProvisionAction(FDPRequest fdpRequest) {

		ServiceProvisioningRule serviceprovisioningrule = (ServiceProvisioningRule) fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);

		List<FDPStep> spfdpSteps = serviceprovisioningrule.getFdpSteps();
		for (Iterator iterator = spfdpSteps.iterator(); iterator.hasNext();) {
			FDPStep fdpStep = (FDPStep) iterator.next();
			if (fdpStep instanceof ServiceStep) {
				ServiceStep servicestep = (ServiceStep) fdpStep;
				if (servicestep.getJndiLookupName().equals(SPServices.EMA_SERVICE.getValue())) {
					return servicestep.getAdditionalInformation().get(ServiceStepOptions.PROVISION_ACTION_EMA);
				}
			}
		}
		return null;
	}

	private Object getEMAServiceID(FDPRequest fdpRequest) {
		ServiceProvisioningRule serviceprovisioningrule = (ServiceProvisioningRule) fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);

		List<FDPStep> spfdpSteps = serviceprovisioningrule.getFdpSteps();
		for (Iterator iterator = spfdpSteps.iterator(); iterator.hasNext();) {
			FDPStep fdpStep = (FDPStep) iterator.next();
			if (fdpStep instanceof ServiceStep) {
				ServiceStep servicestep = (ServiceStep) fdpStep;
				if (servicestep.getJndiLookupName().equals(SPServices.EMA_SERVICE.getValue())) {

					return "BBRIM,OPERATION,Activate,SERVICE,"
							+ servicestep.getAdditionalInformation().get(ServiceStepOptions.SERVICE_ID);
				}
			}
		}

		return null;
	}

	/**
	 * This method evaluates the renewal count.
	 * 
	 * @param fdpRequest
	 *            the request object.
	 * @return the renewal count.
	 * @throws EvaluationFailedException
	 *             Exception in execution.
	 */
	private Integer getRenewalCount(final FDPRequest fdpRequest) throws EvaluationFailedException {
		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		Integer renewalCount = null;
		if (product instanceof Product) {
			renewalCount = ((Product) product).getRenewalCount();
			
			//Added by eagarsh for Auto Renewal product type feature.
			if(ChannelType.USSD.equals(fdpRequest.getChannel()) && FDPConstant.TRUE.equalsIgnoreCase(((Product)product).getAdditionalInfo(ProductAdditionalInfoEnum.IS_AUTO_RENEWAL))
					&& null == fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.AUTO_RENEWAL_SESSION_TERMINATE)){
				renewalCount=1;
			}
			if (renewalCount != null) {
				if (renewalCount == 0) {
					return renewalCount;
				} else if (!FDPConstant.UNLIMITED_RENEWAL.equals(renewalCount)) {
					for (CommandParam commandParam : this.getCommand().getInputParam()) {
						if (FDPConstant.ADVANCE_RENEWAL.equals(commandParam.getName())
								&& commandParam instanceof CommandParamInput) {
							((CommandParamInput) commandParam).evaluateValue(fdpRequest);
							if (FDPConstant.ADVANCE_RENEWAL_VALUE.equals(commandParam.getValue().toString())) {
								return renewalCount;

							}
							break;
						}
					}
				}
			}

			else {
				renewalCount = -1;
			}
		}
		return renewalCount;
	}

	/**
	 * Gets the mcarbon value from cache.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @return the mcarbon value from cache
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	private Object getMcarbonValueFromCache(final FDPRequest fdpRequest) throws ExecutionFailedException {
		Object result = null;
		final ExternalSystemDetail mCarbonExternalSystem = MCarbonCommandUtil.getExternalSystem(fdpRequest);
		if (mCarbonExternalSystem == null && !(mCarbonExternalSystem instanceof MCarbonSystemDetail)) {
			throw new ExecutionFailedException("MCarbon Extenal System Details Not Found in Cache");
		} else {
			result = ((MCarbonSystemDetail) mCarbonExternalSystem).getMCarbonValue();
		}
		return result;
	}

	/**
	 * This method is used to get vouchers from cache.
	 * 
	 * @param fdpRequest
	 *            the request.
	 * @return the voucher ids.
	 * @throws ExecutionFailedException
	 *             Exception in execution.
	 */
	private List<String> getValueFromCache(final FDPRequest fdpRequest, final String key)
			throws ExecutionFailedException {
		final FDPCircle fdpCircleWithCSAttr = (FDPCircle) ApplicationConfigUtil.getApplicationConfigCache()
				.getValue(new FDPAppBag(AppCacheSubStore.CS_ATTRIBUTES, fdpRequest.getCircle().getCircleCode()));
		final Map<String, Map<String, String>> csAttrMap = fdpCircleWithCSAttr.getCsAttributesKeyValueMap();
		final Map<String, String> mapOfCS = csAttrMap.get(key);
		List<String> voucherGroup = null;
		if (mapOfCS != null) {
			voucherGroup = new ArrayList<String>();
			for (final Map.Entry<String, String> map : mapOfCS.entrySet()) {
				voucherGroup.add(map.getValue());
			}
		}
		return voucherGroup;
	}

	/**
	 * This method is used to get the charging value for external system.
	 * 
	 * @param externalSystem
	 *            the external system.
	 * @param chargingValue
	 *            the charging value.
	 * @param fdpCircle
	 *            the circle.
	 * @return the charging amount.
	 * @throws EvaluationFailedException
	 *             Exception, in evaluation.
	 * @throws ExecutionFailedException
	 *             Exception, in execution.
	 */
	private Object getValueForExternalSystem(final ExternalSystem externalSystem, final Object chargingValue,
			final FDPCircle fdpCircle) throws EvaluationFailedException, ExecutionFailedException {
		Object value = 0;
		switch (externalSystem) {
		case MM:
		case AIR:
			final Long chargingAmount = (Long) ClassUtil.getPrimitiveValue(chargingValue, Long.class);
			if (chargingAmount != null) {
				value = -1 * chargingAmount;
			}
			break;

		case CGW:
			final FDPCircle fdpCircleWithCSAttr = (FDPCircle) ApplicationConfigUtil.getApplicationConfigCache()
					.getValue(new FDPAppBag(AppCacheSubStore.CS_ATTRIBUTES, fdpCircle.getCircleCode()));
			final Map<String, Map<String, String>> csAttrMap = fdpCircleWithCSAttr.getCsAttributesKeyValueMap();
			final Map<String, String> csAttrClass = csAttrMap.get(FDPConstant.SERVICE_ID);
			value = csAttrClass.get(chargingValue.toString());
			break;
		default:
			break;
		}
		return (value == null) ? new Long(0) : value;
	}

	/**
	 * This method is used to get the parameter value when it is an output of
	 * another command.
	 * 
	 * @param fdpRequest
	 *            The request object.
	 * @return The value of the parameter.
	 * @throws EvaluationFailedException
	 *             Exception, in evaluating the parameter value.
	 */
	private Object getParamValueForCommandOutput(final FDPRequest fdpRequest) throws EvaluationFailedException {
		Object paramOutput = null;
		if (definedValue instanceof CommandParamOutput) {
			final CommandParamOutput commandParamOutput = (CommandParamOutput) definedValue;
			paramOutput = CommandParamUtil.evaluateCommandParameter(fdpRequest, commandParamOutput);
		}
		// System.out.println("param output :: "+paramOutput.toString());
		return paramOutput;
	}

	/**
	 * @param paramTransformationTypeToSet
	 *            the param transformation type to set.
	 */
	public void setParamTransformationType(final ParamTransformationType paramTransformationTypeToSet) {
		this.paramTransformationType = paramTransformationTypeToSet;
	}

	/**
	 * @return the defined value.
	 */
	public Object getDefinedValue() {
		return definedValue;
	}

	/**
	 * This method is used to get the string value for the comamnd param input.
	 * 
	 * @param ignorePath
	 *            true, if the path is to be ignored, false if complete path is
	 *            required.
	 * @return the string format.
	 */
	public String toString(final boolean ignorePath) {
		String stringVal = super.toString();
		if (ignorePath) {
			stringVal = (getValue() == null) ? ((getDefinedValue() == null) ? "" : getDefinedValue().toString())
					: getValue().toString();
		}
		return stringVal;
	}

	/**
	 * This method fetches the value for Command originHostName from
	 * config.props
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private String getOriginHostName(final FDPRequest fdpRequest) {
		String originHostName = PropertyUtils.getProperty(FDPConstant.D_ORIGIN_HOST_NAME);
		if (originHostName == null || originHostName.isEmpty()) {
			originHostName = fdpRequest.getOriginHostName();
		}
		return originHostName;
	}

	/**
	 * This method will return CircleId parameter value to be send in Manhattan
	 * Command
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private String getManhattanCircleId(final FDPRequest fdpRequest) {
		String manhattanCircleId = null;
		manhattanCircleId = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.MANHATTAN_REQUEST_CIRCLEID.getAttributeName());
		return ((manhattanCircleId == null) ? fdpRequest.getCircle().getCircleCode() : manhattanCircleId);
	}

	/**
	 * This method will return the system-IP.
	 * 
	 * @return
	 * @throws ExecutionFailedException
	 */
	private String getSystemIp() throws ExecutionFailedException {
		String ipAddress = PropertyUtils.getProperty(FDPConstant.FDP_VIP_ADDRESS);
		if (StringUtil.isNullOrEmpty(ipAddress)) {
			throw new ExecutionFailedException(
					"Missing configuration in system-config (config.props), against FDP_VIP_ADDRESS key!!");
		}
		return ipAddress.trim();
	}

	/**
	 * This method returns the external data field value for Extra benefit.
	 * 
	 * @param auxKey
	 * @param fdpRequest
	 * @return
	 */
	private String getExtrenalDataFieldValue(AuxRequestParam auxKey, FDPRequest fdpRequest) {
		Object obj = fdpRequest.getAuxiliaryRequestParameter(auxKey);
		if (obj == null) {
			obj = CommandParamInputUtil.getValueFromCircleConfiguration(
					ConfigurationKey.EXTERNAL_DATA_FEILD_DEFAULT_VALUE.getAttributeName(), fdpRequest);
			if (obj == null) {
				obj = FDPConstant.EMPTY_STRING;
			}
		}

		return obj.toString();
	}

	/**
	 * This method returns the circle code from FDPRequest.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private String getFDPCircleCode(final FDPRequest fdpRequest) {
		return fdpRequest.getCircle().getCircleCode();
	}

	/**
	 * This method returns the product ID.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private String getFDPProductId(final FDPRequest fdpRequest) {
		String productId = null;
		final Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (null != product) {
			productId = String.valueOf(product.getProductId());
		}

		if (productId == null) {
			productId = "-1";
		}
		return productId;
	}

	/**
	 * This method returns the product ID.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private String getFDPProductCode(final FDPRequest fdpRequest) {
		String productId = null;
		final Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (null != product) {
			productId = String.valueOf(product.getProductId());
		}
		return productId;
	}

	private Integer getRenewalValue(final FDPRequest fdpRequest) throws EvaluationFailedException {
		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		Integer renewalValue = null;
		String strRenewalValue;
		// Change by eagarsh for ActionType
		String actionName = null;
		final ServiceProvisioningRule serviceProvisionRule = (ServiceProvisioningRule) fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);

		if (serviceProvisionRule.getServiceProvDTO() instanceof AbstractServiceProvDTO) {
			AbstractServiceProvDTO abstractServiceProvDTO = (AbstractServiceProvDTO) serviceProvisionRule
					.getServiceProvDTO();
			FDPLogger.debug(logger, getClass(), "getFDPProductProvisionType()",
					LoggerUtil.getRequestAppender(fdpRequest) + "ActionName "
							+ abstractServiceProvDTO.getSpSubType().name());
			actionName = abstractServiceProvDTO.getSpSubType().name();
		}

		if (product instanceof Product) {
			strRenewalValue = ((Product) product).getRecurringOrValidityValue();

			if (FDPServiceProvSubType.PRODUCT_BUY.name().equals(actionName)) {
				renewalValue = -1;
				return renewalValue;
			} else if (FDPServiceProvSubType.PRODUCT_BUY_RECURRING.name().equals(actionName)
					|| FDPServiceProvSubType.PRODUCT_BUY_SPLIT.name().equals(actionName)) {
				renewalValue = Integer.parseInt(strRenewalValue);
				return renewalValue;
			} else {
				renewalValue = Integer.parseInt(strRenewalValue);

				if (renewalValue != null) {
					if (renewalValue == 0) {
						return renewalValue;
					} else if (!FDPConstant.UNLIMITED_RENEWAL.equals(renewalValue)) {
						for (CommandParam commandParam : this.getCommand().getInputParam()) {
							if (FDPConstant.ADVANCE_RENEWAL.equals(commandParam.getName())
									&& commandParam instanceof CommandParamInput) {
								((CommandParamInput) commandParam).evaluateValue(fdpRequest);
								if (FDPConstant.ADVANCE_RENEWAL_VALUE.equals(commandParam.getValue().toString())) {
									return renewalValue;

								}
								break;
							}
						}
					}
				}

				else {
					renewalValue = -1;
				}
			}
		}
		return renewalValue;
	}

	private String getExpiryNotificationFlag(final FDPRequest fdpRequest) {
		String expiryNotificationFlag = "Y";
		// need to add implmentation
		expiryNotificationFlag = (String) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.EXPIRY_NOTIFICATION_FLAG);
		if (expiryNotificationFlag == null) {
			expiryNotificationFlag = "Y";
		} else {
			if (expiryNotificationFlag.equalsIgnoreCase("false")) {
				expiryNotificationFlag = "N";
			} else {
				expiryNotificationFlag = "Y";
			}
		}
		return expiryNotificationFlag;
	}

	private String getIsSplit(final FDPRequest fdpRequest) {
		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		Boolean isSplit = false;
		String strIsSplit = "N";
		String actionName = null;
		// Change by eagarsh on action type
		final ServiceProvisioningRule serviceProvisionRule = (ServiceProvisioningRule) fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);

		if (serviceProvisionRule.getServiceProvDTO() instanceof AbstractServiceProvDTO) {
			AbstractServiceProvDTO abstractServiceProvDTO = (AbstractServiceProvDTO) serviceProvisionRule
					.getServiceProvDTO();
			FDPLogger.debug(logger, getClass(), "getFDPProductProvisionType()",
					LoggerUtil.getRequestAppender(fdpRequest) + "ActionName "
							+ abstractServiceProvDTO.getSpSubType().name());
			actionName = abstractServiceProvDTO.getSpSubType().name();
		}
		if (FDPServiceProvSubType.PRODUCT_BUY.name().equals(actionName)
				|| FDPServiceProvSubType.PRODUCT_BUY_RECURRING.name().equals(actionName)) {
			strIsSplit = "N";
		} else if (FDPServiceProvSubType.PRODUCT_BUY_SPLIT.name().equals(actionName)) {
			strIsSplit = "Y";
		} else {
			if (product instanceof Product) {
				isSplit = ((Product) product).getIsSplit();
				if (isSplit) {
					strIsSplit = "Y";
				} else {
					strIsSplit = "N";
				}
				return strIsSplit;
			}
		}

		return strIsSplit;
	}

	/**
	 * To get and set the grace period value configured for a recurring product.
	 * 
	 * @param fdpRequest
	 * @return grace period value
	 */
	private String getRecurringGracePeriod(final FDPRequest fdpRequest) {
		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		String gracePeriod = "0";

		if (product instanceof Product) {
			gracePeriod = ((Product) product).getAdditionalInfo(ProductAdditionalInfoEnum.RECURRING_GRACE_PERIOD);
			return gracePeriod;
		}
		return gracePeriod;
	}

	/**
	 * To get and set the retry limit value configured for a recurring product.
	 * 
	 * @param fdpRequest
	 * @return retry limit value
	 */
	private String getRetryLimit(final FDPRequest fdpRequest) {

		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		String retryLimit = "0";

		if (product instanceof Product) {
			retryLimit = ((Product) product).getAdditionalInfo(ProductAdditionalInfoEnum.RS_RETRY);
			return retryLimit;
		}
		return retryLimit;
	}

	// Artifact artf685974 : [RS] Validity parameter should be taken account
	// from Product General configuration screen
	private Object getFDPProductExpiryForRS(FDPRequest fdpRequest) throws ExecutionFailedException {

		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		ValidityValueDTO validityDto = new ValidityValueDTO();
		validityDto.setValidityType(ValidityTypeEnum.NOW_PLUS);

		String recurringOrValidityUnit = null;
		String recurringOrValidityValue = null;
		if (product instanceof Product) {
			recurringOrValidityUnit = ((Product) product).getRecurringOrValidityUnit();
			recurringOrValidityValue = ((Product) product).getRecurringOrValidityValue();
			if (recurringOrValidityValue != null) {
				if ("Days".equalsIgnoreCase(recurringOrValidityUnit)) {
					validityDto.setDays(Integer.parseInt(recurringOrValidityValue));
				} else if ("Hours".equalsIgnoreCase(recurringOrValidityUnit)) {
					validityDto.setHours(Integer.parseInt(recurringOrValidityValue));
				}
			}
		}
		return CommandParamInputUtil.evaluateValidity(validityDto);

	}

	private String getPaysrc(final FDPRequest fdpRequest) {
		String paysrc = null;
		paysrc = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.PAYMENT_MODE);

		if (null != paysrc) {
			return paysrc;
		} else {
			ExternalSystem paysrcSystem = fdpRequest.getExternalSystemToCharge();
			if (paysrcSystem != null)
				return paysrcSystem.name();
			else
				return ExternalSystem.AIR.name();
		}

	}

	private String getBenMsisdn(final FDPRequest fdpRequest) {
		String beneficiaryMsisdn = null;
		beneficiaryMsisdn = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN);
		if (null != beneficiaryMsisdn && FDPCommonValidationUtil.isLong(beneficiaryMsisdn)) {
			return beneficiaryMsisdn;
		} else {
			return "";
		}

	}

	private Boolean sendSMS(final FDPRequest fdpRequest) {
		String sendSMS = null;
		Boolean bolSendSms = false;
		sendSMS = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SEND_SMS);
		Product product = (Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		product.getAdditionalInfo(ProductAdditionalInfoEnum.SEND_TO_SMS);

		if (null != sendSMS && "true".equalsIgnoreCase(sendSMS)) {
			return true;
		} else if (null != sendSMS && "false".equalsIgnoreCase(sendSMS)) {
			return false;
		} else if (product.getAdditionalInfo(ProductAdditionalInfoEnum.SEND_TO_SMS).equals("true")) {
			return true;
		} else {
			return false;
		}

	}

	private Integer getSplitNo(final FDPRequest fdpRequest) {
		final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		final Product product = (Product) fdpCacheable;
		String splitNo = null;
		Integer intSplitNo = -1;
		Integer intSplitNoPro = -1;
		splitNo = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.SPLIT_NUMBER);
		if (null != splitNo) {
			if (FDPCommonValidationUtil.isInteger(splitNo)) {
				intSplitNo = Integer.parseInt(splitNo);
			}
			return intSplitNo;
		} else {
			intSplitNoPro = product.getRenewalCount();
			return intSplitNoPro;
		}

	}

	private String getProductCost(final FDPRequest fdpRequest) {
		String productCost = null;

		productCost = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.PRODUCT_COST);
		if (null != productCost) {
			return productCost;
		} else {
			return "";
		}

	}

	private String getAction(final FDPRequest fdpRequest) {
		String action = null;
		action = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ACTION);
		if (null != action) {
			return action;
		} else {
			return "";
		}

	}

	/**
	 * To get and set the pam id value configured for a recurring product.
	 * 
	 * @param fdpRequest
	 * @return pam id value
	 */
	private Integer getPamId(final FDPRequest fdpRequest) {
		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		Integer pamId = -1;

		if (product instanceof Product) {
			pamId = ((Product) product).getAdditionalInfo(ProductAdditionalInfoEnum.RECURRING_PAM_ID) != null ? Integer
					.parseInt(((Product) product).getAdditionalInfo(ProductAdditionalInfoEnum.RECURRING_PAM_ID))
					: pamId;
		}
		return pamId;
	}

	private String getBeneficiaryMsisdn(final FDPRequest fdpRequest) {
		String beneficiaryMsisdn = null;
		beneficiaryMsisdn = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_MSISDN);
		if (null != beneficiaryMsisdn && FDPCommonValidationUtil.isInteger(beneficiaryMsisdn)) {
			return beneficiaryMsisdn;
		} else {
			return fdpRequest.getSubscriberNumber().toString();
		}
	}

	private Object getImsiID(FDPRequest fdpRequest) {
		return fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.IMSI);
	}

	private String getChannelAndPaySrc(FDPRequest fdpRequest) {
		String channelPaysrc = null;
		String channel = fdpRequest.getChannel().getName();
		if (channel != null) {
			channelPaysrc = "SRCChannel:" + channel.toUpperCase();
		}
		String paySrc = getPaysrc(fdpRequest);
		if (paySrc != null) {
			channelPaysrc = channelPaysrc.concat(",");
			channelPaysrc = channelPaysrc.concat("PaySrc:" + paySrc);
		}
		return channelPaysrc;
	}

	private String getPCRFStartDDate(FDPRequest fdpRequest) throws ExecutionFailedException {
		ValidityValueDTO validityDto = new ValidityValueDTO();
		validityDto.setValidityType(ValidityTypeEnum.NOW);
		String pcrfstartdate = null;
		try {
			pcrfstartdate = DateUtil.convertCalendarDateToString(
					DateUtil.convertStringToCalendarDate((String) CommandParamInputUtil.evaluateValidity(validityDto),
							FDPConstant.FDP_DB_SAVE_DATE_PATTERN),
					FDPConstant.PCRF_DATE_PATTERN);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return pcrfstartdate;
	}

	private String getPCRFENDDDate(FDPRequest fdpRequest) throws ExecutionFailedException {
		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		ValidityValueDTO validityDto = new ValidityValueDTO();
		validityDto.setValidityType(ValidityTypeEnum.NOW_PLUS);
		String pcrfenddate = null;

		String recurringOrValidityUnit = null;
		String recurringOrValidityValue = null;
		if (product instanceof Product) {
			recurringOrValidityUnit = ((Product) product).getRecurringOrValidityUnit();
			recurringOrValidityValue = ((Product) product).getRecurringOrValidityValue();
			if (recurringOrValidityValue != null) {
				if ("Days".equalsIgnoreCase(recurringOrValidityUnit)) {
					validityDto.setDays(Integer.parseInt(recurringOrValidityValue));
				} else if ("Hours".equalsIgnoreCase(recurringOrValidityUnit)) {
					validityDto.setHours(Integer.parseInt(recurringOrValidityValue));
				}
			}
		}

		try {
			pcrfenddate = DateUtil.convertCalendarDateToString(
					DateUtil.convertStringToCalendarDate((String) CommandParamInputUtil.evaluateValidity(validityDto),
							FDPConstant.FDP_DB_SAVE_DATE_PATTERN),
					FDPConstant.PCRF_DATE_PATTERN);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return pcrfenddate;
	}

	/**
	 * To get and set the pam id Zambia value configured for a recurring
	 * product.
	 * 
	 * @param fdpRequest
	 * @return pam id for Zambia value
	 */
	private Object getPamIdZM(final FDPRequest fdpRequest) throws ExecutionFailedException, EvaluationFailedException {

		Object value = null;
		if (null != fdpRequest) {

			FDPCommand command = fdpRequest.getExecutedCommand("RunPeriodicAccountManagement");

			if (null == command) {
				final FDPCacheable fdpCommandCached = ApplicationConfigUtil.getMetaDataCache()
						.getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.COMMAND,
								Command.RunPeriodicAccountManagement.getCommandDisplayName()));
				if (fdpCommandCached instanceof FDPCommand) {
					command = (FDPCommand) fdpCommandCached;
					command = CommandUtil.getExectuableFDPCommand(command);
				} else {
					throw new EvaluationFailedException("Could not evaluate value");
				}
				command.execute(fdpRequest);
				fdpRequest.addExecutedCommand(command);

			}
			if (null != command) {
				CommandParam innputParam = command.getInputParam("pamIndicator");
				if (null != innputParam) {
					value = innputParam.getValue();
				}

			}

		}

		return value;
	}

	/**
	 * To get and set the pam id Zambia value configured for a recurring
	 * product.
	 * 
	 * @param fdpRequest
	 * @return offer id for Zambia value
	 */
	private Object getOfferIdZM(final FDPRequest fdpRequest) {
		String offer = "";
		final FDPCommand command = fdpRequest.getExecutedCommand("RunPeriodicAccountManagement");
		CommandParam innputParam = command.getInputParam("pamIndicator");
		if (innputParam != null) {
			Object value = innputParam.getValue();
			String offerId = value.toString();
			offer = offerId.substring(1);
		}
		return offer;
	}

	/**
	 * To get and set the bundle name value configured for a recurring product.
	 * 
	 * @param fdpRequest
	 * @return bundle name RS
	 */
	private String getBundleName(final FDPRequest fdpRequest) {
		String bundleName = null;
		final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (fdpCacheable instanceof Product) {
			final Product product = (Product) fdpCacheable;
			bundleName = product.getProductName();
		}

		return bundleName;
	}

	/**
	 * To get and set product category value configured for a recurring product.
	 * 
	 * @param fdpRequest
	 * @return product category RS
	 */
	private String getProductCategory(final FDPRequest fdpRequest) {
		String category = null;
		final FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (fdpCacheable instanceof Product) {
			final Product product = (Product) fdpCacheable;
			category = product.getProductType().getName();
		}
		return category;
	}

	public long getOrigantransactionid() {
		return origantransactionid;
	}

	public void setOrigantransactionid(long origantransactionid) {
		this.origantransactionid = origantransactionid;
	}

	/**
	 * Gets SubscriberFriNumber.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private String getSubscriberFRINumber(final FDPRequest fdpRequest) {
		String requestorMsisdn = null;
		requestorMsisdn = String.valueOf(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.REQUESTOR_MSISDN));
		/**
		 * bug fix artf785365 : In Buy for For Others using MMoney, MMDebit
		 * command executed for Party B subscriber
		 */
		/*
		 * if (null != beneficiaryMsisdn &&
		 * FDPCommonValidationUtil.isInteger(beneficiaryMsisdn)) { return "FRI"
		 * + FDPConstant.COLON + beneficiaryMsisdn + "/" + "MSISDN"; }
		 */
		if (null != requestorMsisdn) {
			return "FRI" + FDPConstant.COLON + requestorMsisdn + "/" + "MSISDN";
		} else {
			return "FRI" + FDPConstant.COLON + fdpRequest.getSubscriberNumber().toString() + "/" + "MSISDN";
		}
	}

	/**
	 * This method calculates the difference between current date and activation
	 * date.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private Object getUsagePeriod(FDPRequest fdpRequest) {
		Calendar activatonDate = null;
		final String paramterName = "activationDate";
		FDPCommand executedCommand = (FDPCommand) fdpRequest
				.getExecutedCommand(Command.GETACCOUNTDETAILS.getCommandName());
		if (null != executedCommand && null != executedCommand.getOutputParams()) {
			activatonDate = (Calendar) executedCommand.getOutputParam(paramterName).getValue();
			return DateUtil.calculateDateDifference(activatonDate.getTime());
		}
		return Function.USAGE_PERIOD.getFunctionName();
	}

	private Object getOfferIdFromIMEI(FDPRequest fdpRequest) throws ExecutionFailedException {
		setDeviceBasedValuesInRequest(fdpRequest);
		StringBuffer key = new StringBuffer(FDPConstant.HANDSET_BASED_CHARGING);
		Object offerId = null;
		key.append(FDPConstant.UNDERSCORE).append(FDPConstant.PARAMETER_PRODUCT_ID).append(FDPConstant.UNDERSCORE);
		Long productId = ((Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT)).getProductId();
		key.append(productId);
		key.append(FDPConstant.UNDERSCORE).append(FDPConstant.PARAMETER_DEVICE_TYPE).append(FDPConstant.UNDERSCORE);
		String devType = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.HANDSET_BRANDMODEL);
		key.append(devType);
		key.append(FDPConstant.UNDERSCORE).append(FDPConstant.PARAMETER_IMEI_NUMBER).append(FDPConstant.UNDERSCORE);
		String imei = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.IMEI);
		String imeiLengthStr = PropertyUtils.getProperty(FDPConstant.IMEI_MAX_LENGTH);
		Integer imeiLength = imeiLengthStr != null ? Integer.parseInt(imeiLengthStr) : FDPConstant.ZERO;
		FDPLogger.debug(logger, getClass(), "evaluateValue()", LoggerUtil.getRequestAppender(fdpRequest)
				+ "Max length of IMEI from config.properties file :" + imeiLength);
		FDPLogger.debug(logger, getClass(), "evaluateValue()",
				LoggerUtil.getRequestAppender(fdpRequest) + "Getting Value for the Key:: " + key);
		imei = (imei != null && imei.length() > imeiLength && imeiLengthStr != null
				? imei.substring(FDPConstant.ZERO, imeiLength) : imei);
		key.append(imei);
		final ProductAttributeMapCacheDTO fdpCacheableObject = (ProductAttributeMapCacheDTO) ApplicationConfigUtil
				.getMetaDataCache()
				.getValue(new FDPMetaBag(fdpRequest.getCircle(), ModuleType.PRODUCT_ATTRIBUTE_MAP, key));

		Map<Long, Map<String, String>> valueMap = fdpCacheableObject.getValueMap();
		String notificationText = null;
		for (Map<String, String> entry : valueMap.values()) {
			offerId = entry.get(FDPConstant.PARAMETER_OFFER_ID);
			notificationText = entry.get(FDPConstant.NOTIFICATION_TEXT);
		}
		((FDPRequestImpl) fdpRequest).putAuxiliaryRequestParameter(AuxRequestParam.HANDSET_NOTIFICATION_TEXT,
				notificationText);
		if (offerId == null)
			throw new ExecutionFailedException("the offer id fetched from cache is null");
		return offerId;
	}

	private Object getCisActionType(FDPRequest fdpRequest) {
		Object fieldName = null;
		final ServiceProvisioningRule serviceProvisionRule = (ServiceProvisioningRule) fdpRequest
				.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);
		String actionName = null;
		if (null != serviceProvisionRule
				&& serviceProvisionRule.getServiceProvDTO() instanceof AbstractServiceProvDTO) {
			AbstractServiceProvDTO abstractServiceProvDTO = (AbstractServiceProvDTO) serviceProvisionRule
					.getServiceProvDTO();
			FDPLogger.debug(logger, getClass(), "getFDPProductProvisionType()",
					LoggerUtil.getRequestAppender(fdpRequest) + "ActionName "
							+ abstractServiceProvDTO.getSpSubType().name());
			actionName = abstractServiceProvDTO.getSpSubType().name();
			final FDPNode fdpNode = (FDPNode) fdpRequest.getValueFromRequest(RequestMetaValuesKey.NODE);
			if (null != fdpNode && !(fdpNode instanceof SpecialMenuNode)) {
				if (FDPServiceProvSubType.PRODUCT_BUY_RECURRING.name().equals(actionName)) {
					fieldName = "";
				}
			}
		}
		return fieldName;
	}

	/**
	 * To set the device based values in FDPRequest AuxRequestParam
	 * 
	 * @param fdpRequest
	 * @return offer id for Zambia value
	 */
	private void setDeviceBasedValuesInRequest(final FDPRequest fdpRequest) {
		final FDPCommand command = fdpRequest
				.getExecutedCommand(Command.QUERY_SUSBSCRIBER_HANDSET.getCommandDisplayName());
		if (null != command && fdpRequest instanceof FDPRequestImpl
				&& fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.HANDSET_BRAND) == null) {
			final FDPRequestImpl fdpRequestImpl = (FDPRequestImpl) fdpRequest;
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.IMEI,
					command.getOutputParam(FDPConstant.IMEI_NUMBER).getValue().toString());
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.HANDSET_BRAND,
					command.getOutputParam(FDPConstant.HANDSET_BRAND).getValue().toString());
			fdpRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.HANDSET_BRANDMODEL,
					command.getOutputParam(FDPConstant.HANDSET_BRANDMODEL).getValue().toString());
		}
	}

	private Object getFDPCumProductExpiryForRS(FDPRequest fdpRequest) throws ExecutionFailedException {

		Object expiryDate = null;
		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		expiryDate = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.CUMULATIVE_OFFER_EXPIRY);
		if (null != expiryDate) {
			logger.debug("Inside..." + expiryDate);
			return expiryDate;

		} else {
			ValidityValueDTO validityDto = new ValidityValueDTO();
			validityDto.setValidityType(ValidityTypeEnum.NOW_PLUS);

			String recurringOrValidityUnit = null;
			String recurringOrValidityValue = null;
			if (product instanceof Product) {
				recurringOrValidityUnit = ((Product) product).getRecurringOrValidityUnit();
				recurringOrValidityValue = ((Product) product).getRecurringOrValidityValue();
				if (recurringOrValidityValue != null) {
					if ("Days".equalsIgnoreCase(recurringOrValidityUnit)) {
						validityDto.setDays(Integer.parseInt(recurringOrValidityValue));
					} else if ("Hours".equalsIgnoreCase(recurringOrValidityUnit)) {
						validityDto.setHours(Integer.parseInt(recurringOrValidityValue));
					}
				}

			}

			return CommandParamInputUtil.evaluateValidity(validityDto);

		}

	}

	/** This function will parse command response **/
	private List<String> parseCommandResponse(final FDPRequest fdpRequest, final FDPCommand fdpCommand) {
		/*
		 * List<String> productList= new ArrayList<String>();
		 * productList.add(CommandUtil.
		 * parseReponseFromRsSingleProvisioningRequest(fdpRequest,fdpCommand));
		 */ return CommandUtil.parseReponseFromRsSingleProvisioningRequest(fdpRequest, fdpCommand);
	}

	/**
	 * This method is to update expiry date or expiry date time parameter
	 * 
	 * @param fdpRequest
	 * @throws ExecutionFailedException
	 */
	public String evaluateExpiryOffer(final FDPRequest fdpRequest) throws ExecutionFailedException {
		String expiryDateTime = null;
		logger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);
		FDPLogger.debug(logger, getClass(), "evaluateExpiryOffer()",
				LoggerUtil.getRequestAppender(fdpRequest) + "Evaluating parameter " + getName());
		Object offerValidity = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.OFFER_VALIDITY);
		try {
			if (fdpRequest.getChannel().equals(ChannelType.FLYTXT) && null != offerValidity) {
				expiryDateTime = DateUtil.getDateTimeFormatGMTPlusOne((String) offerValidity,
						FDPConstant.FDP_DB_SAVE_DATE_PATTERN_WITHOUT_Z);
			} else {
				expiryDateTime = (String) getFDPProductExpiryForRS(fdpRequest);
			}
			super.setValue(TransformationUtil.evaluateTransformation(expiryDateTime, paramTransformationType));
		} catch (ParseException e) {
			FDPLogger.error(logger, getClass(), LoggerUtil.getRequestAppender(fdpRequest),
					"Error while updating expiry date and time :" + e);
			throw new ExecutionFailedException("Could not execute command", e);
		}
		return expiryDateTime;
	}

	public String getSimLanguageId(final FDPRequest fdpRequest) {
		LanguageType simLangauge;
		if (null != fdpRequest && null != fdpRequest.getSimLangauge()) {
			simLangauge = fdpRequest.getSimLangauge();
			return simLangauge.getValue().toString();
		}
		return LanguageType.ENGLISH.getValue().toString();
	}

	/**
	 * The method is used to show charging amount in notification param.
	 * 
	 * @param fdpRequest
	 * @return
	 * @throws EvaluationFailedException
	 */
	public Long getChargingAmount(final FDPRequest fdpRequest) throws EvaluationFailedException {
		Long chargingAmt = 0L;
		final FDPCacheable charging = fdpRequest.getValueFromRequest(RequestMetaValuesKey.CHARGING_STEP);
		if (null != charging && charging instanceof ChargingValueImpl
				&& ((ChargingValueImpl) charging).getChargingValue() != null) {
			chargingAmt = Long.parseLong(((ChargingValueImpl) charging).getChargingValue().toString());
			chargingAmt = Math.abs(chargingAmt);
		}
		return chargingAmt;
	}

	/**
	 * The method is used to show reneual date in notification param.
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private Object getRenewalDate(FDPRequest fdpRequest) {
		String renewalDate = null;
		String newRenewalDate = null;
		final String parameterName = "expiryDate";
		FDPCommand executedCommand = (FDPCommand) fdpRequest.getExecutedCommand(Command.SPR.getCommandName());
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FDPConstant.DATE_PATTERN);
		if (null != executedCommand && null != executedCommand.getInputParam()) {
			renewalDate = executedCommand.getInputParam(parameterName).getValue().toString();
			try {
				SimpleDateFormat sd1 = new SimpleDateFormat(FDPConstant.FDP_DB_SAVE_DATE_PATTERN_WITHOUT_Z);
				newRenewalDate = simpleDateFormat.format(sd1.parse(renewalDate));

			} catch (ParseException e) {
				FDPLogger.error(logger, getClass(), "getRenewalDate()", LoggerUtil.getRequestAppender(fdpRequest)
						+ "Evaluating parameter " + getName() + " " + e.getMessage());
			}
		} else {
			Date date1 = new Date();
			newRenewalDate = simpleDateFormat.format(date1);
		}
		return newRenewalDate;
	}

	/**
	 * Returns the originoperatorid that contains Iname, Paysrc and productId
	 * 
	 * @param fdpRequest
	 * @return
	 */
	private Object getOriginOperatorId(FDPRequest fdpRequest) {
		String delimiter = fdpRequest.getCircle().getConfigurationKeyValueMap()
				.get(ConfigurationKey.OPERATOR_ID_DELIMITER.getAttributeName());
		StringBuilder operatorId = new StringBuilder();
		if (fdpRequest instanceof FulfillmentRequestImpl) {
			operatorId.append(((FulfillmentRequestImpl) fdpRequest).getIname());
		} else {
			operatorId.append(fdpRequest.getChannel().getName());
		}
		String paySrc = getPaysrc(fdpRequest);
		operatorId.append((delimiter != null) ? delimiter : "000")
				.append(paySrc.equals(ExternalSystem.AIR.name()) || paySrc.equals(FDPConstant.PAYMENY_MODE_AIR)
						? ExternalSystem.AIR.name() : getPaysrc(fdpRequest))
				.append((delimiter != null) ? delimiter : "000");

		String productId = getFDPProductId(fdpRequest);
		operatorId.append(productId.equals("-1") ? "" : productId);
		return operatorId.toString();
	}

	/**
	 * This method will return the value of input configuration key as defined
	 * in fdpCircle
	 * 
	 * @return
	 */
	private String getConfigurationMapValue(FDPRequest fdpRequest, ConfigurationKey key) {
		final Map<String, String> configurationMap = fdpRequest.getCircle().getConfigurationKeyValueMap();
		return configurationMap.get(key.getAttributeName());
	}
	/**
    * Returns the emailId
    * 
    * @param fdpRequest
    * @return
    *//*
	private String getEmailId(FDPRequest fdpRequest) {
		String emailId = "";
		
		if (fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.EMAIL_ID) != null) {
			emailId = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.EMAIL_ID);
		}
		return emailId;
	}*/
	

	private Object getFDPProductExpiryForRSOneDayExtra(FDPRequest fdpRequest) throws ExecutionFailedException {

		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		ValidityValueDTO validityDto = new ValidityValueDTO();
		validityDto.setValidityType(ValidityTypeEnum.NOW_MINUS_ONE);

		String recurringOrValidityUnit = null;
		String recurringOrValidityValue = null;
		if (product instanceof Product) {
			recurringOrValidityUnit = ((Product) product).getRecurringOrValidityUnit();
			recurringOrValidityValue = ((Product) product).getRecurringOrValidityValue();
			if (recurringOrValidityValue != null) {
				if ("Days".equalsIgnoreCase(recurringOrValidityUnit)) {
					validityDto.setDays(Integer.parseInt(recurringOrValidityValue));
				} else if ("Hours".equalsIgnoreCase(recurringOrValidityUnit)) {
					validityDto.setHours(Integer.parseInt(recurringOrValidityValue));
				}
			}
		}
		return CommandParamInputUtil.evaluateValidity(validityDto);

	}
	
	/**
	 * This function will evaluate the CIS product name and append "CIS-" in it
	 * @param fdpRequest
	 * @return
	 */
	private Object getCisProductName(FDPRequest fdpRequest) {
		StringBuilder cisProductName = new StringBuilder();
		cisProductName.append("CIS-");
		final FDPCacheable product = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
		if (product instanceof Product) {
			cisProductName.append(((Product) product).getProductName());
		}
		 return (Object)cisProductName;
	}
	
	/**
	 * Get the Product Type
	 * @param fdpRequest
	 * @return
	 */
	private Object getViewProductType(FDPRequest fdpRequest) {
		String viewProductType = null;
		viewProductType = (String) fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.VIEW_PRODUCT_TYPE);
		if (viewProductType == null)
			viewProductType = "";
		return viewProductType;
	}
	
	/**
	 * This method will generate the MD5 has value of Me2u Pin
	 * @param fdpRequest
	 * @return
	 */
	
	private Object getMe2uPin(FDPRequest fdpRequest) {
		String me2uPin = null;
		String pin = null;
		pin = (String)fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.ME2U_PIN);
		if (pin != null && !pin.isEmpty()) {
			me2uPin = com.ericsson.fdp.common.util.ClassUtils.getMD5Hash(pin);
			FDPLogger.info(logger, getClass(), "getMe2uPin()",
					LoggerUtil.getRequestAppender(fdpRequest) + "Generated MD5 value of Me2u Pin is" + me2uPin);
		}
		if (me2uPin == null)
			me2uPin = "";
		
		return me2uPin;
	}
	
	/**
	 * This method would respond the USSD string or 3pp Channel based on input channel
	 * @param fdpRequest
	 * @return The Short Code
	 */
	private Object getShortCodeOrChannel(FDPRequest fdpRequest){
		
		if(fdpRequest.getChannel()==ChannelType.USSD){
			String rootString = fdpRequest.getLastServedString().substring(0+1, fdpRequest.getLastServedString().length());			
			return rootString.substring(0, rootString.indexOf('*'));		
		}
		return fdpRequest.getChannel().name();
	}
	
}