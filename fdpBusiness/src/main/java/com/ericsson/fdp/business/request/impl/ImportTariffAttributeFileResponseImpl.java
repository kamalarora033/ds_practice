package com.ericsson.fdp.business.request.impl;

import com.ericsson.fdp.business.request.ImportTariffAttributeFileResponse;
import com.ericsson.fdp.common.enums.Status;

/**
 * This class implements the import file response.
 * 
 * @author Ericsson
 * 
 */
public class ImportTariffAttributeFileResponseImpl implements ImportTariffAttributeFileResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7105123136963049011L;

	/**
	 * The status of the import.
	 */
	private Status status=Status.FAILURE;

	/**
	 * The execution status for Json File upload.
	 */
	private Status jsonStatus=Status.FAILURE;
	
	/**
	 * The execution status for Database Operations.
	 */
	private Status databaseStatus=Status.FAILURE;
	
	/**
	 * The execution status for daFileStatus
	 */
	private Status daFileStatus = Status.FAILURE;
	
	/**
	 * The execution status for psoFileStatus
	 */
	private Status psoFileStatus = Status.FAILURE;
	
	/**
	 * The execution status for communityIdFileStatus
	 */
	private Status communityIdFileStatus = Status.FAILURE;
	
	/**
	 * The execution status offerIdFileStatus
	 */
	private Status offerIdFileStatus = Status.FAILURE;
	

	@Override
	public Status getExecutionStatus() {
		return status;
	}

	@Override
	public Status getJsonExecutionStatus() {
		return jsonStatus;
	}

	/**
	 * @return the status
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(Status status) {
		this.status = status;
	}

	/**
	 * @return the jsonStatus
	 */
	public Status getJsonStatus() {
		return jsonStatus;
	}

	/**
	 * @param jsonStatus the jsonStatus to set
	 */
	public void setJsonStatus(Status jsonStatus) {
		this.jsonStatus = jsonStatus;
	}

	/**
	 * @param databaseStatus the databaseStatus to set
	 */
	public void setDatabaseStatus(Status databaseStatus) {
		this.databaseStatus = databaseStatus;
	}

	@Override
	public Status getDatabaseOperationStatus() {
		return this.databaseStatus;
	}
	
	/**
	 * @return the databaseStatus
	 */
	public Status getDatabaseStatus() {
		return databaseStatus;
	}

	@Override
	public String toString() {
		return "ImportTariffAttributeFileResponseImpl [status=" + status + ", jsonStatus=" + jsonStatus
				+ ", databaseStatus=" + databaseStatus + ", daFileStatus=" + daFileStatus + ", psoFileStatus="
				+ psoFileStatus + ", communityIdFileStatus=" + communityIdFileStatus + ", offerIdFileStatus="
				+ offerIdFileStatus + "]";
	}

	/**
	 * @return the daFileStatus
	 */
	@Override
	public Status getDaFileStatus() {
		return daFileStatus;
	}

	/**
	 * @param daFileStatus the daFileStatus to set
	 */
	public void setDaFileStatus(Status daFileStatus) {
		this.daFileStatus = daFileStatus;
	}

	/**
	 * @return the psoFileStatus
	 */
	@Override
	public Status getPsoFileStatus() {
		return psoFileStatus;
	}

	/**
	 * @param psoFileStatus the psoFileStatus to set
	 */
	public void setPsoFileStatus(Status psoFileStatus) {
		this.psoFileStatus = psoFileStatus;
	}

	/**
	 * @return the communityIdFileStatus
	 */
	@Override
	public Status getCommunityIdFileStatus() {
		return communityIdFileStatus;
	}

	/**
	 * @param communityIdFileStatus the communityIdFileStatus to set
	 */
	public void setCommunityIdFileStatus(Status communityIdFileStatus) {
		this.communityIdFileStatus = communityIdFileStatus;
	}

	/**
	 * @return the offerIdFileStatus
	 */
	@Override
	public Status getOfferIdFileStatus() {
		return offerIdFileStatus;
	}

	/**
	 * @param offerIdFileStatus the offerIdFileStatus to set
	 */
	public void setOfferIdFileStatus(Status offerIdFileStatus) {
		this.offerIdFileStatus = offerIdFileStatus;
	}
	
}
