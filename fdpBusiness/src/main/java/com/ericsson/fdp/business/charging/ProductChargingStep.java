package com.ericsson.fdp.business.charging;

import java.io.Serializable;
import com.ericsson.fdp.business.FDPRollbackable;
import com.ericsson.fdp.business.step.FDPStep;

/**
 * This interface defines the product charging interface. The implementation
 * class defines the charging for a product.
 * 
 * @author Ericsson
 * 
 */
public interface ProductChargingStep extends FDPStep, FDPRollbackable, Serializable {

}
