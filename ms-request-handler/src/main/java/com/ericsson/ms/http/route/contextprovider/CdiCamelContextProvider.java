package com.ericsson.ms.http.route.contextprovider;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.springframework.stereotype.Component;

/**
 * @link org.apache.camel.cdi.CdiCamelContext based class
 * 	CdiCamelContextProvider used to generate the CdiCamelContext.
 * 
 *	
 * @author GUR36857
 *
 */
@Component
public class CdiCamelContextProvider {	
	
	private CamelContext context;
	
	/**
	 * Gets the context.
	 * 
	 * @return the context
	 */
	public CamelContext getContext() {
		if(context==null)
			context = new DefaultCamelContext();
		return context;
	}

}
