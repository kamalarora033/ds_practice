package com.ericsson.fdp.business.response.fulfillment.xml;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
/**
 * 
 * @author GUR36857
 *
 */
@XmlRootElement(name = "activebundles")
@XmlAccessorType(XmlAccessType.FIELD)
public class ActiveBundles implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	@XmlElement(name="bundle")
	private List<Bundle> bundles;

	/**
	 * @return the bundles
	 */
	public List<Bundle> getBundles() {
		return bundles;
	}

	/**
	 * @param bundles the bundles to set
	 */
	public void setBundles(List<Bundle> bundles) {
		this.bundles = bundles;
	}
	
	
	

}
