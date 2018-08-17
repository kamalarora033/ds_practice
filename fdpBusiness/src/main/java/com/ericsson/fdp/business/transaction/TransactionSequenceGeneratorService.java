package com.ericsson.fdp.business.transaction;

import javax.ejb.Remote;

//@Remote
public interface TransactionSequenceGeneratorService {
	
	
	public Long generateTransactionId();

}
