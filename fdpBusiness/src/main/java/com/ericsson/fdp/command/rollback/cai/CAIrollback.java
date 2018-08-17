package com.ericsson.fdp.command.rollback.cai;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.enums.ServiceStepOptions;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.business.serviceprovisioning.rule.ServiceProvisioningRule;
import com.ericsson.fdp.business.step.FDPStep;
import com.ericsson.fdp.business.step.impl.ServiceStep;
import com.ericsson.fdp.common.enums.SPServices;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

public class CAIrollback extends NonTransactionCommand{
	@Inject 
	private PropertyUtils propertyutil;
	
	
	
	static FileWriter filewriter;
	/**
	 * Instantiates a new rsdeprovision rollback.
	 * 
	 * @param commandDisplayName
	 *            the command display name
	 * @throws IOException 
	 */
	public CAIrollback(final String commandDisplayName) throws IOException {
		super(commandDisplayName);
		File file=new File(PropertyUtils.getProperty("EMA_ROLLBACK_LOGFILE_PATH"));
		if(!file.exists())
		{
			file.createNewFile();
		}
		filewriter=new FileWriter(file);
	}

	
	@Override
	public Status execute(FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
	Product product=(Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
	
	try
	{ String actionType=getActionType(fdpRequest);
		filewriter.append("Rollback");
		filewriter.append(fdpRequest.getIncomingSubscriberNumber()+","+product.getProductName()+",BlackBerryAction:"+actionType);
		filewriter.append("END");
		filewriter.flush();
	}
	catch(Exception exp)
	{
		exp.printStackTrace();
		return Status.FAILURE;
	}
		return Status.SUCCESS;
	}


	private String getActionType(FDPRequest fdpRequest) {
			ServiceProvisioningRule serviceprovisioningrule = (ServiceProvisioningRule) fdpRequest
					.getValueFromRequest(RequestMetaValuesKey.SERVICE_PROVISIONING);

			List<FDPStep> spfdpSteps = serviceprovisioningrule.getFdpSteps();
			for (Iterator iterator = spfdpSteps.iterator(); iterator.hasNext();) {
				FDPStep fdpStep = (FDPStep) iterator.next();
				if (fdpStep instanceof ServiceStep) {
					ServiceStep servicestep = (ServiceStep) fdpStep;
					if (servicestep.getJndiLookupName().equals(
							SPServices.EMA_SERVICE.getValue())) {
						return servicestep.getAdditionalInformation().get(
								ServiceStepOptions.PROVISION_ACTION_EMA);
					}
				}
			}
			return null;
	}

	
}
