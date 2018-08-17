package com.ericsson.fdp.business.monitoring.listener;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.inject.Inject;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import org.dozer.DozerBeanMapperSingletonWrapper;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

import com.ericsson.fdp.business.smsc.router.FDPSMSCRouter;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.util.DozerHelper;
import com.ericsson.fdp.core.dsm.DSMService;
import com.ericsson.fdp.core.dsm.framework.CacheQueueConstants;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.dsm.impl.DSMServiceImpl;
import com.ericsson.fdp.core.logging.LogLevelChangerRequest;
import com.ericsson.fdp.core.logging.LoggerType;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationUtil;
import com.ericsson.fdp.dao.constants.QueryConstants;
import com.ericsson.fdp.dao.dto.ExternalSystemUpdationRequest;
import com.ericsson.fdp.dao.dto.FDPCircleDTO;
import com.ericsson.fdp.dao.dto.FDPCodeTypeDTO;
import com.ericsson.fdp.dao.dto.FDPNodeExternalSystemMappingDTO;
import com.ericsson.fdp.dao.dto.FDPSMSCConfigDTO;
import com.ericsson.fdp.dao.dto.PublicationRequest;
import com.ericsson.fdp.dao.dto.SMPPServerMappingDTO;
import com.ericsson.fdp.dao.dto.SMSCUpdateDTO;
import com.ericsson.fdp.dao.entity.FDPAdminCodeType;
import com.ericsson.fdp.dao.entity.FDPNodeExternalSystemMapping;
import com.ericsson.fdp.dao.entity.FDPSMSCConfig;
import com.ericsson.fdp.dao.enums.CodeTypeEnum;
import com.ericsson.fdp.dao.enums.Operation;
import com.ericsson.fdp.dao.fdpadmin.FDPCodeTypeDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPNodeExternalSystemMappingDAO;
import com.ericsson.fdp.dao.util.PasswordEncyrptDecryptUtil;
import com.ericsson.fdp.route.controller.FDPRouteControlManager;

@MessageDriven(mappedName = CacheQueueConstants.JMS_MONITORING_TOPIC, activationConfig = {
		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge"),
		@ActivationConfigProperty(propertyName = "destination", propertyValue = "topic/monitoringTopic"),
		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Topic") })
public class RequestMonitorListener implements MessageListener {

	@Resource(lookup = "java:app/fdpBusiness-1.0/FDPRouteControlManagerImpl")
	private FDPRouteControlManager routeControlManager;

	/** The request cache. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/ApplicationConfigCache")
	private FDPCache<FDPAppBag, Object> applicationConfigCache;
	
	@Inject
	private FDPSMSCRouter fdpSMSCRouter;
	
	@PersistenceContext(unitName = "primary")
	protected EntityManager entityManager;
	
	@Inject
	FDPCodeTypeDAO codeTypeDAO;
	
	@Inject
	FDPNodeExternalSystemMappingDAO fdpNodeExternalSystemMappingDAO;

	/** The Constant DSMSERVICE. */
	private static final DSMService DSMSERVICE = DSMServiceImpl.getInstance();

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestMonitorListener.class);
	
	private static final Mapper MAPPER = DozerBeanMapperSingletonWrapper.getInstance();
	
	private static final String START_NODE ="start";
	private static final String STOP_NODE ="stop";

	@Override
	public void onMessage(final Message message) {

		LOGGER.info("RequestMonitorListener called");
		try {
			if (message.getBooleanProperty("IS_USSD_UPDATED")) {
				return;
			}
		} catch (final JMSException e1) {
		}
		LOGGER.debug("Updating Route -");
		final ObjectMessage objectMessage = (ObjectMessage) message;
		PublicationRequest pub = null;
		try {
			pub = (PublicationRequest) objectMessage.getObject();
			if (pub instanceof ExternalSystemUpdationRequest) {
				final ExternalSystemUpdationRequest externalSystemUpdationRequest = (ExternalSystemUpdationRequest) pub;
				final Operation operation = externalSystemUpdationRequest.getOperation();
				if (Operation.ADD.equals(operation)) {
					routeControlManager.addRoutes(externalSystemUpdationRequest.getNewExternalSystem());
				} else if (Operation.UPDATE.equals(operation)) {
					routeControlManager.editRoutes(externalSystemUpdationRequest.getOldExternalSystem(),
							externalSystemUpdationRequest.getNewExternalSystem());
				} else if (Operation.DELETE.equals(operation)) {
					routeControlManager.removeRoute(externalSystemUpdationRequest.getOldExternalSystem());
				} else {
					throw new IllegalArgumentException("Invalid Operation.");
				}
				LOGGER.info("RequestMonitorListener called externalSystemUpdationRequest ");
				externalSystemUpdationRequest.getNewExternalSystem();
			} else if (pub instanceof LogLevelChangerRequest) {
				LOGGER.info("else RequestMonitorListener called logLevelChangerRequest ");
				final LogLevelChangerRequest logLevelChangerRequest = (LogLevelChangerRequest) pub;
				final LoggerType loggerType = logLevelChangerRequest.getLoggerType();
				loggerType.setCircleName(loggerType.getCircleName());
				loggerType.setModuleName(loggerType.getModuleName());
				loggerType.setAppenderName(loggerType.getAppenderName());
				final ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) DSMSERVICE
						.getValue(loggerType);
				logger.setLevel(Level.toLevel(logLevelChangerRequest.getLevel()));
			} else if (pub instanceof SMSCUpdateDTO) {
				final SMSCUpdateDTO smscUpdateDTO = (SMSCUpdateDTO) pub;
				String action = smscUpdateDTO.getAction();
				LOGGER.info("Request received to " + action + " SMSC");
				
				if (null != action && action.equalsIgnoreCase(START_NODE)) {
					fdpSMSCRouter.createRoutes(true);
				}else if (null != action && action.equalsIgnoreCase(STOP_NODE)) {
					deactivateSMSCRoute(false);
				}
			} 
		} catch (final JMSException e) {
			e.printStackTrace();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}
	
	private void deactivateSMSCRoute(boolean isActive){
		
		//final List<FDPNodeExternalSystemMappingDTO> fdpNodeExternalSymMappingDTO = getExtSysMapping();
		final List<FDPNodeExternalSystemMappingDTO> fdpNodeExternalSymMappingDTO = fdpNodeExternalSystemMappingDAO.getMappingByServerName(
				ApplicationUtil.getServerName(), "SMSC_TYPE");
		final List<Long> smscConfigIDs = new ArrayList<Long>();
		for (final FDPNodeExternalSystemMappingDTO mappingDTO : fdpNodeExternalSymMappingDTO) {
			smscConfigIDs.add(mappingDTO.getEntityId());
		}
		TypedQuery<FDPSMSCConfig> typedQuery = entityManager.createNamedQuery(
				QueryConstants.JPQL_GET_SMSC_CONFIG_LIST, FDPSMSCConfig.class).setParameter("idList", smscConfigIDs)
				.setParameter("bNode","receiver");
		final List<FDPSMSCConfigDTO> fdpSMSCConfigDTOList = new ArrayList<>();		
		final List<FDPSMSCConfig> fdpSMSCConfig = typedQuery.getResultList();
		for (Iterator<FDPSMSCConfig> iterator = fdpSMSCConfig.iterator(); iterator.hasNext();) {
			FDPSMSCConfigDTO fdpSMSCConfigDTO = new FDPSMSCConfigDTO();
			MAPPER.map((FDPSMSCConfig) iterator.next(), fdpSMSCConfigDTO);
			setSystemMapping(fdpNodeExternalSymMappingDTO, fdpSMSCConfigDTO);
			fdpSMSCConfigDTOList.add(fdpSMSCConfigDTO);
		}
		updateSMSCRoute(fdpSMSCConfigDTOList,isActive);
		
	}
	
	/*private List<FDPNodeExternalSystemMappingDTO> getExtSysMapping(){
		final List<FDPNodeExternalSystemMapping> entityList = entityManager
				.createNamedQuery(QueryConstants.JPQL_GET_SMSC_ID, FDPNodeExternalSystemMapping.class)
					.setParameter("systemType", "SMSC_TYPE")
					.getResultList();
		final List<FDPNodeExternalSystemMappingDTO> fdpNodeExternalSymMappingDTO = DozerHelper.map(MAPPER, entityList,
					FDPNodeExternalSystemMappingDTO.class);
		for (final FDPNodeExternalSystemMappingDTO fdpNodeExternalSystemMappingDTO : fdpNodeExternalSymMappingDTO) {
				fdpNodeExternalSystemMappingDTO.setSystemPassword(getDecodedPassword(fdpNodeExternalSystemMappingDTO
						.getSystemPassword()));
		}
		return fdpNodeExternalSymMappingDTO;
	}*/
	
	private void setSystemMapping(List<FDPNodeExternalSystemMappingDTO> fdpNodeExternalSymMappingDTO, FDPSMSCConfigDTO fdpSMSCConfigDTO){
		final List<FDPNodeExternalSystemMappingDTO> systemMappings = new ArrayList<>();
		for (final FDPNodeExternalSystemMappingDTO nodeMappingDTO : fdpNodeExternalSymMappingDTO) {
			if (fdpSMSCConfigDTO.getExternalSystemId() == nodeMappingDTO.getEntityId()) {
				systemMappings.add(nodeMappingDTO);
				break;
			}
		}
		fdpSMSCConfigDTO.setSystemMappings(systemMappings);
	}
	
	private void updateSMSCRoute(final List<FDPSMSCConfigDTO> fdpSMSCConfigDTOList,boolean isActive){
		if(null != fdpSMSCConfigDTOList){
			for(FDPSMSCConfigDTO smscConfigDTO:fdpSMSCConfigDTOList){
				smscConfigDTO.setIsActive(isActive);
				ExternalSystemUpdationRequest esUpdateRequest = new ExternalSystemUpdationRequest();
				if (smscConfigDTO.getExternalSystemId() != null) {
					esUpdateRequest.setOperation(Operation.UPDATE);
					esUpdateRequest.setNewExternalSystem(smscConfigDTO);
					esUpdateRequest.setOldExternalSystem(new FDPSMSCConfigDTO(smscConfigDTO));
					esUpdateRequest.getOldExternalSystem().setPort(smscConfigDTO.getPort());
				}				
				LOGGER.info(" called publisher for smscConfigDTO getIp:"+smscConfigDTO.getIp());
				SMPPServerMappingDTO smppServerMappingDTO = getSMPPServerMappingDTO(smscConfigDTO, CodeTypeEnum.SMSC_CONFIG);
				esUpdateRequest.setNewExternalSystem(smppServerMappingDTO);
				LOGGER.info(" called publisher for smppServerMappingDTO getIp:"+smppServerMappingDTO.getIp());
				try {
					//publisher.pushToTopic(esUpdateRequest);
					routeControlManager.editRoutes(esUpdateRequest.getOldExternalSystem(),
							esUpdateRequest.getNewExternalSystem());
				} catch (Exception e) {
					LOGGER.error("System already exists.");
					e.printStackTrace();
				}
				
			}
		}
	}
	
	/*private String getDecodedPassword(final String encodedPassword) {
		String decodedPassword = PasswordEncyrptDecryptUtil.getDecodedPassword(encodedPassword);
		return decodedPassword;
	}*/
	
	private SMPPServerMappingDTO getSMPPServerMappingDTO(FDPSMSCConfigDTO smscConfigDTO, CodeTypeEnum aCodeTypeEnum) {
		final List<FDPAdminCodeType> fdpCodeTypeList = getCodeType(aCodeTypeEnum, smscConfigDTO.getExternalSystemId().toString());
		List<FDPCodeTypeDTO> codeTypes =DozerHelper.map(MAPPER, fdpCodeTypeList, FDPCodeTypeDTO.class);
		List<String> circleCodes = new ArrayList<String>();
		for (FDPCodeTypeDTO codeType : codeTypes) {
			circleCodes.add(codeType.getKey());
		}
		SMPPServerMappingDTO smppServerMappingDTO = MAPPER.map(smscConfigDTO, SMPPServerMappingDTO.class);
		smppServerMappingDTO.setCircleCodes(circleCodes);
		return smppServerMappingDTO;
	}
	private List<FDPAdminCodeType> getCodeType(final CodeTypeEnum codeType, final String value) {
		final TypedQuery<FDPAdminCodeType> query =
				entityManager.createNamedQuery(QueryConstants.JPQL_FIND_FDP_CODE_TYPE_BY_CODE_TYPE_AND_VALUE,
						FDPAdminCodeType.class);
		query.setParameter("codeType", codeType);
		query.setParameter("value", value);
		final List<FDPAdminCodeType> fdpCodeTypeList = query.getResultList();
		return fdpCodeTypeList;
	}
}
