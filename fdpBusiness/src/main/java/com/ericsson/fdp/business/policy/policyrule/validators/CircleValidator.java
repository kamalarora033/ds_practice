package com.ericsson.fdp.business.policy.policyrule.validators;

import java.util.Map;

import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.core.config.utils.FDPApplicationFeaturesEnum;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.core.utils.CircleCodeFinder;

/**
 * This class implements the circle validation for the inputs.
 * 
 * @author Ericsson
 * 
 */
public class CircleValidator implements RuleValidator {

	@Override
	public boolean validate(final Map<Object, Object> from, final Map<Object, Object> to)
			throws EvaluationFailedException {
		final Object fromMsisdn = from.get(FDPConstant.MSISDN);
		final Object toMsisdn = to.get(FDPConstant.MSISDN);
		boolean valid = false;
		final String fromMsisdnToUse = getMsisdnString(fromMsisdn);
		final String toMsisdnToUse = getMsisdnString(toMsisdn);
		if (fromMsisdnToUse != null && toMsisdnToUse != null) {
			try {
				final FDPCache<FDPAppBag, Object> applicationConfigCache = ApplicationConfigUtil
						.getApplicationConfigCache();
				if(FDPApplicationFeaturesEnum.isFeatureAllowed(FDPApplicationFeaturesEnum.FEATURE_IS_IMSI_BASED_CIRCLE_CHECK_ALLOW,applicationConfigCache)) {
					valid = true;
				} else {
					
					final String circleCode = CircleCodeFinder.getCircleCode(fromMsisdnToUse, applicationConfigCache);
					if (circleCode != null
							&& circleCode.equals(CircleCodeFinder.getCircleCode(toMsisdnToUse, applicationConfigCache))) {
						valid = true;
					}
				}
			} catch (final ExecutionFailedException e) {
				throw new EvaluationFailedException("Cache could not be found", e);
			}
		}
		return valid;
	}

	private String getMsisdnString(final Object fromMsisdn) {
		String fromMsisdnToUse = fromMsisdn.toString();
		if (fromMsisdn instanceof String) {
			fromMsisdnToUse = (String) fromMsisdn;
		} else if (fromMsisdn instanceof Long) {
			fromMsisdnToUse = ((Long) fromMsisdn).toString();
		}
		return fromMsisdnToUse;
	}

}
