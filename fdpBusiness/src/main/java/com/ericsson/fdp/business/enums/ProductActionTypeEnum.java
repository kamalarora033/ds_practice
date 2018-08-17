package com.ericsson.fdp.business.enums;

/**
 * This enum defines the product action type.
 * 
 * @author Ericsson
 * 
 */

public enum ProductActionTypeEnum {
	
	NORMAL_PRODUCT_PREFIX("NACT"),
	
	RECURRING_PRODUCT_PREFIX("RACT"),
	
	SPLIT_PRODUCT_PREFIX("SACT"),
	
	DEPROVISION_PRODUCT_PREFIX("DACT"),
	
	BALANCE_CHECK_PRODUCT_PREFIX("BCHK"),
	
	FAF_ADD_PREFIX("ADD"),
	
	FAF_DELETE_PREFIX("DEL"),
	
	FAF_VIEW_PREFIX("LIST"),
	
	FAF_REGISTER_PREFIX("REG"),
	
	FAF_UNREGISTER_PREFIX("UREG"),
	
	ME2U_PREFIX("ME2U"),
	
	FAF_MODIFY_PREFIX("MOD");
	
	private String actionName;
	
	private ProductActionTypeEnum(String actionName) {
		this.actionName = actionName;
	}
	
	public String getActionName() {
		return actionName;
	}
	
	/**
	 * This method is used to get the product action name from Product Action Type
	 * 
	 * @param productAction
	 *            the productAction for which value is required.
	 * @return the productActionName corresponding to the command.
	 */
	public static String getValueFromProductAction(final String productAction) {
		for (ProductActionTypeEnum productActionType : ProductActionTypeEnum.values()) {
			if (productActionType.getActionName().equalsIgnoreCase(productAction)) {
				return productActionType.getActionName();
			}
		}
		return null;
	}


}
