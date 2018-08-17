package com.ericsson.fdp.business.command.rollback.rs;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.inject.Inject;

import com.ericsson.fdp.business.command.impl.NonTransactionCommand;
import com.ericsson.fdp.business.enums.AuxRequestParam;
import com.ericsson.fdp.business.enums.RequestMetaValuesKey;
import com.ericsson.fdp.business.product.Product;
import com.ericsson.fdp.common.enums.Status;
import com.ericsson.fdp.core.config.utils.PropertyUtils;
import com.ericsson.fdp.core.exception.ExecutionFailedException;
import com.ericsson.fdp.core.request.FDPRequest;

public class RSDeprovisioningLog extends NonTransactionCommand{
	
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
	public RSDeprovisioningLog(final String commandDisplayName) throws IOException {
		super(commandDisplayName);
		filewriter=new FileWriter(new File(PropertyUtils.getProperty("RS_ROLLBACK_LOGFILE_PATH")));
	}

	
	@Override
	public Status execute(FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
	Product product=(Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
	
	try
	{
		filewriter.write(fdpRequest.getIncomingSubscriberNumber()+","+product.getProductName());
		filewriter.flush();
	}
	catch(Exception exp)
	{
		exp.printStackTrace();
		return Status.FAILURE;
	}
		return Status.SUCCESS;
	}

}
