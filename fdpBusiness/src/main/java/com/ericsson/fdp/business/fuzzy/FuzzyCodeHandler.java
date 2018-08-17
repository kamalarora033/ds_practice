package com.ericsson.fdp.business.fuzzy;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.cache.MetaDataCache;
import com.ericsson.fdp.business.vo.FDPFuzzyCodeVo;
import com.ericsson.fdp.common.constants.FDPConstant;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.dsm.framework.service.FDPCache;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPAppBag;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;
import com.ericsson.fdp.dao.dto.FDPFuzzyCodeDTO;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;

public class FuzzyCodeHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PerformRefillEr100.class);

	public boolean handleFuzzyCode(FDPRequest fdpRequest,FDPCommand fdpcommand, FDPResultCodesDTO fdpresultCodeDto,Integer fdpFuzzyCode) {
		boolean status=true;
		try {
		FuzzyCheck fuzzycheckclass = getFuzzyCommandClass(fdpRequest.getCircle(), fdpcommand, fdpresultCodeDto,fdpFuzzyCode);
		if(null != fuzzycheckclass) {
			status=fuzzycheckclass.execute(fdpRequest, fdpcommand, fdpresultCodeDto);
		} else {
			LOGGER.error("ERROR in handling error code "+ fdpresultCodeDto.getResultCodeValue());
		}
		} catch (EvaluationFailedException e) {
			LOGGER.error("ERROR in handling error code "+ fdpresultCodeDto.getResultCodeValue());
		}
		return status;
	}

	/**
	 * Return the fuzzyCodeClass name
	 * 
	 * @param fdpCircle
	 * @param commandDisplayName
	 * @param fdpFuzzyCode
	 * @return
	 */

	private String getFuzzyCodeCheckClass(FDPCircle fdpCircle,String commandDisplayName, Integer fdpFuzzyCode) {
		FDPFuzzyCodeVo fdpFuzzyCodeVo=null;
		String fdpFuzzyCodeCheckClass=null;
		FDPCache<FDPMetaBag, FDPCacheable> configCache = null;
		try {
		configCache = ApplicationConfigUtil.getMetaDataCache();
		FDPMetaBag metaBag = new FDPMetaBag(fdpCircle, ModuleType.FUZZY_CODES,commandDisplayName + FDPConstant.UNDERSCORE + fdpFuzzyCode);
		FDPCacheable fdpCacheable = configCache.getValue(metaBag);
		fdpFuzzyCodeVo = (FDPFuzzyCodeVo) fdpCacheable;
		if (null != fdpFuzzyCodeVo)
		fdpFuzzyCodeCheckClass=fdpFuzzyCodeVo.getFdp_fuzzyCodeCheck_Class();
		else LOGGER.error("ERROR in handling error code "+ fdpFuzzyCodeVo);
		} catch (ExecutionFailedException | NullPointerException e) {
			// TODO Auto-generated catch block
			LOGGER.error("ERROR in handling error code "+ e);
		}
		return fdpFuzzyCodeCheckClass;

	}

	private FuzzyCheck getFuzzyCommandClass(FDPCircle fdpCircle,	FDPCommand fdpcommand, FDPResultCodesDTO fdpresultCodeDto,Integer fdpFuzzyCode) {
		FuzzyCheck fuzzy=null;
		try {
			fuzzy= (FuzzyCheck) Class.forName(new FuzzyCodeHandler().getFuzzyCodeCheckClass(fdpCircle,fdpcommand.getCommandDisplayName(),fdpFuzzyCode)).newInstance();
		} catch (ClassNotFoundException |InstantiationException |IllegalAccessException e) {
			// TODO Auto-generated catch block
			LOGGER.error("ERROR in handling error code "+ e);
		
		}
		return fuzzy;

	}

}
