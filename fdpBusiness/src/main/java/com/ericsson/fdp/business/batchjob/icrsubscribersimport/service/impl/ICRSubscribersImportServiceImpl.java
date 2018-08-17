package com.ericsson.fdp.business.batchjob.icrsubscribersimport.service.impl;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.business.batchjob.icrsubscribersimport.service.ICRSubscribersImportService;
import com.ericsson.fdp.business.dto.icrsubscribersimport.ICRSubscribersCSVData;
import com.ericsson.fdp.business.enums.CSVActionType;
import com.ericsson.fdp.common.constants.JNDILookupConstant;
import com.ericsson.fdp.core.cache.AppCacheSubStore;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.CircleCodeFinder;
import com.ericsson.fdp.dao.dto.FDPICRSubscribersDTO;
import com.ericsson.fdp.dao.fdpbusiness.FDPICRSubscribersDAO;

/**
 * The Class ICRSubscribersImportServiceImpl.
 *
 * @author Ericsson
 */
@Stateless
public class ICRSubscribersImportServiceImpl implements ICRSubscribersImportService {

	/** The application config cache. */
	@Resource(lookup = JNDILookupConstant.APPLICATION_CONFIG_CACHE_GLOBAL_JNDI_NAME)
	private FDPCache<FDPAppBag, Object> applicationConfigCache;

	/** The icr subscribers dao. */
	@Inject
	private FDPICRSubscribersDAO icrSubscribersDAO;

	/** The all icr subscribers. */
	private List<FDPICRSubscribersDTO> allICRSubscribers;

	/** The length of msisdn. */
	private Integer lengthOfMsisdn = null;

	/** The valid length. */
	private Integer validLength = null;

	/** The failure message. */
	private String FAILURE_MESSAGE = "";

	@Override
	public ICRSubscribersCSVData processICSSubscriberData(final ICRSubscribersCSVData icrSubscribersCSVData) {

		ICRSubscribersCSVData errorData = new ICRSubscribersCSVData();
		List<List<String>> errorRows = new ArrayList<List<String>>();
		errorData.setDataList(errorRows);

		// Setting data for this run
		allICRSubscribers = icrSubscribersDAO.getAllICRSubscribers();
		lengthOfMsisdn = Integer.parseInt((String) applicationConfigCache.getValue(new FDPAppBag(
				AppCacheSubStore.CONFIGURATION_MAP, "MSISDN_NUMBER_LEN")));
		if (lengthOfMsisdn == null) {
			lengthOfMsisdn = Integer.parseInt(PropertyUtils.getProperty("fdp.msisdn.length"));
		}

		Integer countryCode = Integer.parseInt((String) applicationConfigCache.getValue(new FDPAppBag(
				AppCacheSubStore.CONFIGURATION_MAP, "COUNTRY_CODE")));

		validLength = lengthOfMsisdn + countryCode.toString().length();

		// For each valid row, perform action in DB and for
		// each invalid row, prepare error data
		for (List<String> dataRow : icrSubscribersCSVData.getDataList()) {
			CSVActionType actionType = CSVActionType.getCSVActionType(dataRow.get(2));
			if (!this.validMSISDNandCircleCode(dataRow.get(0), dataRow.get(1), actionType)
					|| !this.performDBAction(dataRow, actionType)) {
				List<String> errorRow = new ArrayList<String>(dataRow);
				errorRow.add(FAILURE_MESSAGE);
				errorRows.add(errorRow);
			}
		}

		return errorData;
	}

	/**
	 * Valid msisd nand circle code.
	 *
	 * @param msisdn the msisdn
	 * @param circleCode the circle code
	 * @param actionType the action type
	 * @return the boolean
	 */
	private Boolean validMSISDNandCircleCode(final String msisdn, final String circleCode,
			final CSVActionType actionType) {

		Long parsedMSISDN = null;
		try {
			parsedMSISDN = Long.parseLong(msisdn);
		} catch (NumberFormatException e) {
			FAILURE_MESSAGE = "Invalid data.";
			return false;
		}

		String circleCodeForMSISDN = CircleCodeFinder.getCircleCode(msisdn, applicationConfigCache);
		if (parsedMSISDN == null || msisdn.length() != validLength || circleCode == null
				|| !circleCode.equals(circleCodeForMSISDN)) {

			FAILURE_MESSAGE = "Invalid data.";
			return false;
		} else if (!validActionForMSISDN(msisdn, actionType)) {

			FAILURE_MESSAGE = "Invalid action.";
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Check msisdn in db.
	 *
	 * @param msisdn
	 *            the msisdn
	 * @param actionType
	 *            the action type
	 * @return true, if successful
	 */
	private Boolean validActionForMSISDN(final String msisdn, final CSVActionType actionType) {

		Boolean found = false;
		for (FDPICRSubscribersDTO icrSubscriber : allICRSubscribers) {
			if (icrSubscriber.getMsisdn().equals(msisdn)) {
				found = true;
				break;
			}
		}

		Boolean validAction = true;

		if (actionType == null) {
			validAction = false;
		} else {
			switch (actionType) {
			case INSERT:
				if (found) {
					validAction = false;
				}
				break;
			case DELETE:
				if (!found) {
					validAction = false;
				}
				break;
			default:
				validAction = false;
				break;
			}
		}

		return validAction;
	}

	/**
	 * Perform db action.
	 *
	 * @param dataRow
	 *            the data row
	 * @param actionType
	 *            the action type
	 * @return the boolean
	 */
	private Boolean performDBAction(final List<String> dataRow, final CSVActionType actionType) {

		Boolean operationSuccessfull = true;
		switch (actionType) {
		case INSERT:
			FDPICRSubscribersDTO icrSubscriber = new FDPICRSubscribersDTO();
			icrSubscriber.setMsisdn(dataRow.get(0));
			icrSubscriber.setCircleCode(dataRow.get(1));

			icrSubscribersDAO.insertICRSubscriber(icrSubscriber);
			break;
		case DELETE:
			if (icrSubscribersDAO.deleteIcrSubscriber(dataRow.get(0)) == 0) {
				operationSuccessfull = false;
				FAILURE_MESSAGE = "Could not delete.";
			}
			break;
		default:
			operationSuccessfull = false;
			FAILURE_MESSAGE = "Invalid action type.";
			break;
		}
		return operationSuccessfull;
	}
}