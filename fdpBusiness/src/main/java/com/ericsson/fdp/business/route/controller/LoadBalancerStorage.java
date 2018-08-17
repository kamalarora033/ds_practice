package com.ericsson.fdp.business.route.controller;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Singleton;

@Singleton(name = "LoadBalancerStorage")
public class LoadBalancerStorage {

	private Map<String, Object> loadBalancerMap;

	public Map<String, Object> getLoadBalancerMap() {
		if (loadBalancerMap == null) {
			loadBalancerMap = new HashMap<String, Object>();
		}
		return loadBalancerMap;

	}
}
