package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;

import org.apache.commons.collections.CollectionUtils;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.CommandCacheUtil;
import com.ericsson.fdp.business.cache.datageneration.service.AIRRechargeDataService;
import com.ericsson.fdp.business.constants.BusinessConstants;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.rule.Rule;
import com.ericsson.fdp.business.rule.impl.RuleImpl;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.step.impl.AttachProvisioningConditionStep;
import com.ericsson.fdp.business.step.impl.AttachProvisioningStep;
import com.ericsson.fdp.business.step.impl.CommandStep;
import com.ericsson.fdp.business.step.impl.ServiceStep;
import com.ericsson.fdp.business.util.ExpressionUtil;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.CommandDTO;
import com.ericsson.fdp.dao.dto.FDPAIRRechargeProvCommandDTO;
import com.ericsson.fdp.dao.dto.FDPAIRRechargeStepDTO;
import com.ericsson.fdp.dao.dto.FDPCommandCircleDTO;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.airrecharge.FDPAIRRechargeEMAServiceDTO;
import com.ericsson.fdp.dao.dto.airrecharge.FDPAIRRechargeServiceDTO;
import com.ericsson.fdp.dao.dto.common.constraint.SubStepConstraintDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;

/**
 * The Class AIRRechargeDataServiceImpl.
 *
 * @author Ericsson
 */
@Stateless(mappedName = "airRechargeDataService")
public class AIRRechargeDataServiceImpl implements AIRRechargeDataService {

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	@Override
	public boolean updateMetaCache(final UpdateCacheDTO updateCacheDTO) throws FDPServiceException {
		Long airRechargeStepId = updateCacheDTO.getId();
		final FDPCircle circle = updateCacheDTO.getCircle();
		if (ActionTypeEnum.DELETE.equals(updateCacheDTO.getAction())) {
			// remove the cached object
			fdpCache.removeKey(new FDPMetaBag(circle, ModuleType.AIR_RECHARGE, BusinessConstants.AIR_RECHARGE_STEP_ID));
		} else {
			this.initializeUpdateAIRRechargeRule(entityService.getAIRRechargeSteps(airRechargeStepId), circle);
		}
		return true;
	}

	@Override
	public boolean initializeMetaCache(final FDPCircle fdpCircle) throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle cannot be null");
		} else {
			this.initializeUpdateAIRRechargeRule(entityService.getAIRRechargeSteps(fdpCircle), fdpCircle);
		}
		return true;
	}

	/**
	 * Initialize update air recharge rule.
	 *
	 * @param fdpAirRechargeStepDTOMap
	 *            the fdp air recharge step dto map
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	private void initializeUpdateAIRRechargeRule(final List<FDPAIRRechargeStepDTO> fdpAirRechargeStepDTOs,
			final FDPCircle fdpCircle) throws FDPServiceException {
		final List<FDPStep> steps = new ArrayList<FDPStep>();
		final List<AttachProvisioningConditionStep> attachProvisioningConditionSteps = new ArrayList<AttachProvisioningConditionStep>();
		for (final FDPAIRRechargeStepDTO fdpAirRechargeStepDTO : fdpAirRechargeStepDTOs) {
			final List<SubStepConstraintDTO> constraintDTOList = fdpAirRechargeStepDTO.getConstraintDTOList();
			// create expression for constraints here
			final Expression expression = this.createConstraintsExpression(constraintDTOList);
			// populate commands here
			/*final List<FDPStep> commandAndServiceSteps = this.populateCommandAndServiceSteps(
					fdpAirRechargeStepDTO.getProvCommandDTOs(), fdpAirRechargeStepDTO.getEmaServiceDTOList());*/
			final List<FDPStep> commandAndServiceSteps = this.populateCommandAndServiceSteps(
					fdpAirRechargeStepDTO.getProvCommandDTOs(), fdpAirRechargeStepDTO.getAirRechargeServiceDTOList());

			attachProvisioningConditionSteps.add(new AttachProvisioningConditionStep(expression,
					commandAndServiceSteps, fdpAirRechargeStepDTO.getStepId(), fdpAirRechargeStepDTO.getStepName()));
		}
		// TODO change stepId and name
		steps.add(new AttachProvisioningStep(attachProvisioningConditionSteps, 1L, FDPConstant.AIR_CHARCHING_STEP));
		final Rule fdpRule = new RuleImpl(steps, true);
		final FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.AIR_RECHARGE,
				BusinessConstants.AIR_RECHARGE_STEP_ID);
		fdpCache.putValue(metaBag, fdpRule);

	}

	/**
	 * Creates the constraints expression.
	 *
	 * @param constraintDTOList
	 *            the constraint dto list
	 * @return the expression
	 * @throws FDPServiceException
	 *             the fDP service exception
	 */
	private Expression createConstraintsExpression(final List<SubStepConstraintDTO> constraintDTOList)
			throws FDPServiceException {
		Expression expression = null;
		try {
			expression = ExpressionUtil.createExpressionForSubStep(constraintDTOList);
		} catch (final ExpressionFailedException e) {
			throw new FDPServiceException(e);
		}
		return expression;
	}

	/**
	 * Populate step constraint commands.
	 *
	 * @param emaServices
	 *
	 * @param spCommandDTOList
	 *            the sp command dto list
	 * @return the list
	 *//*
	private List<FDPStep> populateCommandAndServiceSteps(
			final List<FDPAIRRechargeProvCommandDTO> fdpAirRechargeProvCommandDTOs,
			final List<FDPAIRRechargeEMAServiceDTO> emaServices) {
		List<FDPStep> commandSteps = null;
		long index = 1;
		if (CollectionUtils.isNotEmpty(fdpAirRechargeProvCommandDTOs)) {
			commandSteps = new ArrayList<FDPStep>(fdpAirRechargeProvCommandDTOs.size());
			for (final FDPAIRRechargeProvCommandDTO rechargeProvCommandDTO : fdpAirRechargeProvCommandDTOs) {
				final FDPCommandCircleDTO commandCircleDTO = rechargeProvCommandDTO.getCommandCircleDTO();
				if (null == commandCircleDTO) {
					continue;
				}
				final CommandDTO commandDTO = commandCircleDTO.getCommandDTO();
				final FDPCommand fdpCommand = CommandCacheUtil.commandDTOToFDPCommand(commandDTO);
				if (null != fdpCommand) {
					final CommandStep commandStep = new CommandStep(fdpCommand, commandDTO.getCommandDisplayName(),
							index, FDPConstant.COMMAND_STEP + index);
					commandSteps.add(commandStep);
					index++;
				}
			}
		}
		if (CollectionUtils.isNotEmpty(emaServices)) {
			if (commandSteps == null) {
				commandSteps = new ArrayList<FDPStep>(emaServices.size());
			}
			for (final FDPAIRRechargeEMAServiceDTO emaService : emaServices) {
				final ServiceStep serviceStep = new ServiceStep(emaService.getServiceValue(), index,
						emaService.getServiceName());
				serviceStep.putAdditionalInformation(ServiceStepOptions.ACTION, emaService.getServiceSubType());
				serviceStep.putAdditionalInformation(ServiceStepOptions.MODE, emaService.getServiceMode());
				serviceStep.putAdditionalInformation(ServiceStepOptions.CHECK_ICR_CIRCLE,
						emaService.getWhiteListLookup());
				commandSteps.add(serviceStep);
				index++;
			}
		}
		return commandSteps;
	}*/
	
	/**
	 * Populate step constraint commands.
	 *
	 * @param emaServices
	 *
	 * @param spCommandDTOList
	 *            the sp command dto list
	 * @return the list
	 */
	private List<FDPStep> populateCommandAndServiceSteps(
			final List<FDPAIRRechargeProvCommandDTO> fdpAirRechargeProvCommandDTOs,
			final List<FDPAIRRechargeServiceDTO>  fdpairRechargeServiceDTOs) {
		List<FDPStep> commandSteps = null;
		long index = 1;
		if (CollectionUtils.isNotEmpty(fdpAirRechargeProvCommandDTOs)) {
			commandSteps = new ArrayList<FDPStep>(fdpAirRechargeProvCommandDTOs.size());
			for (final FDPAIRRechargeProvCommandDTO rechargeProvCommandDTO : fdpAirRechargeProvCommandDTOs) {
				final FDPCommandCircleDTO commandCircleDTO = rechargeProvCommandDTO.getCommandCircleDTO();
				if (null == commandCircleDTO) {
					continue;
				}
				final CommandDTO commandDTO = commandCircleDTO.getCommandDTO();
				final FDPCommand fdpCommand = CommandCacheUtil.commandDTOToFDPCommand(commandDTO);
				if (null != fdpCommand) {
					final CommandStep commandStep = new CommandStep(fdpCommand, commandDTO.getCommandDisplayName(),
							index, FDPConstant.COMMAND_STEP + index);
					commandSteps.add(commandStep);
					index++;
				}
			}
		}
		if (CollectionUtils.isNotEmpty(fdpairRechargeServiceDTOs)) {
			if (commandSteps == null) {
				commandSteps = new ArrayList<FDPStep>(fdpairRechargeServiceDTOs.size());
			}
			for (final FDPAIRRechargeServiceDTO fdpairRechargeServiceDTO : fdpairRechargeServiceDTOs) {
				final ServiceStep serviceStep = new ServiceStep(fdpairRechargeServiceDTO.getServiceValue(), index,
						fdpairRechargeServiceDTO.getServiceName());
				/*serviceStep.putAdditionalInformation(ServiceStepOptions.ACTION, emaService.getServiceSubType());
				serviceStep.putAdditionalInformation(ServiceStepOptions.MODE, emaService.getServiceMode());*/
				serviceStep.putAdditionalInformation(ServiceStepOptions.CHECK_ICR_CIRCLE,
						fdpairRechargeServiceDTO.getWhiteListLookup());
				commandSteps.add(serviceStep);
				index++;
			}
		}
		return commandSteps;
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.AIR_RECHARGE;
	}

}
