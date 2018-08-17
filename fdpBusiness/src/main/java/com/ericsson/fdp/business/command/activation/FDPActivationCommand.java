package com.ericsson.fdp.business.command.activation;

import com.ericsson.fdp.business.FDPExecutable;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.request.FDPRequest;

/**
 * This interface provides the method for intercepting the activation commands.
 * 
 * @author Ericsson
 * 
 */
public interface FDPActivationCommand extends FDPExecutable<FDPRequest, Status> {

}
