package com.ericsson.fdp.business.cache.datageneration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.business.charging.Discount;
import com.ericsson.fdp.business.charging.impl.ProductChargingStepImpl;
import com.ericsson.fdp.business.command.AbstractCommand;
import com.ericsson.fdp.business.command.param.AbstractCommandParam;
import com.ericsson.fdp.business.command.param.impl.CommandParamInput;
import com.ericsson.fdp.business.enums.Command;
import com.ericsson.fdp.business.enums.SPServiceJNDILookupPath;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.notification.FDPNotification;
import com.ericsson.fdp.business.notification.impl.ServiceProvisioningNotificationImpl;
import com.ericsson.fdp.business.rule.Rule;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.step.impl.AttachProvisioningConditionStep;
import com.ericsson.fdp.business.step.impl.AttachProvisioningStep;
import com.ericsson.fdp.business.step.impl.CommandStep;
import com.ericsson.fdp.business.step.impl.DatabaseNoRollbackExecutionStep;
import com.ericsson.fdp.business.step.impl.DatabaseStep;
import com.ericsson.fdp.business.step.impl.OfflineNotificationStep;
import com.ericsson.fdp.business.step.impl.OverrideNotification;
import com.ericsson.fdp.business.step.impl.ServiceStep;
import com.ericsson.fdp.business.step.impl.UpdateBalanceAndDateCommandStep;
import com.ericsson.fdp.business.util.ExpressionUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.FDPSPNotificationParamEnum;
import com.ericsson.fdp.common.enums.ParameterFeedType;
import com.ericsson.fdp.common.enums.Primitives;
import com.ericsson.fdp.common.enums.SPServices;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.command.param.CommandParam;
import com.ericsson.fdp.dao.dto.CommandDTO;
import com.ericsson.fdp.dao.dto.FDPCommandCircleDTO;
import com.ericsson.fdp.dao.dto.ParameterDTO;
import com.ericsson.fdp.dao.dto.common.constraint.SubStepConstraintDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvCommandDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvCommandParamDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvNotificationDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvProductDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvSubStepDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepChargingDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepCommandDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepConstraintDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepDataBaseDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepNotificationDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepNotificationOtherDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepSPServiceDTO;
import com.ericsson.fdp.dao.dto.serviceprov.StepValidationDTO;
import com.ericsson.fdp.dao.enums.ChargingType;
import com.ericsson.fdp.dao.enums.CommandDefinition;
import com.ericsson.fdp.dao.enums.CommandParameterType;
import com.ericsson.fdp.dao.enums.DatabaseStepType;
import com.ericsson.fdp.dao.enums.FDPServiceProvType;
import com.ericsson.fdp.dao.enums.LanguageType;
import com.ericsson.fdp.dao.enums.SPNotificationType;

/**
 * The Class SPCacheUtil.
 * 
 * @author Ericsson
 */
public class SPCacheUtil {
	private static final Logger LOGGER = LoggerFactory.getLogger(SPCacheUtil.class);

	/**
	 * Instantiates a new sP cache util.
	 */
	private SPCacheUtil() {

	}

	/**
	 * Gets the input command param.
	 * 
	 * @param spCommandParamsList
	 *            the sp command params list
	 * @param fdpCommand
	 *            the fdp command
	 * @return the input command param
	 */
	public static List<CommandParam> getInputCommandParam(final List<ServiceProvCommandParamDTO> spCommandParamsList,
			final AbstractCommand fdpCommand) {
		final List<CommandParam> inputFDPCommandParamList = new ArrayList<CommandParam>();

		for (final ServiceProvCommandParamDTO spCommandParamDTO : spCommandParamsList) {
			final ParameterDTO parameterDTO = spCommandParamDTO.getCommandCircleParamDTO().getParameterDTO();
			// TODO CommandParameterSource and defined value
			final ParameterFeedType commandParameterSource = null;
			final AbstractCommandParam fdpCommandParamInput =
					new CommandParamInput(commandParameterSource, parameterDTO.getDefaultValueSP());
			// set the name of the input parameter of command
			fdpCommandParamInput.setName(parameterDTO.getParameterName());

			// CommandParameterDataType type =
			// CommandParameterDataType.valueOf(parameterDTO.getDisplayType());
			// if (type == FDPCommandParameterDataType.ARRAY) {
			// fdpCommandParamInput.setType(CommandParameterType.Array);
			// } else if (type == FDPCommandParameterDataType.STRUCT) {
			// fdpCommandParamInput.setType(CommandParameterType.Struct);
			// } else {
			// fdpCommandParamInput.setType(CommandParameterType.Primitive);
			// }
			// TODO
			// fdpCommandParamInput.setPrimitiveValue(primitiveValueToSet);
			// fdpCommandParamInput.setValue(parameterDTO.get);
			// TODO
			// fdpCommandParamInput.setChilderen(childerenToSet);
			// set the owner command of the parameter
			fdpCommandParamInput.setCommand(fdpCommand);
			// TODO
			// fdpCommandParamInput.setParent(parentToSet);
			// TODO
			// /flattenedParameter
			// TODO set parent here
			// fdpCommandParamInput.setParent(fdpCommandParamInput);
			inputFDPCommandParamList.add(fdpCommandParamInput);
		}
		return inputFDPCommandParamList;
	}
	
	/**
	 * Populate command step for others.
	 *
	 * @param steps
	 *            the steps
	 * @param step
	 *            the step
	 */
	public static void populateCommandStepForOthers(final List<FDPStep> steps, final StepCommandDTO step) {
		final CommandDTO commandDTO = step.getSpCommandDTO().getCommandCircleDTO().getCommandDTO();
		final FDPCommand fdpCommand = CommandCacheUtil.commandDTOToFDPCommand(commandDTO);
		steps.add(new CommandStep(fdpCommand, fdpCommand.getCommandDisplayName(), step.getStepId(), step.getStepName()));
	}
	
	/**
	 * Populates Command Step into the steps list.
	 *
	 * @param steps
	 *            list of the steps
	 * @param step
	 *            the step
	 */
	public static void populateCommandStep(final List<FDPStep> steps, final StepCommandDTO step, final Logger LOGGER) {
		CommandStep commandStep;
		
		LOGGER.debug("populateCommandStep Current Step is "+step);
		final CommandDTO commandDTO = step.getSpCommandDTO().getCommandCircleDTO().getCommandDTO();
		LOGGER.debug(
				"SP Command Step : {} UPDATING with command : {} commandDefinition is : {}",
				new Object[] { step.getStepName(), commandDTO.getCommandDisplayName(),
						commandDTO.getCommandDefinition() });
		/*if (CommandDefinition.GET.equals(commandDTO.getCommandDefinition())) {
			commandStep = new CommandStep(commandDTO.getCommandDisplayName(), step.getStepId(), step.getStepName());
		} else {
			final FDPCommand fdpCommand = CommandCacheUtil.commandDTOToFDPCommand(commandDTO);
			commandStep = new CommandStep(fdpCommand, fdpCommand.getCommandDisplayName(), step.getStepId(),
					step.getStepName());
			LOGGER.debug("SP Command Step : {} UPDATED with command : {} ", step.getStepName(), fdpCommand);
		}*/
		
		// Changes done to stop loading get command step into cache 
		if (!CommandDefinition.GET.equals(commandDTO.getCommandDefinition())) {
			final FDPCommand fdpCommand = CommandCacheUtil.commandDTOToFDPCommand(commandDTO);
			commandStep = new CommandStep(fdpCommand, fdpCommand.getCommandDisplayName(), step.getStepId(),
					step.getStepName());
			LOGGER.debug("SP Command Step : {} UPDATED with command : {} ", step.getStepName(), fdpCommand);
			steps.add(commandStep);
		}
		//steps.add(commandStep);
	}
	
	/**
	 * Populates Constraint step and the sub steps.
	 *
	 * @param steps
	 *            list of steps
	 * @param step
	 *            the step
	 */
	public static void populateConstraintStep(final List<FDPStep> steps, final StepConstraintDTO step, final Logger logger) {
		final List<ServiceProvSubStepDTO> serviceProvSubStepList = step.getSubstepDTOList();
		final List<AttachProvisioningConditionStep> attachProvisioningToList = new ArrayList<AttachProvisioningConditionStep>();
		List<FDPStep> commandSteps = null;
		Expression expression = null;
		if (serviceProvSubStepList != null) {
			for (final ServiceProvSubStepDTO serviceProvSubStepDTO : serviceProvSubStepList) {
				final List<SubStepConstraintDTO> constraintDTOList = serviceProvSubStepDTO.getConstraintDTOList();
				// create expression for constraints here
				expression = createConstraintsExpression(constraintDTOList);
				// populate commands here
				commandSteps = populateStepConstraintCommands(serviceProvSubStepDTO.getSpCommandDTOList(),logger);
				final AttachProvisioningConditionStep fdpAttachProvisioningConditionStep = new AttachProvisioningConditionStep(
						expression, commandSteps, serviceProvSubStepDTO.getServiceProvSubStepId(),
						serviceProvSubStepDTO.getSubStepName());
				attachProvisioningToList.add(fdpAttachProvisioningConditionStep);
			}
		}
		final AttachProvisioningStep attachActionConstraintStep = new AttachProvisioningStep(attachProvisioningToList,
				step.getStepId(), step.getStepName());
		steps.add(attachActionConstraintStep);
	}
	
	/**
	 * Creates Expression for list of Step Constraint sub step constraints.
	 *
	 * @param constraintDTOList
	 *            list of constraints.
	 * @return the expression
	 */
	private static Expression createConstraintsExpression(final List<SubStepConstraintDTO> constraintDTOList) {
		Expression expression = null;
		try {
			expression = ExpressionUtil.createExpressionForSubStep(constraintDTOList);
		} catch (final ExpressionFailedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// TODO
		}
		return expression;
	}
	
	/**
	 * populates Step Constraint Commands in the cache object.
	 *
	 * @param spCommandDTOList
	 *            list of service provisioning Commands.
	 * @return list of cache commands
	 */
	private static List<FDPStep> populateStepConstraintCommands(final List<ServiceProvCommandDTO> spCommandDTOList, final Logger LOGGER) {
		List<FDPStep> commandSteps = null;
		if (null != spCommandDTOList && !spCommandDTOList.isEmpty()) {
			commandSteps = new ArrayList<FDPStep>(spCommandDTOList.size());
			long index = 1;
			for (final ServiceProvCommandDTO fdpServiceProvCommandDTO : spCommandDTOList) {
				final FDPCommandCircleDTO commandCircleDTO = fdpServiceProvCommandDTO.getCommandCircleDTO();
				if (null == commandCircleDTO) {
					continue;
				}
				final CommandDTO commandDTO = commandCircleDTO.getCommandDTO();
				LOGGER.debug("Constraint Command : {} , command : {} Updating.",
						fdpServiceProvCommandDTO.getSubstepCommandName(), fdpServiceProvCommandDTO
								.getCommandCircleDTO().getCommandDTO().getCommandDisplayName());
				final FDPCommand fdpCommand = CommandCacheUtil.commandDTOToFDPCommand(commandDTO);
				if (null != fdpCommand) {
					final CommandStep commandStep;
					//code commited for zambia ehlnopu
					
				/*	if(fdpCommand.getCommandName().equalsIgnoreCase(Command.UPDATE_USAGE_THRESHOLDS_AND_COUNTERS.getCommandName())){
						commandStep = new UpdateUsageThresholdAndCountersCommandStep(fdpCommand, commandDTO.getCommandDisplayName(),
							index, FDPConstant.COMMAND_STEP + index);
					}else */
						if(fdpCommand.getCommandDisplayName().equals(Command.UPDATE_BALACEANDATE_MAIN.getCommandDisplayName())){
						commandStep = new UpdateBalanceAndDateCommandStep(fdpCommand, commandDTO.getCommandDisplayName(),
								index, FDPConstant.COMMAND_STEP + index);
					}else if(fdpCommand.getCommandDisplayName().equals(Command.OVERRIDE_NOTIFICATION.getCommandDisplayName())){
						commandStep = new OverrideNotification(fdpCommand, commandDTO.getCommandDisplayName(),
								index, FDPConstant.COMMAND_STEP + index);
					}else{
						commandStep = new CommandStep(fdpCommand, commandDTO.getCommandDisplayName(),
								index, FDPConstant.COMMAND_STEP + index);
					}
					commandSteps.add(commandStep);
					index++;
				}
			}
		}

		return commandSteps;
	}
	
	/**
	 * Populate validation step.
	 *
	 * @param steps
	 *            the steps
	 * @param step
	 *            the step
	 * @param productId
	 *            the product id
	 */
	public static void populateValidationStep(final List<FDPStep> steps, final StepValidationDTO step, final Long productId) {
		steps.add(new DatabaseStep(step.getValidationStepClassName().getLookUpClass(), productId, step.getStepId(),
				step.getStepName(), DatabaseStepType.VALIDATION_STEP));
	}
	
	/**
	 * Populates Charging step for SP.
	 *
	 * @param steps
	 *            list od steps
	 * @param step
	 *            the step
	 * @param chargingType
	 *            the charging type
	 */
	public static void populateChargingStep(final List<FDPStep> steps, final StepChargingDTO step,
			final ChargingType chargingType) {
		// put charging step here id this is of Product type SP
		final ProductChargingStepImpl fdpProductChargingStepImpl = new ProductChargingStepImpl(step.getStepId(),
				step.getStepName(), chargingType);
		steps.add(fdpProductChargingStepImpl);
	}
	
	/**
	 * Populate database step.
	 *
	 * @param steps
	 *            the steps
	 * @param step
	 *            the step
	 * @param productId
	 *            the product id
	 */
	public static void populateDatabaseStep(final List<FDPStep> steps, final StepDataBaseDTO step, final Long productId) {
		steps.add(new DatabaseNoRollbackExecutionStep(step.getDbActionClassName().getLookUpClass(), productId, step
				.getStepId(), step.getStepName(), DatabaseStepType.DATABASE_STEP));
	}
	
	public static void populateOfflineNotificationStep(final List<FDPStep> steps, final StepNotificationOtherDTO step) {
		final ServiceProvNotificationDTO serviceProvNotificationDTO = step.getSpNotification();
		if (serviceProvNotificationDTO != null && !serviceProvNotificationDTO.getIsSuppress()) {
			final Map<SPNotificationType, Long> notificationsToSet = new LinkedHashMap<SPNotificationType, Long>();
			// Offline notification step's status is success
			notificationsToSet.put(SPNotificationType.SUCCESS, serviceProvNotificationDTO.getNotifications()
					.getNotificationsId());

			final ServiceProvisioningNotificationImpl notificationImpl = new ServiceProvisioningNotificationImpl(
					notificationsToSet, ChannelType.valueOf(serviceProvNotificationDTO.getChannelType()));
			final CommandParamInput commandParamInput = new CommandParamInput(
					ParameterFeedType.valueOf(serviceProvNotificationDTO.getMsidnSource()),
					serviceProvNotificationDTO.getMsidnValue());
			commandParamInput.setPrimitiveValue(Primitives.LONG);
			commandParamInput.setType(CommandParameterType.PRIMITIVE);
			final OfflineNotificationStep offlineNotificationStep = new OfflineNotificationStep(commandParamInput,
					notificationImpl, step.getStepId(), step.getStepName());
			steps.add(offlineNotificationStep);
		}

	}
	
	/**
	 * Populates and returns the notification for SP.
	 *
	 * @param spNotifications
	 *            the sp notifications
	 * @return FDPNotification
	 */
/*	public static FDPNotification getPopulateNotifications(final List<ServiceProvNotificationDTO> spNotifications) {
		ServiceProvisioningNotificationImpl serviceProvisioningNotification = null;
		final Map<SPNotificationType, Long> notificationsToSet = new LinkedHashMap<SPNotificationType, Long>();
		if (null != spNotifications && !spNotifications.isEmpty()) {
			for (final ServiceProvNotificationDTO notification : spNotifications) {
				if (!notification.getIsSuppress()) {
					// TOD check this id should be notification id
					notificationsToSet.put(notification.getNotificationType(), notification.getNotifications()
							.getNotificationsId());
					// set notification here
					serviceProvisioningNotification = new ServiceProvisioningNotificationImpl(notificationsToSet);
				}
			}
		}
		return serviceProvisioningNotification;
	}*/
	
	public static FDPNotification getPopulateNotifications(final List<ServiceProvNotificationDTO> spNotifications) {
	ServiceProvisioningNotificationImpl serviceProvisioningNotification = null;
	final Map<SPNotificationType, Long> notificationsToSet = new LinkedHashMap<SPNotificationType, Long>();
	final Map<LanguageType,Map<SPNotificationType, Long>> otherLangNotificationMap = new HashMap<LanguageType, Map<SPNotificationType,Long>>();
	FDPSPNotificationParamEnum fdpSPNotificationParamEnum = null;
	if (null != spNotifications && !spNotifications.isEmpty()) {	
		for (final ServiceProvNotificationDTO notification : spNotifications) {
			if (!notification.getIsSuppress()) {
				
				if(null == notification.getLanguageType()) {					
						notificationsToSet.put(notification.getNotificationType(), notification.getNotifications()
								.getNotificationsId());
					}
				else {
						Map<SPNotificationType, Long> otherLang = otherLangNotificationMap.get(notification.getLanguageType());

						if(null == otherLang) {
							otherLang = new HashMap<SPNotificationType, Long>();
							otherLang.put(notification.getNotificationType(), notification.getNotifications()
								.getNotificationsId());
							otherLangNotificationMap.put(notification.getLanguageType(), otherLang);

						} else {
							otherLang.put(notification.getNotificationType(), notification.getNotifications()
									.getNotificationsId());
								otherLangNotificationMap.put(notification.getLanguageType(), otherLang);

						}
					}
					
					//moved by rahul to make serviceProvisioningNotification out of loop					
					//Added for SP notification parameters
					if(null!=notification.getSpNotificationParam() && SPNotificationType.SUCCESS==notification.getNotificationType())
					{
						fdpSPNotificationParamEnum = FDPSPNotificationParamEnum.getFDPNotificationSortingParamEnumUsingParamValue(Integer.parseInt(notification.getSpNotificationParam()));
					}
				}
			}
			serviceProvisioningNotification = new ServiceProvisioningNotificationImpl(notificationsToSet,otherLangNotificationMap,fdpSPNotificationParamEnum);
		}
		return serviceProvisioningNotification;
	}
	
	/**
	 * Populate sp service step.
	 *
	 * @param steps
	 *            the steps
	 * @param spServiceDTO
	 *            the sp service dto
	 */
	public static void populateSPServiceStep(final List<FDPStep> steps, final StepSPServiceDTO spServiceDTO) {

		final SPServices serviceEnum = SPServices.getSPService(spServiceDTO.getService());
		SPServiceJNDILookupPath jndiLookupNameEnum = null;
		switch (serviceEnum) {
		case EMA_SERVICE:
			jndiLookupNameEnum = SPServiceJNDILookupPath.getEnumFromService(spServiceDTO.getMode());
			break;
		case TARIFF_ENQUIRY_SERVICE:
			jndiLookupNameEnum = SPServiceJNDILookupPath.TARIFF_ENQUIRY;
			break;
		case CHECK_BALANCE_SERVICES:
			jndiLookupNameEnum = SPServiceJNDILookupPath.CHECK_BALANCE_SERVICES;
			break;
		case COMVIVA_MENU_REDIRECT:
			jndiLookupNameEnum = SPServiceJNDILookupPath.COMVIVA_MENU_REDIRECT;
		default:
			break;
		}

		final ServiceStep serviceStep = new ServiceStep(jndiLookupNameEnum.getJndiLookupPath(),
				spServiceDTO.getStepId(), spServiceDTO.getStepName());

		switch (serviceEnum) {
		case EMA_SERVICE:
			 /*Get the EMA Action and Mode
			  * Actions will have service type on gui QUERY_AND_UPDATE and UPDATE_ALL
			  * Mode is are 2G_Activation and 3G_Activation 
			  * */
				if(spServiceDTO.getMode().equals(SPServiceJNDILookupPath.BLACKBERRY_SERVICE_ACTIVATION.getService()))
				{
				serviceStep.putAdditionalInformation(ServiceStepOptions.MODE, spServiceDTO.getMode());
				serviceStep.putAdditionalInformation(ServiceStepOptions.SERVICE_ID, spServiceDTO.getServiceid());
				serviceStep.putAdditionalInformation(ServiceStepOptions.PROVISION_ACTION_EMA,spServiceDTO.getProvisionAction());
				}
				else
				{
					serviceStep.putAdditionalInformation(ServiceStepOptions.ACTION, spServiceDTO.getServiceType());
					serviceStep.putAdditionalInformation(ServiceStepOptions.MODE, spServiceDTO.getMode());
					
				}
				break;
		case TARIFF_ENQUIRY_SERVICE:
			final StringBuilder tariffOptionsString = new StringBuilder("");
			if (spServiceDTO.getTariffEnquiryOption() != null) {
				final Iterator<String> iterator = spServiceDTO.getTariffEnquiryOption().iterator();
				while (iterator.hasNext()) {
					tariffOptionsString.append(iterator.next());
					if (iterator.hasNext()) {
						tariffOptionsString.append(FDPConstant.COMMA);
					}
				}
			}
			serviceStep.putAdditionalInformation(ServiceStepOptions.TARIFF_ENQUIRY_ATTRIBUTES,
					tariffOptionsString.toString());
			break;
		case COMVIVA_MENU_REDIRECT :
			serviceStep.putAdditionalInformation(ServiceStepOptions.SP_SERVICE_APPLICATION_RESPONSE, spServiceDTO.getApplicationResponse());
			serviceStep.putAdditionalInformation(ServiceStepOptions.SP_SERVICE_FREE_FLOW_STATE, spServiceDTO.getFreeflowState());
			serviceStep.putAdditionalInformation(ServiceStepOptions.SP_SERVICE_FREE_FLOW_CHARGING,spServiceDTO.getFreeflowCharging());
			serviceStep.putAdditionalInformation(ServiceStepOptions.SP_SERVICE_FREE_FLOW_CHARGING_AMOUNT,spServiceDTO.getFreeflowChargingAmount());
			serviceStep.putAdditionalInformation(ServiceStepOptions.SP_SERVICE_MENU_CODE, spServiceDTO.getMenuCode());
		default:
			break;
		}

		serviceStep.putAdditionalInformation(ServiceStepOptions.CHECK_ICR_CIRCLE, spServiceDTO.getWhiteListLookup());
		steps.add(serviceStep);
	}
	
	public static ServiceProvisioningRule getExecutableFDPSP(final Rule rule) {
		ServiceProvisioningRule serviceProvisioningRule = (ServiceProvisioningRule) rule;
		final ServiceProvDTO serviceProvDTO = serviceProvisioningRule.getServiceProvDTO();
		final List<FDPStep> steps = new ArrayList<FDPStep>();
		ServiceProvisioningNotificationImpl fdpServiceProvisioningNotification = null;
		final List<StepDTO> serviceProvStepsList = serviceProvDTO.getServiceProvStepList();
		if (serviceProvStepsList != null && !serviceProvStepsList.isEmpty()) {
			for (final StepDTO step : serviceProvStepsList) {
				if (step instanceof StepCommandDTO) {
					final StepCommandDTO cmdStep = (StepCommandDTO) step;
					if (FDPServiceProvType.OTHER.equals(((ServiceProvProductDTO) serviceProvDTO).getSpType())) {
						SPCacheUtil.populateCommandStepForOthers(steps, cmdStep);
					} else {
						SPCacheUtil.populateCommandStep(steps, cmdStep,LOGGER);
					}
				} else if (step instanceof StepConstraintDTO) {
					SPCacheUtil.populateConstraintStep(steps, (StepConstraintDTO) step,LOGGER);
				} else if (step instanceof StepValidationDTO) {
					// populate validation step
					SPCacheUtil.populateValidationStep(steps, (StepValidationDTO) step,
							((ServiceProvProductDTO) serviceProvDTO).getProductId());
				} else if (step instanceof StepChargingDTO) {
					final Discount discount = populateDiscount();
					if(null != discount) {
						SPCacheUtil.populateChargingStep(steps, (StepChargingDTO) step, ((ServiceProvProductDTO) serviceProvDTO)
								.getSpSubType().getChargingType(),discount);
					} else {
						SPCacheUtil.populateChargingStep(steps, (StepChargingDTO) step, ((ServiceProvProductDTO) serviceProvDTO)
								.getSpSubType().getChargingType());
					}
					
				} else if (step instanceof StepDataBaseDTO) {
					// populates database step here
					SPCacheUtil.populateDatabaseStep(steps, (StepDataBaseDTO) step,
							((ServiceProvProductDTO) serviceProvDTO).getProductId());
				} else if (step instanceof StepNotificationOtherDTO) {
					// populates other notifications here
					SPCacheUtil.populateOfflineNotificationStep(steps, (StepNotificationOtherDTO) step);
				} else if (step instanceof StepNotificationDTO) {
					// populates notifications here
					final StepNotificationDTO notificationStep = (StepNotificationDTO) step;
					final List<ServiceProvNotificationDTO> spNotifications = notificationStep.getSpNotificationList();
					fdpServiceProvisioningNotification = (ServiceProvisioningNotificationImpl) SPCacheUtil
							.getPopulateNotifications(spNotifications);
				} else if (step instanceof StepSPServiceDTO) {
					// populates other notifications here
					SPCacheUtil.populateSPServiceStep(steps, (StepSPServiceDTO) step);
				}
			}
		}

		return new ServiceProvisioningRule(steps, fdpServiceProvisioningNotification,
				serviceProvDTO.isRollBack(),serviceProvDTO);
	}
	
	private static Discount populateDiscount() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Populate product charging with discount.
	 * @param steps
	 * @param step
	 * @param chargingType
	 */
	public static void populateChargingStep(final List<FDPStep> steps, final StepChargingDTO step,
			final ChargingType chargingType, final Discount discount) {
		// put charging step here id this is of Product type SP
		final ProductChargingStepImpl fdpProductChargingStepImpl = new ProductChargingStepImpl(step.getStepId(),
				step.getStepName(), chargingType,discount);
		steps.add(fdpProductChargingStepImpl);
	}
}