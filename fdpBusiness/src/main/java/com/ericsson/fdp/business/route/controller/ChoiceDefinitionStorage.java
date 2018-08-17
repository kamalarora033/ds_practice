package com.ericsson.fdp.business.route.controller;

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Singleton;

@Singleton
public class ChoiceDefinitionStorage {
	private Map<String, Object> choiceDefinitionMap;
	
	
	public Map<String, Object> getchoiceDefinitionMap() {
		if (choiceDefinitionMap == null) {
			choiceDefinitionMap = new HashMap<String, Object>();
		}
		return choiceDefinitionMap;

	}
}
