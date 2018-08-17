package com.ericsson.fdp.business.sharedaccount.impl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.request.impl.FulfillmentRequestImpl;
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
import com.ericsson.fdp.dao.fdpadmin.FDPSharedAccountReqDAO;

/**
 * The Class AddConsumerServiceImpl.
 * 
 * @author Ericsson
 */
@Stateless
public class AddConsumerServiceImpl extends AbstractSharedAccountService {

	/** The fdp shared account req dao. */
	@Inject
	private FDPSharedAccountReqDAO fdpSharedAccountReqDao;

	/** The fdp product additional info dao. */
	@Inject
	private FDPProductAdditionalInfoDAO fdpProductAdditionalInfoDAO;

	/** The application cache. */
	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_JNDI_NAME)
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	@Override
	public Map<SharedAccountResponseType, Object> executeSharedAccountService(final FDPRequest fdpRequest,
			final Object... params) throws ExecutionFailedException {

		final Map<SharedAccountResponseType, Object> statusDataMap = new HashMap<SharedAccountResponseType, Object>();
		final Long consumerMsisdn = Long.parseLong((((String) fdpRequest
				.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_MSISDN))));
		final String consumerName = fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_NAME) == null ? null
				: fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.CONSUMER_NAME).toString();
		final SharedAccountDTO sharedAccountDto = getSharedAccountDTO(fdpRequest, (Long) params[0], consumerMsisdn,
				consumerName);
		final Long accReqNo = fdpSharedAccountReqDao.save(sharedAccountDto);
		statusDataMap.put(SharedAccountResponseType.STATUS, SharedAccountConstants.SUCCESS);
		statusDataMap.put(SharedAccountResponseType.ACC_REQ_NO, accReqNo);
		statusDataMap.put(SharedAccountResponseType.PROVIDER_MSISDN, fdpRequest.getSubscriberNumber());
		statusDataMap.put(SharedAccountResponseType.CONSUMER_MSISDN, consumerMsisdn);
		if(fdpRequest instanceof FulfillmentRequestImpl) {
			FulfillmentRequestImpl fulfillmentRequestImpl = (FulfillmentRequestImpl) fdpRequest;
			fulfillmentRequestImpl.putAuxiliaryRequestParameter(AuxRequestParam.SHARED_DBID, accReqNo);
		}
		return statusDataMap;
	}

	/**
	 * Gets the shared account dto.
	 * 
	 * @param fdpRequest
	 *            the fdp request
	 * @param productId
	 *            the product id
	 * @param consumerMsisdn
	 *            the consumer msisdn
	 * @param consumerName
	 *            the consumer name
	 * @return the shared account dto
	 */
	private SharedAccountDTO getSharedAccountDTO(final FDPRequest fdpRequest, final Long productId,
			final Long consumerMsisdn, final String consumerName) {

		final SharedAccountRequestDTO addConsumerDto = new SharedAccountRequestDTO();
		addConsumerDto.setCircle(fdpRequest.getCircle());
		addConsumerDto.setConsumerRequestType(ConsumerRequestType.ADD_CONSUMER);
		addConsumerDto.setConsumerStatus(ConsumerStatusEnum.PENDING);
		addConsumerDto.setEntityId(productId);
		addConsumerDto.setSenderMsisdn(fdpRequest.getSubscriberNumber());
		addConsumerDto.setSenderCountryCode(fdpRequest.getSubscriberNumberNAI());
		addConsumerDto.setRequestId(fdpRequest.getRequestId());
		final Long reciverMsidn = consumerMsisdn;
		addConsumerDto.setReciverMsisdn(reciverMsidn);
		addConsumerDto.setConsumerAddInfo(consumerName == null ? consumerMsisdn.toString() : consumerName);
		addConsumerDto.setReciverCountryCode(getCountryCode(reciverMsidn));
		addConsumerDto.setChannelType(fdpRequest.getChannel().getName());
		final Calendar currentDate = Calendar.getInstance();
		final FDPCircle circle = fdpRequest.getCircle();
		circle.getConfigurationKeyValueMap().get(SharedAccountConstants.EXPIRE_HOURS);
		currentDate.add(Calendar.HOUR,
				Integer.parseInt(circle.getConfigurationKeyValueMap().get(SharedAccountConstants.EXPIRE_HOURS)));
		addConsumerDto.setExpiredOn(currentDate);
		final Long maxAccReqNo = fdpSharedAccountReqDao.getMaxAccReqNo(fdpRequest.getCircle(),
				ConsumerRequestType.ADD_CONSUMER);
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
