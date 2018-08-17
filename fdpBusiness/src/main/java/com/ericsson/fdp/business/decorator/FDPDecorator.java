package com.ericsson.fdp.business.decorator;

import com.ericsson.fdp.core.display.DisplayObject;
import com.ericsson.fdp.core.exception.ExecutionFailedException;

/**
 * This interface is a decorator interface, to be used for decorator pattern. It
 * decorates the text to be sent as response to the user.
 * 
 * @author Ericsson
 * 
 */
public interface FDPDecorator {

	/**
	 * This method is used to decorate the text in the display object. This
	 * method modifies the entities used in the implementation. The entity
	 * provided should be modifiable.
	 * 
	 * @return the decorated display object.
	 * @throws ExecutionFailedException
	 *             Exception if display could not be done.
	 */
	DisplayObject display() throws ExecutionFailedException;

}
