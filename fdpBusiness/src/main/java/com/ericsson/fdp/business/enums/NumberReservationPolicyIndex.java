package com.ericsson.fdp.business.enums;
/**
 * This Enum is for the Number Reservation Policy Index sequence 
*/
public enum NumberReservationPolicyIndex {

	/** Number Reservation Policy Rule 0 **/
	NUMBER_RESERVATION_POLICY_RULE(0),
	/** Number Reservation Policy RuleIndex 1 **/
	NUMBER_RESERVATION_ALTERNATE_NUMBER_POLICY_RULE(1),
	/** Number Reservation Policy Rule 2 **/
	NUMBER_RESERVATION_ALTERNATE_NUMBER_CONFIRM_POLICY_RULE(2),
	/** Number Reservation Policy Rule 3 **/
	NUMBER_RESERVATION_FOR_SOMEONE_POLICY_RULE(3),
	/** Number Reservation Policy Rule 4 **/
	NUMBER_RESERVATION_FOR_SOMEONE_CONFIRM_POLICY_RULE(4);
	
	/** The index**/
	private Integer index;
	
	/**
	 * Instantiates a new number reservation policy index.
	 *
	 * @param name the index
	 */
	private NumberReservationPolicyIndex(Integer index){
		this.index = index;
	}
	
	/**
	 * Gets the index.
	 *
	 * @return the index
	 */
	public Integer getIndex(){
		return index;
	}

}
