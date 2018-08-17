package com.ericsson.fdp.business.mcarbon.response;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.ericsson.fdp.FDPCacheable;
import com.ericsson.fdp.business.enums.mcarbon.loan.CreditStatus;
import com.ericsson.fdp.business.enums.mcarbon.loan.LoanStatus;

@XmlRootElement(name = "MCARBON")
@XmlAccessorType(XmlAccessType.FIELD)
public class MCarbonLoanResponse implements FDPCacheable {
	
	/**
	 * serial version ID
	 */
	private static final long serialVersionUID = 1838232654028402594L;

	@XmlElement(name = "STATUS")
	private LoanStatus status;
	
	@XmlElement(name = "DESCRIPTION")
	private String description;
	
	@XmlElement(name = "ELIGIBILITY")
	private String eligibility;
	
	@XmlElement(name = "BALANCE")
	private String balance;
	
	@XmlElement(name = "LOAN_TYPE")
	private String loanType;
	
	@XmlElement(name = "CREDIT")
	private CreditStatus creditStatus;
	
	@XmlElement(name = "FEE")
	private Fee fee;

	public LoanStatus getStatus() {
		return status;
	}

	public void setStatus(LoanStatus status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getEligibility() {
		return eligibility;
	}

	public void setEligibility(String eligibility) {
		this.eligibility = eligibility;
	}

	public String getBalance() {
		return balance;
	}

	public void setBalance(String balance) {
		this.balance = balance;
	}

	public String getLoanType() {
		return loanType;
	}

	public void setLoanType(String loanType) {
		this.loanType = loanType;
	}

	public CreditStatus getCreditStatus() {
		return creditStatus;
	}

	public void setCreditStatus(CreditStatus creditStatus) {
		this.creditStatus = creditStatus;
	}
	
	public Fee getFee() {
		return fee;
	}

	public void setFee(Fee fee) {
		this.fee = fee;
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "FEE")
	public static class Fee implements FDPCacheable{
		
		/**
		 * The serial Version Id
		 */
		private static final long serialVersionUID = 21214454577761L;
		
		@XmlElement(name = "PROCESSING_FEE")
		private List<ProcessingFee> processingFeeList = new ArrayList<ProcessingFee>();

		public List<ProcessingFee> getProcessingFeeList() {
			return processingFeeList;
		}

		public void setProcessingFeeList(List<ProcessingFee> processingFeeList) {
			this.processingFeeList = processingFeeList;
		}

		@Override
		public String toString() {
			return "Fee [processingFeeList=" + processingFeeList + "]";
		}
		
		
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "PROCESSING_FEE")
	public static class ProcessingFee implements Comparable<ProcessingFee>, FDPCacheable{
		
		/**
		 * the serial version id.
		 */
		private static final long serialVersionUID = 11212145687653L;

		@XmlElement(name = "TYPE")
		private Float type;
		
		@XmlElement(name = "VALUE")
		private Float value;
		
		public Float getType() {
			return type;
		}
		public void setType(Float type) {
			this.type = type;
		}
		public Float getValue() {
			return value;
		}
		public void setValue(Float value) {
			this.value = value;
		}
		
		@Override
		public int compareTo(ProcessingFee o) {
			return this.getType() < o.getType()? 0 : 1;
		}
		@Override
		public String toString() {
			return "ProcessingFee [type=" + type + ", value=" + value + "]";
		}
		
		
	}

	@Override
	public String toString() {
		return "MCarbonLoanResponse [status=" + status + ", description=" + description + ", eligibility="
				+ eligibility + ", balance=" + balance + ", loanType=" + loanType + ", creditStatus=" + creditStatus
				+ ", fee=" + fee + "]";
	}
	
	
}
