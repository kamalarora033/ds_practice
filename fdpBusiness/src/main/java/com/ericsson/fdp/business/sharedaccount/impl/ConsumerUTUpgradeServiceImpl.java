package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.sharedaccount.AbstractSharedAccountService;
import com.ericsson.fdp.business.sharedaccount.constans.SharedAccountConstants;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountDTO;
import com.ericsson.fdp.dao.dto.sharedaccount.SharedAccountRequestDTO;
import com.ericsson.fdp.dao.enums.ConsumerRequestType;
import com.ericsson.fdp.dao.enums.ConsumerStatusEnum;
import com.ericsson.fdp.dao.enums.SharedAccountResponseType;
import com.ericsson.fdp.dao.fdpadmin.FDPProductAdditionalInfoDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountGroupDAO;
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

/**
 * The Class ConsumerUTUpgradeServiceImpl.
 * 
 * @author Ericsson
 */
@Stateless
public class ConsumerUTUpgradeServiceImpl extends AbstractSharedAccountService {

	/** The fdp shared account req dao. */
	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDao;

	/** The fdp shared account group dao. */
	@Inject
	private FDPSharedAccountGroupDAO fdpSharedAccountGroupDAO;

	/** The fdp product additional info dao. */
	@Inject
	private FDPProductAdditionalInfoDAO fdpProductAdditionalInfoDAO;

	/** The application cache. */
	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_JNDI_NAME)
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@SuppressWarnings("unchecked")
	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		// List<AddConsumerDTO> addConsumerDtoList =
		// fdpSharedAccountReqDao.getPendingConsumerList((Long)params[0],
		// fdpRequest.getSubscriberNumber(),
		// fdpRequest.getCircle().getCircleId(),ConsumerRequestType.ADD_CONSUMER);
		Long accReqNo;
		// if(addConsumerDtoList.isEmpty()){
		// SharedAccountDTO sharedAccountDto =
		// getSharedAccountDTO(fdpRequest,(Long)params[0]);
		//
		// }else{
		// accReqNo = addConsumerDtoList.get(0).getAccReqNumber();
		// }
		final SharedAccountDTO sharedAccountDto = getSharedAccountDTO(fdpRequest, (Long) params[0]);
		accReqNo = fdpSharedAccountReqDao.save(sharedAccountDto);
		statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		statusDataMap.put(SharedAccountResponseType.ACC_REQ_NO, accReqNo);
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());
		statusDataMap.put(
				SharedAccountResponseType.CONSUMER_MSISDN,
				new Long(((List<String>) fdpRequest
						.getAuxiliaryRequestParameter(AuxRequestParam.CURRENT_SHARED_ACCOUNT_VALUE)).get(0)));
		return statusDataMap;
	}

	/**
	 * Gets the shared account dto.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param productId
	 *            the product id
	 * @return the shared account dto
	 */
	private SharedAccountDTO getSharedAccountDTO(final FDPRequest fdpRequest, final Long productId) {
		final SharedAccountRequestDTO addConsumerDto = new SharedAccountRequestDTO();
		addConsumerDto.setCircle(fdpRequest.getCircle());
		addConsumerDto.setConsumerRequestType(ConsumerRequestType.CONSUMER_UT_UPGRADE);
		addConsumerDto.setConsumerStatus(ConsumerStatusEnum.PENDING);
		addConsumerDto.setEntityId(productId);
		final Long senderId = Long.parseLong((fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.PROVIDER_MSISDN)
				.toString()));
		addConsumerDto.setSenderMsisdn(fdpRequest.getSubscriberNumber());
		addConsumerDto.setSenderCountryCode(fdpRequest.getSubscriberNumberNAI());
		addConsumerDto.setRequestId(fdpRequest.getRequestId());
		addConsumerDto.setReciverMsisdn(senderId);
		addConsumerDto.setReciverCountryCode(getCountryCode(senderId));
		addConsumerDto.setConsumerAddInfo(fdpRequest.getAuxiliaryRequestParameter(
				AuxRequestParam.CONSUMER_LIMIT_UPGRADE_VALUE).toString());
		addConsumerDto.setChannelType(fdpRequest.getChannel().getName());
		final Calendar currentDate = Calendar.getInstance();
		final FDPCircle circle = fdpRequest.getCircle();
		circle.getConfigurationKeyValueMap().get(SharedAccountConstants.EXPIRE_HOURS);
		currentDate.add(Calendar.HOUR,
				Integer.parseInt(circle.getConfigurationKeyValueMap().get(SharedAccountConstants.EXPIRE_HOURS)));
		addConsumerDto.setExpiredOn(currentDate);
		final Long maxAccReqNo = fdpSharedAccountReqDao.getMaxAccReqNo(fdpRequest.getCircle(),
				ConsumerRequestType.CONSUMER_UT_UPGRADE);
		if ((maxAccReqNo + 1l) <= 100000) {
			addConsumerDto.setAccReqNumber(100000l);
		} else if ((maxAccReqNo + 1l) <= 999999) {
			addConsumerDto.setAccReqNumber(maxAccReqNo + 1l);
		} else {
			final List<Long> accReqNoList = fdpSharedAccountReqDao.getAccReqListNotPending(fdpRequest.getCircle());
			addConsumerDto.setAccReqNumber(accReqNoList.get(0));
		}
		return addConsumerDto;
	}

}
