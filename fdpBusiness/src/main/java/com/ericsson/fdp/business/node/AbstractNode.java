package com.ericsson.fdp.business.node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.display.impl.DisplayObjectImpl;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.DynamicMenuPaginationKey;
import com.ericsson.fdp.business.enums.PolicyRuleEnum;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.State;
import com.ericsson.fdp.business.node.impl.ProductNode;
import com.ericsson.fdp.business.policy.impl.AbstractPolicy;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.util.DynamicMenuUtil;
import com.ericsson.fdp.business.util.LoggerUtil;
import com.ericsson.fdp.business.util.ProductUtil;
import com.ericsson.fdp.business.util.RequestUtil;
import com.ericsson.fdp.business.util.ResponseUtil;
import com.ericsson.fdp.common.enums.ChannelType;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.enums.Visibility;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.logging.FDPLogger;
import com.ericsson.fdp.core.node.FDPNode;
import com.ericsson.fdp.core.policy.Policy;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.request.FDPResponse;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.enums.FDPServiceProvSubType;

import ch.qos.logback.classic.Logger;
import com.ericsson.fdp.dao.enums.LanguageType;
/**
 * This class stores the common variables that are shared across the nodes.
 */
public abstract class AbstractNode implements FDPNode {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -2594680330997694024L;

	/**
	 * The diplay name of the node.
	 */
	private String displayName;
	
	/**
	 * The diplay name of the node.
	 */
	private Visibility visibility;

	/**
	 * The short code of the node.
	 */
	private String shortCode;

	/**
	 * The fully qualified path of the node.
	 */
	private String fullyQualifiedPath;

	/**
	 * The channel to which the node belongs.
	 */
	private ChannelType channel;

	/**
	 * The circle to which the node belongs.
	 */
	private FDPCircle circle;

	/**
	 * The priority of the node.
	 */
	private Long priority;

	/**
	 * The parent of the node.
	 */
	private FDPNode parent;

	/**
	 * The children of the node.
	 */
	private List<FDPNode> children;

	/**
	 * The policy to be applied if any.
	 */
	private String policyName;

	/** The policy. */
	private Policy policy;

	/**
	 * The additional info on the node.
	 */
	private Map<String, Object> additionalInfo;


	/**
	 * The pagination options that have been selected by the user.
	 */
	private List<DynamicMenuPaginationKey> paginationOptions;
	
	/**
	 * The DM Node Status.
	 */
	private State dmStatus;

	/**
	 * The otherLangMap.
	 */
	private Map<LanguageType,String> otherLangMap;
	/**
	 * Sets the abstract node.
	 *
	 * @param displayNameToSet the display name to set
	 * @param shortCodeToSet the short code to set
	 * @param fullyQualifiedPathToSet the fully qualified path to set
	 * @param channelToSet the channel to set
	 * @param circleToSet the circle to set
	 * @param priorityToSet the priority to set
	 * @param parentToSet the parent to set
	 * @param childrenToSet the children to set
	 * @param additionalInfo the additional info
	 * @param visibility the visibility
	 */
	protected void setAbstractNode(final String displayNameToSet, final String shortCodeToSet,
			final String fullyQualifiedPathToSet, final ChannelType channelToSet, final FDPCircle circleToSet,
			final Long priorityToSet, final FDPNode parentToSet, final List<FDPNode> childrenToSet,
			final Map<String, Object> additionalInfo,final Visibility visibility, final State dmStatus) {
		this.setDisplayName(displayNameToSet);
		this.setShortCode(shortCodeToSet);
		this.setFullyQualifiedPath(fullyQualifiedPathToSet);
		this.setChannel(channelToSet);
		this.setCircle(circleToSet);
		this.setPriority(priorityToSet);
		this.setParent(parentToSet);
		this.setChildren(childrenToSet);
		this.setAdditionalInfo(additionalInfo);
		this.setVisibility(visibility);
		this.setDmStatus(dmStatus);
	}

	/**
	 * Execute policy.
	 *
	 * @param fdpRequest
	 *            the fdp request
	 * @return the fDP response
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected FDPResponse executePolicy(final FDPRequest fdpRequest) throws ExecutionFailedException {
		FDPResponse fdpResponse = null;
		if (getPolicy(fdpRequest) != null) {
			fdpResponse = policy.executePolicy(fdpRequest, this);
		}
		return fdpResponse;
	}

	/**
	 * Gets the policy.
	 *
	 * @return the policy
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	@Override
	public Policy getPolicy(final FDPRequest fdpRequest) throws ExecutionFailedException {
		if (policy == null && policyName != null) {
			policy = (Policy) ApplicationConfigUtil.getMetaDataCache().getValue(
					new FDPMetaBag(new FDPCircle(-1L, "ALL", "ALL"), ModuleType.POLICY, policyName));
			FDPCacheable fdpCacheable = fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
			if (fdpCacheable instanceof Product && this instanceof ProductNode && (FDPServiceProvSubType.PRODUCT_BUY.equals(((ProductNode)this).getServiceProvSubType()) || FDPServiceProvSubType.PRODUCT_BUY_RECURRING.equals(((ProductNode)this).getServiceProvSubType()))) {
				Product product = (Product) fdpCacheable;			
				policy = updateCustomisedPolicyRuleInCurrentPolicy(policy,ProductUtil.getPolicyIdentifierList(product));
			}		
		}
		return policy;
	}

	@Override
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * Sets the display name.
	 *
	 * @param displayNameToSet
	 *            the displayName to set
	 */
	public void setDisplayName(final String displayNameToSet) {
		this.displayName = displayNameToSet;
	}

	/**
	 * Gets the short code.
	 *
	 * @return the shortCode
	 */
	public String getShortCode() {
		return shortCode;
	}

	/**
	 * Sets the short code.
	 *
	 * @param shortCodeToSet
	 *            the shortCode to set
	 */
	public void setShortCode(final String shortCodeToSet) {
		this.shortCode = shortCodeToSet;
	}

	@Override
	public String getFullyQualifiedPath() {
		return fullyQualifiedPath;
	}

	/**
	 * Sets the fully qualified path.
	 *
	 * @param fullyQualifiedPathToSet
	 *            the fullyQualifiedPath to set
	 */
	public void setFullyQualifiedPath(final String fullyQualifiedPathToSet) {
		this.fullyQualifiedPath = fullyQualifiedPathToSet;
	}

	/**
	 * Gets the channel.
	 *
	 * @return the channel
	 */
	public ChannelType getChannel() {
		return channel;
	}

	/**
	 * Sets the channel.
	 *
	 * @param channelToSet
	 *            the channel to set
	 */
	public void setChannel(final ChannelType channelToSet) {
		this.channel = channelToSet;
	}

	/**
	 * Gets the circle.
	 *
	 * @return the circle
	 */
	public FDPCircle getCircle() {
		return circle;
	}

	/**
	 * Sets the circle.
	 *
	 * @param circleToSet
	 *            the circle to set
	 */
	public void setCircle(final FDPCircle circleToSet) {
		this.circle = circleToSet;
	}

	/**
	 * Gets the parent.
	 *
	 * @return the parent
	 */
	@Override
	public FDPNode getParent() {
		return parent;
	}

	/**
	 * Sets the parent.
	 *
	 * @param parentToSet
	 *            the parent to set
	 */
	public void setParent(final FDPNode parentToSet) {
		this.parent = parentToSet;
	}

	@Override
	public Long getPriority() {
		return priority;
	}

	/**
	 * Sets the priority.
	 *
	 * @param priorityToSet
	 *            the priority to set
	 */
	public void setPriority(final Long priorityToSet) {
		this.priority = priorityToSet;
	}

	@Override
	public List<FDPNode> getChildren() {
		return children;
	}

	/**
	 * Sets the children.
	 *
	 * @param childrenToSet
	 *            the children to set
	 */
	public void setChildren(final List<FDPNode> childrenToSet) {
		this.children = childrenToSet;
	}

	@Override
	public DisplayObject displayNode(final FDPRequest fdpRequest) throws ExecutionFailedException {
		return generateDefaultDisplay(fdpRequest);
	}

	/**
	 * This method is used to generate the default display.
	 *
	 * @param fdpRequest
	 *            the fdp request
	 * @return The default display object.
	 */
	private DisplayObject generateDefaultDisplay(final FDPRequest fdpRequest) {
		final DisplayObjectImpl displayObject = new DisplayObjectImpl(this,
				ResponseUtil.getDisplayNodeMessage(fdpRequest.getChannel()));
		// displayObject.setCurrDisplayText(getDisplayName());
		// displayObject.addNodesToDisplay(this);
		return displayObject;
	}

	@Override
	public boolean evaluateNode(final FDPRequest fdpRequest) throws EvaluationFailedException {
		return true;
	}

	/**
	 * This method is used to display the child nodes for this node.
	 *
	 * @param fdpRequest
	 *            The request object.
	 * @return The display object storing data on child nodes.
	 * @throws EvaluationFailedException
	 *             the evaluation failed exception
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	protected DisplayObject displayChildNodes(final FDPRequest fdpRequest) throws EvaluationFailedException,
			ExecutionFailedException {
		final Logger circleLogger = LoggerUtil.getSummaryLoggerFromRequest(fdpRequest);		
		DisplayObject displayObject = null;
		if (this.getChildren() != null) {
			final DisplayObjectImpl displayObjectImpl = new DisplayObjectImpl(this,
					ResponseUtil.getDisplayNodeMessage(fdpRequest.getChannel()));
			for (final FDPNode fdpNode : this.getChildren()) {
				boolean isNodeValueEvaluated=false;
				try{
					isNodeValueEvaluated=RequestUtil.evaluateNodeValue(fdpRequest, fdpNode);
				}catch(EvaluationFailedException evaluationFailedException){
					FDPLogger.debug(circleLogger, RequestUtil.class, "displayChildNodes()",
							LoggerUtil.getRequestAppender(fdpRequest.getRequestId()) +"Exception during node value evaluation. Node display name is "
									+ ""+fdpNode.getDisplayName()+" "+ evaluationFailedException.getMessage());
				}
				if (isNodeValueEvaluated && fdpNode.isVisible() && DynamicMenuUtil.isWhiteListedForActiveForTestDMNode(fdpRequest, fdpNode)) {
					displayObjectImpl.addNodesToDisplay(fdpNode);
				}
			}
			displayObject = displayObjectImpl;
		} else {
			displayObject = generateDefaultDisplay(fdpRequest);
		}
		return displayObject;
	}

	@Override
	public String toString() {
		return displayName;
	}

	/**
	 * Gets the policy name.
	 *
	 * @return the policy name
	 */
	public String getPolicyName() {
		return policyName;
	}

	/**
	 * Sets the policy name.
	 *
	 * @param policyName
	 *            the new policy name
	 */
	public void setPolicyName(final String policyName) {
		this.policyName = policyName;
	}

	/**
	 * Gets the policy rule values.
	 *
	 * @return the policy rule values
	 */
	public Map<AuxRequestParam, Object> getPolicyRuleValues() {
		Map<AuxRequestParam, Object> returnValues = null;
		if (policy != null) {
			returnValues = policy.getRuleValues();
		}
		return returnValues;
	}

	/**
	 * This method is used to get the additional info on the node.
	 *
	 * @param key
	 *            the key for the node.
	 * @param value
	 *            the value of the node.
	 */
	public void addAdditionalInfo(final String key, final Object value) {
		if (additionalInfo == null) {
			additionalInfo = new HashMap<String, Object>();
		}
		additionalInfo.put(key, value);
	}

	@Override
	public Object getAdditionalInfo(final String key) {
		return additionalInfo == null ? null : additionalInfo.get(key);
	}

	@Override
	public List<String> getAliasCode() {
		return null;
	}

	/**
	 * Gets the pagination options.
	 *
	 * @return the paginationOptions
	 */
	public List<DynamicMenuPaginationKey> getPaginationOptions() {
		return paginationOptions;
	}

	/**
	 * Sets the pagination options.
	 *
	 * @param paginationOptions
	 *            the paginationOptions to set
	 */
	public void setPaginationOptions(final List<DynamicMenuPaginationKey> paginationOptions) {
		this.paginationOptions = paginationOptions;
	}

	/**
	 * Gets the additional info.
	 *
	 * @return the additionalInfo
	 */
	public Map<String, Object> getAdditionalInfo() {
		return additionalInfo;
	}

	/**
	 * Sets the additional info.
	 *
	 * @param additionalInfo the additionalInfo to set
	 */
	public void setAdditionalInfo(final Map<String, Object> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	/**
	 * Gets the visibility.
	 *
	 * @return the visibility
	 */
	public Visibility getVisibility() {
		return (visibility == null) ? Visibility.VISIBLE_FOR_MENU : visibility;
	}

	/**
	 * Sets the visibility.
	 *
	 * @param visibility the visibility to set
	 */
	public void setVisibility(Visibility visibility) {
		this.visibility = visibility;
	}

    @Override
	public boolean isVisible() {
		return (getVisibility().equals(Visibility.VISIBLE_FOR_MENU));
	}

    @Override
	public State getDmStatus() {
		return dmStatus;
	}

    /**
     * Sets the DM Status.
     * 
     * @param dmStatus
     */
    public void setDmStatus(State dmStatus) {
		this.dmStatus = dmStatus;
	}
    
    /** The method for update the current policy list with new custom policy
     * @param policy
     * @return the policy
     */
    private Policy updateCustomisedPolicyRuleInCurrentPolicy(final Policy policy, final List<String> policyIdentiderList) {
		if(null != policyIdentiderList && !policyIdentiderList.isEmpty() && policy instanceof AbstractPolicy) {
			final AbstractPolicy abstractPolicy = (AbstractPolicy) policy;
			abstractPolicy.setPolicyRules(PolicyRuleEnum.updatePolicyRule(abstractPolicy.getPolicyRules(),policyIdentiderList));
		}	
		return policy;
	}
	
		public Map<LanguageType, String> getOtherLangMap() {
		return otherLangMap;
	}

	public void setOtherLangMap(Map<LanguageType, String> otherLangMap) {
		this.otherLangMap = otherLangMap;
	}

    @Override
    public String getOtherLanguageText(final FDPRequest fdpRequest) {
    	//System.out.println("Entered node:"+this.displayName+", otherLangMap:"+otherLangMap+", fdpRequest.getSimLangauge():"+fdpRequest.getSimLangauge());
    	final String otherLang = (null != this.otherLangMap && null != fdpRequest.getSimLangauge()) ? this.otherLangMap.get(fdpRequest.getSimLangauge()) : null;
    	return (null == otherLang) ? this.displayName : otherLang;
    }
}
