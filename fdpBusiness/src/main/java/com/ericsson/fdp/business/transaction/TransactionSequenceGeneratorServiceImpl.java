package com.ericsson.fdp.business.transaction;

import javax.ejb.Stateless;
import javax.inject.Inject;

import com.ericsson.fdp.dao.fdpbusiness.TransactionSequenceDAO;

@Stateless(mappedName = "TransactionSequenceGeneratorServiceImpl")
public class TransactionSequenceGeneratorServiceImpl implements TransactionSequenceGeneratorService {

	/** The transaction sequence dao. */
	@Inject
	private TransactionSequenceDAO transactionSequenceDAO;
	
	@Override
	public Long generateTransactionId() {
		return transactionSequenceDAO.getNextTransactionNumber();
	}

}
