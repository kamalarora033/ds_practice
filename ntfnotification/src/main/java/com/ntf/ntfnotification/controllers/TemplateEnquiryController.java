package com.ntf.ntfnotification.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.ntf.ntfnotification.model.TemplateEnquiryResponse;

@RestController
@RequestMapping("/notificationTemplateEnquiry/v1/")
public class TemplateEnquiryController {

	@RequestMapping(value = "notificationTemplate", method = RequestMethod.GET)
	public List<TemplateEnquiryResponse> templateEnquiry() {

		List<TemplateEnquiryResponse> aoResponse = new ArrayList<TemplateEnquiryResponse>();
		for (int i = 1; i <= 10; i++) {
			TemplateEnquiryResponse response = new TemplateEnquiryResponse();
			response.setApplicationId("CHA");
			response.setCategory("Refill " + i);
			response.setDescription("NoSubscription " + i);
			response.setId("101010 " + i);
			response.setName("PromoRewardReachingThreshold");
			response.setSchemaVersion("1.0." + i);
			aoResponse.add(response);
		}

		return aoResponse;

	}
}
