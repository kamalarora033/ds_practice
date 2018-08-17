package com.ericsson.fdp.command.rollback.mobilemoney;

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

public class MMDebitRollback extends NonTransactionCommand{


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
	public MMDebitRollback(final String commandDisplayName) throws IOException {
		super(commandDisplayName);
		File file=new File(PropertyUtils.getProperty("MM_ROLLBACK_LOGFILE_PATH"));
		if(!file.exists())file.createNewFile();
		filewriter=new FileWriter(new File(PropertyUtils.getProperty("MM_ROLLBACK_LOGFILE_PATH")));
	}

	
	@Override
	public Status execute(FDPRequest fdpRequest, Object... otherParams)
			throws ExecutionFailedException {
	Product product=(Product) fdpRequest.getValueFromRequest(RequestMetaValuesKey.PRODUCT);
	
	try
	{
		if(filewriter==null)
		{
			filewriter=new FileWriter(new File(PropertyUtils.getProperty("MM_ROLLBACK_LOGFILE_PATH")));	
		}
		filewriter.write(
				"MSISDN:"+fdpRequest.getIncomingSubscriberNumber()+
				",Beneficiary:"+((fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_NUMBER)!=null)?
						(fdpRequest.getAuxiliaryRequestParameter(AuxRequestParam.BENEFICIARY_NUMBER)):fdpRequest.getSubscriberNumber())+
				",Product:"+product.getProductName()+System.getProperty("line.separator"));
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
