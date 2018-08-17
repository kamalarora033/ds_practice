package com.ericsson.fdp.business.util;

import java.util.List;

import javax.naming.NamingException;

import com.ericsson.fdp.business.command.rollback.bean.RollBackCacheBean;
import com.ericsson.fdp.business.serviceprovisioning.ServiceProvisioning;
import com.ericsson.fdp.common.enums.ModuleType;
import com.ericsson.fdp.common.vo.FDPCircle;
import com.ericsson.fdp.core.command.FDPCommand;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.serviceprovisioning.bean.FDPMetaBag;
import com.ericsson.fdp.core.utils.ApplicationConfigUtil;

/**
 * This is a utility class to be used to get the beans statically from the
 * application.
 * 
 * @author Ericsson
 * 
 */
public class ApplicationBusinessConfigUtil {

	/**
	 * This method is used to get the rollback commands for the provided
	 * command.
	 * 
	 * @param commandDisplayName
	 *            The command for which rollback commands are to be fetched.
	 * @return The list of rollback commands.
	 * @throws ExecutionFailedException
	 *             Exception, if the rollback commands could not be fetched.
	 */
	public static List<FDPCommand> getRollbackCommandsForCommand(final String commandDisplayName)
			throws ExecutionFailedException {
		final FDPCircle fdpCircle = new FDPCircle();
		fdpCircle.setCircleCode("All");
		fdpCircle.setCircleName("All");
		fdpCircle.setCircleId(-1l);
		final FDPMetaBag fdpMetaBag = new FDPMetaBag(fdpCircle, ModuleType.ROLLBACK_COMMAND, commandDisplayName);
		return ((RollBackCacheBean) ApplicationConfigUtil.getMetaDataCache().getValue(fdpMetaBag))
				.getRollBackCommandList();
	}

	/**
	 * Gets the service provisioning.
	 * 
	 * @return the service provisioning
	 * @throws ExecutionFailedException
	 *             the execution failed exception
	 */
	public static ServiceProvisioning getServiceProvisioning() throws ExecutionFailedException {
		Object lookupObject = null;
		try {
			lookupObject = ApplicationConfigUtil
					.getBean("java:global/fdpBusiness-ear/fdpBusiness-1.0/ServiceProvisioningImpl");
		} catch (final NamingException e) {
			throw new ExecutionFailedException("Could not get ServiceProvisioning.", e);
		}
		ServiceProvisioning requestObject = null;
		if (lookupObject instanceof ServiceProvisioning) {
			requestObject = (ServiceProvisioning) lookupObject;
		}
		return requestObject;
	}
}
