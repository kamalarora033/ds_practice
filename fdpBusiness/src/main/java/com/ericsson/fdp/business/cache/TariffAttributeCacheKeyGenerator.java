package com.ericsson.fdp.business.cache;

import com.ericsson.fdp.common.enums.TariffEnquiryNetwork;
import com.ericsson.fdp.common.enums.TariffEnquirySpan;
import com.ericsson.fdp.common.enums.TariffEnquiryType;

public class TariffAttributeCacheKeyGenerator {

	public static void main(String[] args) {

		printAttributesCache();
	}
	
	public static void printAttributesCache() {
		int keyCounts = 0;
		//String excludeKey[] = {};
		for(final TariffEnquiryType types : TariffEnquiryType.values()) {
			for(final TariffEnquirySpan spans : TariffEnquirySpan.values()) {
				for(final TariffEnquiryNetwork networks : TariffEnquiryNetwork.values()) {
					//String type = types.getName();
					//if(Arrays.asList(excludeKey).contains(type.toLowerCase())) {
						String name = types.getName().toUpperCase()+"_"+spans.getText().toUpperCase()+"_"+networks.getText().toUpperCase();
						System.out.println(name+"("+types.getTypeId()+spans.getSpanId()+networks.getNetworkId()+",\""+name+"\"),");
						keyCounts++;
					//}
				}
			}
		}
		System.out.println("Total Keys Generated:"+keyCounts);
	}
}
