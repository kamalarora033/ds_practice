package com.ericsson.fdp.business.fuzzy;

import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.EvaluationFailedException;
import com.ericsson.fdp.core.request.FDPRequest;
import com.ericsson.fdp.dao.dto.FDPResultCodesDTO;

public interface FuzzyCheck {

	boolean execute(FDPRequest fdpRequest, FDPCommand fdpcommand,FDPResultCodesDTO fdpresultCodeDto) throws EvaluationFailedException;
	
}
