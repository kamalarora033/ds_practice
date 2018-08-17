package com.ericsson.fdp.business.enums;

public enum TariffEnquiryOperationType {

	DIV("DIV"),
	
	NOP("NOP"),
	
	MOD("MOD"),
	
	MUL("MUL");
	
	private String operation;
	
	private TariffEnquiryOperationType(String operation) {
		this.operation = operation;
	}
	
	/**
	 * @return the operation
	 */
	public String getOperation() {
		return operation;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(String operation) {
		this.operation = operation;
	}



	public static TariffEnquiryOperationType getTariffEnquiryOperationType(String operationType) {
		TariffEnquiryOperationType oType= null;
		for(TariffEnquiryOperationType tariffEnquiryOperationType : TariffEnquiryOperationType.values()) {
			if(tariffEnquiryOperationType.getOperation().equalsIgnoreCase(operationType)) {
				oType = tariffEnquiryOperationType;
				break;
			}
		}
		return oType;
	}
}
