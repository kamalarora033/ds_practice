package com.ericsson.fdp.business.cache.datageneration.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.datageneration.service.DMDataService;
import com.ericsson.fdp.business.enums.DynamicMenuPaginationKey;
import com.ericsson.fdp.business.enums.comparators.SMSCPaginationOptionComparator;
import com.ericsson.fdp.business.enums.comparators.USSDPaginationOptionComparator;
import com.ericsson.fdp.business.exception.ExpressionFailedException;
import com.ericsson.fdp.business.expression.Expression;
import com.ericsson.fdp.business.node.AbstractMenuNode;
import com.ericsson.fdp.business.node.AbstractNode;
import com.ericsson.fdp.business.node.impl.ConstraintNode;
import com.ericsson.fdp.business.node.impl.ExitMenuNode;
import com.ericsson.fdp.business.node.impl.InfoNode;
import com.ericsson.fdp.business.node.impl.NoConstraintMenuNode;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.node.impl.ReturnMenuNode;
import com.ericsson.fdp.business.node.impl.RootNode;
import com.ericsson.fdp.business.node.impl.SpecialMenuNode;
import com.ericsson.fdp.business.util.DynamicMenuUtil;
import com.ericsson.fdp.business.util.ExpressionUtil;
import com.ericsson.fdp.business.vo.NodeAliasCode;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.DynamicMenuAddInfoMessageType;
import com.ericsson.fdp.common.enums.DynamicMenuAdditionalInfoKey;
import com.ericsson.fdp.common.enums.FDPNodeAddInfoKeys;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.TariffEnquiryAttributeKeysEnum;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.exception.FDPServiceException;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.entity.service.EntityService;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.dao.dto.UpdateCacheDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.BaseNodeDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.ConstraintNodeDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.ConstraintOtherNodeDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.EntityNodeDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.ExitNodeDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.InformationNodeDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.KeywordNodeDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.NoConstraintNodeDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.NoConstraintOtherNodeDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.PreviousMenuNodeDTO;
import com.ericsson.fdp.dao.dto.dynamicMenu.ProductNodeDTO;
import com.ericsson.fdp.dao.dto.product.DynamicMenuAliasCodesDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvDTO;
import com.ericsson.fdp.dao.dto.serviceprov.ServiceProvProductDTO;
import com.ericsson.fdp.dao.enums.ActionTypeEnum;
import com.ericsson.fdp.dao.enums.CodeTypeEnum;
import com.ericsson.fdp.dao.enums.EntityType;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;
import com.ericsson.fdp.dao.enums.LanguageType;
import com.ericsson.fdp.dao.enums.UtUpgradeType;
import com.ericsson.fdp.dao.exception.FDPDataNotFoundException;
import com.ericsson.fdp.dao.fdpadmin.FDPCodeValueDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPDynamicMenuAliasCodeDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPServiceProvDAO;

/**
 * The Class DMDataServiceImpl.
 *
 * @author Ericsson
 */
@Stateless(mappedName = "dMDataService")
public class DMDataServiceImpl implements DMDataService {

	/** The fdp cache. */
	@Resource(lookup = "java:app/fdpBusiness-1.0/MetaDataCache")
	private FDPCache<FDPMetaBag, FDPCacheable> fdpCache;

	/** The entity service. */
	@Resource(lookup = "java:app/fdpCoreServices-1.0/EntityServiceImpl")
	private EntityService entityService;

	/** The fdp service prov dao. */
	@Inject
	private FDPServiceProvDAO fdpServiceProvDAO;

	/** The fdp code value dao. */
	@Inject
	private FDPCodeValueDAO fdpCodeValueDAO;

	/** The fdp dynamic menu alias code dao. */
	@Inject
	FDPDynamicMenuAliasCodeDAO fdpDynamicMenuAliasCodeDAO;

	/** The Constant LOGGER. */
	private static final Logger LOGGER = LoggerFactory.getLogger(DMDataServiceImpl.class);

	@Override
	public boolean updateMetaCache(final UpdateCacheDTO updateCacheDTO) throws FDPServiceException {
		Long dmId = null;
		FDPCircle fdpCircle = null;
		if (updateCacheDTO != null) {
			dmId = updateCacheDTO.getId();
			fdpCircle = updateCacheDTO.getCircle();
			if (updateCacheDTO.getUiObjectDTO() != null
					&& updateCacheDTO.getUiObjectDTO() instanceof DynamicMenuAliasCodesDTO) {
				DynamicMenuAliasCodesDTO dynamicMenuAliasCodesDTO = (DynamicMenuAliasCodesDTO) updateCacheDTO
						.getUiObjectDTO();
				if(dynamicMenuAliasCodesDTO.getPreviousAlias() != null) {
					String previousAliasCode = dynamicMenuAliasCodesDTO.getPreviousAlias().toLowerCase();
					previousAliasCode = "SMS".equalsIgnoreCase(dynamicMenuAliasCodesDTO.getChannelName()) ? previousAliasCode
							.toLowerCase() : previousAliasCode;

					FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.DYNAMIC_MENU_CODE_ALIAS, previousAliasCode);
					LOGGER.debug("Inside initializeUpdateDMCache--circle name --" + fdpCircle
							+ " previousAliasCode name --" + previousAliasCode);
					fdpCache.removeKey(metaBag);
				}
			}
			for (BaseNodeDTO baseNodeDTO : entityService.getActiveDynamicMenus(dmId)) {
				try {
					removeChildForNode(fdpCircle, baseNodeDTO);
					this.initializeUpdateDM(baseNodeDTO, fdpCircle);
				} catch (ExpressionFailedException e) {
					throw new FDPServiceException(e);
				}
			}

			if (ActionTypeEnum.DELETE.equals(updateCacheDTO.getAction())) {
				// Refresh the complete cache.
				dmId = null;
				fdpCircle = null;
			}
		}
		return false;
	}

	@Override
	public boolean initializeMetaCache(final FDPCircle fdpCircle) throws FDPServiceException {
		if (fdpCircle == null) {
			throw new FDPServiceException("Circle cannot be null");
		} else {
			for (BaseNodeDTO baseNodeDTO : entityService.getActiveDynamicMenus(fdpCircle)) {
				try {
					removeChildForNode(fdpCircle, baseNodeDTO);
					this.initializeUpdateDM(baseNodeDTO, fdpCircle);
				} catch (ExpressionFailedException e) {
					throw new FDPServiceException(e);
				}
			}
		}
		return true;
	}

	/**
	 * Removes the child for node.
	 *
	 * @param fdpCircle
	 *            the fdp circle
	 * @param baseNodeDTO
	 *            the base node dto
	 */
	private void removeChildForNode(final FDPCircle fdpCircle, final BaseNodeDTO baseNodeDTO) {
		final String code = "SMS".equalsIgnoreCase(baseNodeDTO.getChannelName()) ? baseNodeDTO.getCode().toLowerCase()
				: baseNodeDTO.getCode();
		final FDPCacheable baseNodeCacheable = fdpCache.getValue(new FDPMetaBag(fdpCircle, ModuleType.DM, code));
		if (baseNodeCacheable != null)
			LOGGER.debug("Inside removeChildForNode baseNodeCacheable data  --" + baseNodeCacheable.toString());
		if (baseNodeCacheable instanceof FDPNode) {
			final FDPNode fdpNode = (FDPNode) baseNodeCacheable;
			// remove all children from the cache for this base node
			removeChildForNode(fdpCircle, fdpNode, baseNodeDTO.getChannelName());
		}
		LOGGER.debug("Inside removeChildForNode with 2 parameter--circle name --" + fdpCircle
				+ " previousAliasCode name --" + code);
		fdpCache.removeKey(new FDPMetaBag(fdpCircle, ModuleType.DM, code));
	}

	/**
	 * Removes the child for node.
	 *
	 * @param fdpCircle
	 *            the fdp circle
	 * @param fdpNode
	 *            the fdp node
	 * @param channel
	 *            the channel
	 */
	private void removeChildForNode(final FDPCircle fdpCircle, final FDPNode fdpNode, final String channel) {
		// remove all children from the cache for this base node
		if (fdpNode.getChildren() != null) {
			for (final FDPNode childNode : fdpNode.getChildren()) {
				removeChildForNode(fdpCircle, childNode, channel);
				final String code = "SMS".equalsIgnoreCase(channel) ? childNode.getFullyQualifiedPath().toLowerCase()
						: childNode.getFullyQualifiedPath();
				LOGGER.debug("Inside removeChildForNode with 3 parameter--circle name --" + fdpCircle
						+ " previousAliasCode name --" + code.toString());
				fdpCache.removeKey(new FDPMetaBag(fdpCircle, ModuleType.DM, code));
			}
		}
	}

	/**
	 * Initialize update dm.
	 *
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param fdpCircle
	 *            the fdp circle
	 * @return the fDP node
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private void initializeUpdateDM(final BaseNodeDTO baseNodeDTO, final FDPCircle fdpCircle)
			throws ExpressionFailedException {
		FDPCircle actCircle = fdpCircle;
		if (actCircle == null) {
			actCircle = baseNodeDTO.getCircle();
		}
		final Map<String, FDPNode> dmMap = new LinkedHashMap<String, FDPNode>();
		// fullyQualifiedPathToSet is like *121*3#
		RootNode rootNode = null;
		RootNode childRootNode = null;
		// if (baseNodeDTO.getChannelName().equalsIgnoreCase("USSD")) {
		rootNode = new RootNode(baseNodeDTO.getDefaultOption(), baseNodeDTO.getName(), null, baseNodeDTO.getCode(),
				(baseNodeDTO.getChannelName() == null ? null : ChannelType.valueOf(baseNodeDTO.getChannelName())),
				actCircle, (baseNodeDTO.getPriority() == null ? null : Long.valueOf(baseNodeDTO.getPriority())),DynamicMenuUtil.getDMState(baseNodeDTO.getStatus()));
		updateDMNodeAdditionalInfo(rootNode, baseNodeDTO, baseNodeDTO.getChannelName());
		// }
		final List<BaseNodeDTO> childList = baseNodeDTO.getChildList();
		List<FDPNode> childs = null;
		//System.out.println("DMDataService channel name = "+baseNodeDTO.getChannelName());
		childRootNode = (baseNodeDTO.getChannelName().equalsIgnoreCase("USSD") || baseNodeDTO.getChannelName().equalsIgnoreCase("IVR") || baseNodeDTO.getChannelName().equalsIgnoreCase("WEB")) ? rootNode : null;
		if (childList != null && !childList.isEmpty()) {
			childs = getChildList(childList,childRootNode,
					actCircle, dmMap, baseNodeDTO.getChannelName());
			//System.out.println("childList ="+childList.toString()+" childRootNode="+childRootNode+" childs = "+childs.toString());
		}
		if (rootNode != null) {
			// set the parent null for root node
			rootNode.setParent(null);
			// set the children of the root node
			rootNode.setChildren(childs);
			dmMap.put(rootNode.getFullyQualifiedPath(), rootNode);
			final String code = baseNodeDTO.getChannelName().equalsIgnoreCase("SMS") ? baseNodeDTO.getCode()
					.toLowerCase() : baseNodeDTO.getCode();
					//System.out.println("DMDataService code ="+code +" rootNode="+rootNode.toString());
			LOGGER.debug("Inside initializeUpdateDM with 2 parameter--circle name --" + actCircle + " code name --"
					+ code + "rootNode---" + rootNode.toString());
			fdpCache.putValue(new FDPMetaBag(actCircle, ModuleType.DM, code), rootNode);
		}
	}

	/**
	 * Update dm node additional info.
	 *
	 * @param rootNode
	 *            the root node
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param channel
	 *            the channel
	 */
	private void updateDMNodeAdditionalInfo(final AbstractMenuNode rootNode, final BaseNodeDTO baseNodeDTO,
			final String channel) {
		if (baseNodeDTO.getAddInfo() != null) {
			final Map<DynamicMenuAdditionalInfoKey, String> addInfoMap = baseNodeDTO.getAddInfo().getAddInfoMap();
			if (Boolean.valueOf(addInfoMap.get(DynamicMenuAdditionalInfoKey.MARKETING_MESSAGE_ENABLED))) {
				rootNode.setMarketingMessage(addInfoMap.get(DynamicMenuAdditionalInfoKey.MARKETING_MESSAGE_TEXT));
				if (addInfoMap.get(DynamicMenuAdditionalInfoKey.MARKETING_MESSAGE_TYPE).equals(
						DynamicMenuAddInfoMessageType.CONCATENATE.getValue())) {
					rootNode.setConcatenateMarketingMessage(true);
				} else {
					rootNode.setConcatenateMarketingMessage(false);
				}
			}
			if (Boolean.valueOf(addInfoMap.get(DynamicMenuAdditionalInfoKey.MENU_HEADER_STATUS))) {
				Map<Integer, String> otherLangHeader= new HashMap<Integer, String>();
				if(null != addInfoMap.get(DynamicMenuAdditionalInfoKey.MENU_HEADER_TEXT))
				rootNode.setHeader(addInfoMap.get(DynamicMenuAdditionalInfoKey.MENU_HEADER_TEXT));
				// Add for French support on 19/12/16
				if(null != addInfoMap.get(DynamicMenuAdditionalInfoKey.MENU_HEADER_TEXT_FRENCH) ){ 
					otherLangHeader.put(LanguageType.FRENCH.getValue(), addInfoMap.get(DynamicMenuAdditionalInfoKey.MENU_HEADER_TEXT_FRENCH));
				}
				rootNode.setOtherLangHeader(otherLangHeader);
			}
			
			// display name in french in DM
			// create a map for other languages
			if(addInfoMap.get(DynamicMenuAdditionalInfoKey.FRENCH_DISPLAY_NAME) != null) {
						final Map<LanguageType, String> otherLanguageMap = new HashMap<LanguageType, String>();
						otherLanguageMap.put(LanguageType.FRENCH, addInfoMap.get(DynamicMenuAdditionalInfoKey.FRENCH_DISPLAY_NAME));
						rootNode.setOtherLangMap(otherLanguageMap);
			}

			updatePaginationOptions(rootNode, baseNodeDTO, channel);
		}
	}

	/**
	 * Update pagination options.
	 *
	 * @param rootNode
	 *            the root node
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param channel
	 *            the channel
	 */
	private void updatePaginationOptions(final AbstractNode rootNode, final BaseNodeDTO baseNodeDTO,
			final String channel) {
		if (baseNodeDTO.getAddInfo() != null) {
			final Map<DynamicMenuAdditionalInfoKey, String> addInfoMap = baseNodeDTO.getAddInfo().getAddInfoMap();
			final List<DynamicMenuPaginationKey> paginationOptions = new ArrayList<DynamicMenuPaginationKey>();
			if (Boolean.valueOf(addInfoMap.get(DynamicMenuAdditionalInfoKey.RETURN_TO_MAIN_MENU_STATUS))) {
				paginationOptions.add(DynamicMenuPaginationKey.RETURN_TO_MAIN_MENU_STATUS);
			}
			if (Boolean.valueOf(addInfoMap.get(DynamicMenuAdditionalInfoKey.EXIT_STATUS))) {
				paginationOptions.add(DynamicMenuPaginationKey.EXIT_STATUS);
			}
			if (Boolean.valueOf(addInfoMap.get(DynamicMenuAdditionalInfoKey.RETURN_TO_PREVIOUS_MENU_STATUS))) {
				paginationOptions.add(DynamicMenuPaginationKey.RETURN_TO_PREVIOUS_MENU_STATUS);
			}
			rootNode.setPaginationOptions(sortPaginationOptions(paginationOptions, channel));
		}
	}

	/**
	 * Sort pagination options.
	 *
	 * @param paginationOptions
	 *            the pagination options
	 * @param nodeType
	 *            the node type
	 * @return the list
	 */
	private List<DynamicMenuPaginationKey> sortPaginationOptions(
			final List<DynamicMenuPaginationKey> paginationOptions, final String nodeType) {
		Comparator<DynamicMenuPaginationKey> comparator = null;
		if (nodeType != null && nodeType.equals("USSD")) {
			comparator = new USSDPaginationOptionComparator();
		} else {
			comparator = new SMSCPaginationOptionComparator();
		}
		Collections.sort(paginationOptions, comparator);
		return paginationOptions;
	}

	/**
	 * Gets the child list.
	 *
	 * @param childList
	 *            the child list
	 * @param parentNode
	 *            the parent node
	 * @param fdpCircle
	 *            the fdp circle
	 * @param dmMap
	 *            the dm map
	 * @param channel
	 *            the channel
	 * @return the child list
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private List<FDPNode> getChildList(final List<BaseNodeDTO> childList, final FDPNode parentNode,
			final FDPCircle fdpCircle, final Map<String, FDPNode> dmMap, final String channel)
			throws ExpressionFailedException {
		if (childList == null) {
			return null;
		} else {
			final List<FDPNode> nodeList = new ArrayList<FDPNode>();
			for (final BaseNodeDTO baseNodeDTO : childList) {
				final FDPNode node = transformChildBaseNodeTOToFDPNode(baseNodeDTO, parentNode, fdpCircle, dmMap,
						channel);
				dmMap.put(node.getFullyQualifiedPath(), node);
				final String code = channel.equalsIgnoreCase("SMS") ? baseNodeDTO.getCode().toLowerCase() : baseNodeDTO
						.getCode();
				LOGGER.debug("Inside getChildList with 6 parameter--circle name --" + fdpCircle.getCircleName()
						+ " code name --" + code + "rootNode---" + node.toString());
				fdpCache.putValue(new FDPMetaBag(fdpCircle, ModuleType.DM, code), node);
				nodeList.add(node);
			}
			return nodeList;
		}
	}

	/**
	 * Transform child base node to to fdp node.
	 *
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param parentNode
	 *            the parent node
	 * @param fdpCircle
	 *            the fdp circle
	 * @param dmMap
	 *            the dm map
	 * @param channel
	 *            the channel
	 * @return the fDP node
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private FDPNode transformChildBaseNodeTOToFDPNode(final BaseNodeDTO baseNodeDTO, final FDPNode parentNode,
			final FDPCircle fdpCircle, final Map<String, FDPNode> dmMap, final String channel)
			throws ExpressionFailedException {
		FDPNode chaileNode = null;
		try {
			if (baseNodeDTO != null) {
				
				//System.out.println("======Node=======>"+baseNodeDTO);
				if (baseNodeDTO instanceof NoConstraintNodeDTO) {
					chaileNode = getNoConstraintNode(baseNodeDTO, parentNode, fdpCircle, dmMap, channel);
				} else if (baseNodeDTO instanceof ConstraintNodeDTO) {
					chaileNode = getConstraintNode(baseNodeDTO, parentNode, fdpCircle, dmMap, channel);
				} else if (baseNodeDTO instanceof ExitNodeDTO) {
					chaileNode = getExitNode(baseNodeDTO, parentNode, fdpCircle, dmMap, channel);
				} else if (baseNodeDTO instanceof PreviousMenuNodeDTO) {
					chaileNode = getReturnMenuNode(baseNodeDTO, parentNode, fdpCircle, dmMap, channel);
				} else if (baseNodeDTO instanceof ProductNodeDTO) {
					chaileNode = getProductNode(baseNodeDTO, parentNode, fdpCircle, dmMap, channel);
				} else if (baseNodeDTO instanceof NoConstraintOtherNodeDTO || baseNodeDTO instanceof ConstraintOtherNodeDTO) {
					chaileNode = getOtherNode(baseNodeDTO, parentNode, fdpCircle, dmMap, channel);
				} else if (baseNodeDTO instanceof KeywordNodeDTO) {
					chaileNode = getKeywordNode(baseNodeDTO, parentNode, fdpCircle, dmMap, channel);
				} else if (baseNodeDTO instanceof InformationNodeDTO) {
					chaileNode = getInfoNode(baseNodeDTO, parentNode, fdpCircle, dmMap, channel);
				}
			
			}
		} catch (Exception e) {
			LOGGER.debug("Error in parsing the Node ", e);
			throw e;
		}
		return chaileNode;
	}

	/**
	 * Gets the info node.
	 *
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param parentNode
	 *            the parent node
	 * @param fdpCircle
	 *            the fdp circle
	 * @param dmMap
	 *            the dm map
	 * @param channel
	 *            the channel
	 * @return the info node
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private FDPNode getInfoNode(final BaseNodeDTO baseNodeDTO, final FDPNode parentNode, final FDPCircle fdpCircle,
			final Map<String, FDPNode> dmMap, final String channel) throws ExpressionFailedException {
		final InfoNode chaileNode = new InfoNode(((InformationNodeDTO) baseNodeDTO).getFreeText(),
				baseNodeDTO.getName(), baseNodeDTO.getCode(), baseNodeDTO.getCode(),
				(baseNodeDTO.getChannelName() == null ? null : ChannelType.valueOf(baseNodeDTO.getChannelName())),
				fdpCircle, (baseNodeDTO.getPriority() == null ? null : Long.valueOf(baseNodeDTO.getPriority())),
				parentNode, null, this.getAddInfoMapForFDPNode(baseNodeDTO.getAddInfo().getAddInfoMap()),Visibility.getVisibility(baseNodeDTO.getVisibility()),DynamicMenuUtil.getDMState(baseNodeDTO.getStatus()));
		chaileNode.setChildren(getChildList(baseNodeDTO.getChildList(), chaileNode, fdpCircle, dmMap, channel));
		return chaileNode;
	}

	/**
	 * Gets the keyword node.
	 *
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param parentNode
	 *            the parent node
	 * @param fdpCircle
	 *            the fdp circle
	 * @param dmMap
	 *            the dm map
	 * @param channel
	 *            the channel
	 * @return the keyword node
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private FDPNode getKeywordNode(final BaseNodeDTO baseNodeDTO, final FDPNode parentNode, final FDPCircle fdpCircle,
			final Map<String, FDPNode> dmMap, final String channel) throws ExpressionFailedException {
		String defaultOption = baseNodeDTO.getDefaultOption();
		if (parentNode instanceof RootNode) {
			defaultOption = ((RootNode) parentNode).getHelpText();
		}
		final InfoNode chaileNode = new InfoNode(defaultOption, baseNodeDTO.getName(), baseNodeDTO.getCode(),
				baseNodeDTO.getCode(), (baseNodeDTO.getChannelName() == null ? null : ChannelType.valueOf(baseNodeDTO
						.getChannelName())), fdpCircle, (baseNodeDTO.getPriority() == null ? null
						: Long.valueOf(baseNodeDTO.getPriority())), parentNode, null,
				this.getAddInfoMapForFDPNode(baseNodeDTO.getAddInfo().getAddInfoMap()),Visibility.getVisibility(baseNodeDTO.getVisibility()),DynamicMenuUtil.getDMState(baseNodeDTO.getStatus()));
		chaileNode.setChildren(getChildList(baseNodeDTO.getChildList(), chaileNode, fdpCircle, dmMap, channel));
		return chaileNode;
	}

	/**
	 * Gets the other node.
	 *
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param parentNode
	 *            the parent node
	 * @param fdpCircle
	 *            the fdp circle
	 * @param dmMap
	 *            the dm map
	 * @param channel
	 *            the channel
	 * @return the other node
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private FDPNode getOtherNode(final BaseNodeDTO baseNodeDTO, final FDPNode parentNode, final FDPCircle fdpCircle,
			final Map<String, FDPNode> dmMap, final String channel) throws ExpressionFailedException {
		final EntityNodeDTO otherNodeDTO = (EntityNodeDTO) baseNodeDTO;
		ServiceProvDTO spDto;
		try {
			spDto = fdpServiceProvDAO.getServiceProvGeneralInfoForOthers(otherNodeDTO.getEntityValue(), LOGGER);
		} catch (final FDPDataNotFoundException e) {
			LOGGER.error(e.getMessage());
			throw new ExpressionFailedException(e.getMessage(), e);
		}

		List<String> listOfAliasCode = fdpDynamicMenuAliasCodeDAO.getDynamicMenuAliasCodeForCircleChannelCode(
				fdpCircle, baseNodeDTO.getCode(), channel);

		updateAliasCacheForProductNode(baseNodeDTO, fdpCircle, channel, listOfAliasCode);
		final SpecialMenuNode specialMenuNode = new SpecialMenuNode(baseNodeDTO.getName(), baseNodeDTO.getCode(),
				baseNodeDTO.getCode(), (baseNodeDTO.getChannelName() == null ? null : ChannelType.valueOf(baseNodeDTO
						.getChannelName())), fdpCircle, (baseNodeDTO.getPriority() == null ? null
						: Long.valueOf(baseNodeDTO.getPriority())), parentNode, null, otherNodeDTO.getEntityValue(),
				ModuleType.SP_OTHERS.toString(), ((ServiceProvProductDTO) spDto).getSpSubType(),
				this.getAddInfoMapForFDPNode(baseNodeDTO.getAddInfo().getAddInfoMap()),Visibility.getVisibility(baseNodeDTO.getVisibility()),DynamicMenuUtil.getDMState(baseNodeDTO.getStatus()));
		if (baseNodeDTO instanceof ConstraintOtherNodeDTO) {
			final Expression fdpExpression = ExpressionUtil
					.createExpressionForSteps(((ConstraintOtherNodeDTO) otherNodeDTO).getStepList());
			specialMenuNode.setFdpExpression(fdpExpression);
		}
		if (baseNodeDTO.getPolicy() != null) {
			specialMenuNode.setPolicyName(baseNodeDTO.getPolicy());
		}

		this.updatePaginationOptions(specialMenuNode, baseNodeDTO, channel);
		specialMenuNode
				.setChildren(getChildList(baseNodeDTO.getChildList(), specialMenuNode, fdpCircle, dmMap, channel));
		return specialMenuNode;
	}

	/**
	 * Gets the product node.
	 *
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param parentNode
	 *            the parent node
	 * @param fdpCircle
	 *            the fdp circle
	 * @param dmMap
	 *            the dm map
	 * @param channel
	 *            the channel
	 * @return the product node
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private FDPNode getProductNode(final BaseNodeDTO baseNodeDTO, final FDPNode parentNode, final FDPCircle fdpCircle,
			final Map<String, FDPNode> dmMap, final String channel) throws ExpressionFailedException {
		final ProductNodeDTO prodNodeDTO = (ProductNodeDTO) baseNodeDTO;
		List<String> listOfAliasCode = fdpDynamicMenuAliasCodeDAO.getDynamicMenuAliasCodeForCircleChannelCode(
				fdpCircle, baseNodeDTO.getCode(), channel);

		updateAliasCacheForProductNode(baseNodeDTO, fdpCircle, channel, listOfAliasCode);

		final ProductNode productNode = new ProductNode(baseNodeDTO.getName(), baseNodeDTO.getCode(),
				baseNodeDTO.getCode(), (baseNodeDTO.getChannelName() == null ? null : ChannelType.valueOf(baseNodeDTO
						.getChannelName())), fdpCircle, (baseNodeDTO.getPriority() == null ? null
						: Long.valueOf(baseNodeDTO.getPriority())), parentNode, null, prodNodeDTO.getEntityValue(),
				ModuleType.PRODUCT.toString(), FDPServiceProvSubType.valueOf(prodNodeDTO.getAction()), listOfAliasCode,
				this.getAddInfoMapForFDPNode(baseNodeDTO.getAddInfo().getAddInfoMap()),Visibility.getVisibility(baseNodeDTO.getVisibility()),DynamicMenuUtil.getDMState(baseNodeDTO.getStatus()));
		productNode.setPolicyName(prodNodeDTO.getPolicy());
		if (prodNodeDTO.getAction().equals(FDPServiceProvSubType.SHARED_ACCOUNT_PROVIDER_UT_UPGRADE.name())) {
			final String utUpgradeType = fdpCodeValueDAO.getEntityValue(prodNodeDTO.getId(),
					CodeTypeEnum.DM_PROV_UT_UPGRADE, EntityType.UT_UPGRADE_TYPE, LOGGER);
			productNode.addAdditionalInfo(EntityType.UT_UPGRADE_TYPE.getEntityType(),
					UtUpgradeType.valueOf(utUpgradeType));
			if (utUpgradeType.equals(UtUpgradeType.Temporary.name())) {
				final String days = fdpCodeValueDAO.getEntityValue(prodNodeDTO.getId(),
						CodeTypeEnum.DM_PROV_UT_UPGRADE, EntityType.NO_OF_DAYS, LOGGER);
				productNode.addAdditionalInfo(EntityType.NO_OF_DAYS.getEntityType(), Integer.parseInt(days));
			}
			final String newLimit = fdpCodeValueDAO.getEntityValue(prodNodeDTO.getId(),
					CodeTypeEnum.DM_PROV_UT_UPGRADE, EntityType.NEW_LIMIT, LOGGER);
			productNode.addAdditionalInfo(EntityType.NEW_LIMIT.getEntityType(), Long.parseLong(newLimit));
		}
		
		this.updatePaginationOptions(productNode, baseNodeDTO, channel);
		productNode.setChildren(getChildList(baseNodeDTO.getChildList(), productNode, fdpCircle, dmMap, channel));
		return productNode;
	}

	/**
	 * Update alias cache for product node.
	 *
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param fdpCircle
	 *            the fdp circle
	 * @param channel
	 *            the channel
	 * @param listOfAliasCode
	 *            the list of alias code
	 */
	private void updateAliasCacheForProductNode(final BaseNodeDTO baseNodeDTO, final FDPCircle fdpCircle,
			final String channel, final List<String> listOfAliasCode) {
		// update DYNAMIC_MENU_CODE_ALIAS Cache....
		if (listOfAliasCode != null && !listOfAliasCode.isEmpty()) {
			for (String aliasCode : listOfAliasCode) {
				aliasCode = "SMS".equalsIgnoreCase(channel) ? aliasCode.toLowerCase() : aliasCode;

				FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.DYNAMIC_MENU_CODE_ALIAS, aliasCode);
				NodeAliasCode nodeAliasCode = new NodeAliasCode();
				final String code = "SMS".equalsIgnoreCase(channel) ? baseNodeDTO.getCode().toLowerCase() : baseNodeDTO
						.getCode();
				nodeAliasCode.setActualCode(code);
				nodeAliasCode.setChannelType(ChannelType.valueOf(channel));
				nodeAliasCode.setFdpCircle(fdpCircle);

				nodeAliasCode.setNodeAliasCode(aliasCode);
				LOGGER.debug("Inside updateAliasCacheForProductNode with 4 parameter--circle name --"
						+ metaBag.getFdpCircle().getCircleName() + "nodeAliasCode---" + nodeAliasCode.toString());
				fdpCache.putValue(metaBag, nodeAliasCode);
			}
		}
	}

	/**
	 * Gets the return menu node.
	 *
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param parentNode
	 *            the parent node
	 * @param fdpCircle
	 *            the fdp circle
	 * @param dmMap
	 *            the dm map
	 * @param channel
	 *            the channel
	 * @return the return menu node
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private FDPNode getReturnMenuNode(final BaseNodeDTO baseNodeDTO, final FDPNode parentNode,
			final FDPCircle fdpCircle, final Map<String, FDPNode> dmMap, final String channel)
			throws ExpressionFailedException {
		
		List<String> listOfAliasCode = fdpDynamicMenuAliasCodeDAO.getDynamicMenuAliasCodeForCircleChannelCode(
				fdpCircle, baseNodeDTO.getCode(), channel);

		updateAliasCacheForProductNode(baseNodeDTO, fdpCircle, channel, listOfAliasCode);
		
		final ReturnMenuNode chaileNode = new ReturnMenuNode(baseNodeDTO.getName(), baseNodeDTO.getCode(),
				baseNodeDTO.getCode(), (baseNodeDTO.getChannelName() == null ? null : ChannelType.valueOf(baseNodeDTO
						.getChannelName())), fdpCircle, (baseNodeDTO.getPriority() == null ? null
						: Long.valueOf(baseNodeDTO.getPriority())), parentNode, null,
				this.getAddInfoMapForFDPNode(baseNodeDTO.getAddInfo().getAddInfoMap()),Visibility.getVisibility(baseNodeDTO.getVisibility()),DynamicMenuUtil.getDMState(baseNodeDTO.getStatus()));
		chaileNode.setChildren(getChildList(baseNodeDTO.getChildList(), chaileNode, fdpCircle, dmMap, channel));
		return chaileNode;
	}

	/**
	 * Gets the exit node.
	 *
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param parentNode
	 *            the parent node
	 * @param fdpCircle
	 *            the fdp circle
	 * @param dmMap
	 *            the dm map
	 * @param channel
	 *            the channel
	 * @return the exit node
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private FDPNode getExitNode(final BaseNodeDTO baseNodeDTO, final FDPNode parentNode, final FDPCircle fdpCircle,
			final Map<String, FDPNode> dmMap, final String channel) throws ExpressionFailedException {
		final ExitMenuNode chaileNode = new ExitMenuNode(baseNodeDTO.getName(), baseNodeDTO.getCode(),
				baseNodeDTO.getCode(), (baseNodeDTO.getChannelName() == null ? null : ChannelType.valueOf(baseNodeDTO
						.getChannelName())), fdpCircle, (baseNodeDTO.getPriority() == null ? null
						: Long.valueOf(baseNodeDTO.getPriority())), parentNode, null,
				this.getAddInfoMapForFDPNode(baseNodeDTO.getAddInfo().getAddInfoMap()),Visibility.getVisibility(baseNodeDTO.getVisibility()),DynamicMenuUtil.getDMState(baseNodeDTO.getStatus()));
		chaileNode.setChildren(getChildList(baseNodeDTO.getChildList(), chaileNode, fdpCircle, dmMap, channel));
		return chaileNode;
	}

	/**
	 * Gets the constraint node.
	 *
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param parentNode
	 *            the parent node
	 * @param fdpCircle
	 *            the fdp circle
	 * @param dmMap
	 *            the dm map
	 * @param channel
	 *            the channel
	 * @return the constraint node
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private FDPNode getConstraintNode(final BaseNodeDTO baseNodeDTO, final FDPNode parentNode,
			final FDPCircle fdpCircle, final Map<String, FDPNode> dmMap, final String channel)
			throws ExpressionFailedException {
		
		List<String> listOfAliasCode = fdpDynamicMenuAliasCodeDAO.getDynamicMenuAliasCodeForCircleChannelCode(
				fdpCircle, baseNodeDTO.getCode(), channel);

		updateAliasCacheForProductNode(baseNodeDTO, fdpCircle, channel, listOfAliasCode);
		
		final ConstraintNodeDTO constDTO = (ConstraintNodeDTO) baseNodeDTO;
		final Expression fdpExpression = ExpressionUtil.createExpressionForSteps(constDTO.getStepList());
		final ConstraintNode chaileNode = new ConstraintNode(baseNodeDTO.getName(), baseNodeDTO.getCode(),
				baseNodeDTO.getCode(), (baseNodeDTO.getChannelName() == null ? null : ChannelType.valueOf(baseNodeDTO
						.getChannelName())), fdpCircle, (baseNodeDTO.getPriority() == null ? null
						: Long.valueOf(baseNodeDTO.getPriority())), parentNode, null, fdpExpression,
				this.getAddInfoMapForFDPNode(baseNodeDTO.getAddInfo().getAddInfoMap()),Visibility.getVisibility(baseNodeDTO.getVisibility()),DynamicMenuUtil.getDMState(baseNodeDTO.getStatus()));
		chaileNode.setChildren(getChildList(baseNodeDTO.getChildList(), chaileNode, fdpCircle, dmMap, channel));
		updateDMNodeAdditionalInfo(chaileNode, baseNodeDTO, channel);
		return chaileNode;
	}

	/**
	 * Gets the no constraint node.
	 *
	 * @param baseNodeDTO
	 *            the base node dto
	 * @param parentNode
	 *            the parent node
	 * @param fdpCircle
	 *            the fdp circle
	 * @param dmMap
	 *            the dm map
	 * @param channel
	 *            the channel
	 * @return the no constraint node
	 * @throws ExpressionFailedException
	 *             the expression failed exception
	 */
	private FDPNode getNoConstraintNode(final BaseNodeDTO baseNodeDTO, final FDPNode parentNode,
			final FDPCircle fdpCircle, final Map<String, FDPNode> dmMap, final String channel)
			throws ExpressionFailedException {
		
		List<String> listOfAliasCode = fdpDynamicMenuAliasCodeDAO.getDynamicMenuAliasCodeForCircleChannelCode(
				fdpCircle, baseNodeDTO.getCode(), channel);

		updateAliasCacheForProductNode(baseNodeDTO, fdpCircle, channel, listOfAliasCode);
		
		final NoConstraintMenuNode chaileNode = new NoConstraintMenuNode(baseNodeDTO.getName(), baseNodeDTO.getCode(),
				baseNodeDTO.getCode(), (baseNodeDTO.getChannelName() == null ? null : ChannelType.valueOf(baseNodeDTO
						.getChannelName())), fdpCircle, (baseNodeDTO.getPriority() == null ? null
						: Long.valueOf(baseNodeDTO.getPriority())), parentNode, null,
				this.getAddInfoMapForFDPNode(baseNodeDTO.getAddInfo().getAddInfoMap()),Visibility.getVisibility(baseNodeDTO.getVisibility()),DynamicMenuUtil.getDMState(baseNodeDTO.getStatus()));
		chaileNode.setChildren(getChildList(baseNodeDTO.getChildList(), chaileNode, fdpCircle, dmMap, channel));
		updateDMNodeAdditionalInfo(chaileNode, baseNodeDTO, channel);
		return chaileNode;
	}

	/**
	 * Gets the adds the info map for fdp node.
	 *
	 * @param addInfoMap
	 *            the add info map
	 * @return the adds the info map for fdp node
	 */
	private Map<String, Object> getAddInfoMapForFDPNode(final Map<DynamicMenuAdditionalInfoKey, String> addInfoMap) {
		final Map<String, Object> additionalInfo = new HashMap<String, Object>();

		for (DynamicMenuAdditionalInfoKey keyEnum : addInfoMap.keySet()) {
			switch (keyEnum) {
			case EXIT_STATUS:
				break;
			case LOGICAL_NAME:
				additionalInfo.put(FDPNodeAddInfoKeys.EXTERNAL_SYSTEM_LOGICAL_NAME.name(), addInfoMap.get(keyEnum));
				break;
			case MARKETING_MESSAGE_ENABLED:
				break;
			case MARKETING_MESSAGE_TEXT:
				break;
			case MARKETING_MESSAGE_TYPE:
				break;
			case MENU_HEADER_STATUS:
				break;
			case MENU_HEADER_TEXT:
				break;
			case RETURN_TO_MAIN_MENU_STATUS:
				break;
			case RETURN_TO_PREVIOUS_MENU_STATUS:
				break;
			case TARIFF_ENQUIRY_NETWORK:
				additionalInfo.put(FDPNodeAddInfoKeys.TARIFF_ENQUIRY_NETWORK.name(), addInfoMap.get(keyEnum));
				break;
			case TARIFF_ENQUIRY_SPAN:
				additionalInfo.put(FDPNodeAddInfoKeys.TARIFF_ENQUIRY_SPAN.name(), addInfoMap.get(keyEnum));
				break;
			case TARIFF_ENQUIRY_TYPE:
				additionalInfo.put(FDPNodeAddInfoKeys.TARIFF_ENQUIRY_TYPE.name(), addInfoMap.get(keyEnum));
				break;
			case TARIFF_ENQUIRY_ADDITIONAL_INFO_DATA:
				additionalInfo.put(FDPNodeAddInfoKeys.TARIFF_ENQUIRY_ADDITIONAL_INFO_DATA.name(),
						populateEnumFromKey(addInfoMap.get(keyEnum)));
				break;
			case NO_OF_DAYS:
				additionalInfo.put(FDPNodeAddInfoKeys.NO_OF_DAYS.name(), addInfoMap.get(keyEnum));
				break;
			case LOAN_PROCESSING_FAILURE_NOTIFICATION_ID:
			case LOW_BALANCE_ELIGIBILITY_NOTIFICATION_ID:
			case CASH_ON_DELIVERY_FAILURE_NOTIFICATION_ID:
			case CASH_ON_DELIVERY_SUCCESS_NOTIFICATION_ID:
				additionalInfo.put(keyEnum.name(), addInfoMap.get(keyEnum));
				break;
				/**
				 *  to display name in french in dynamic menu
				 */
			case FRENCH_DISPLAY_NAME:
				additionalInfo.put(keyEnum.name(), addInfoMap.get(keyEnum));
				break;

			default:
				break;
			}
		}
		return additionalInfo;
	}

	/**
	 * This method will populate the tariff additional info enum from its ids
	 *
	 * @param string
	 *            comma seperated ids
	 * @return
	 */
	private List<TariffEnquiryAttributeKeysEnum> populateEnumFromKey(final String addInfoIds) {
		List<TariffEnquiryAttributeKeysEnum> addInfoData = new ArrayList<TariffEnquiryAttributeKeysEnum>();
		if (null != addInfoIds && addInfoIds.length() > 0) {
			String tokens[] = addInfoIds.split(",");
			for (String val : tokens) {
				addInfoData.add(TariffEnquiryAttributeKeysEnum.getEnumFromAttributeKey(val));
			}
		}
		return addInfoData;
	}

	@Override
	public ModuleType getModuleType() {
		return ModuleType.DM;
	}
}
