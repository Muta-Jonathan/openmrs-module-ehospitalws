package org.openmrs.module.ehospitalws.service;

import org.openmrs.module.ehospitalws.util.OpenMRSPropertiesUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class SmsService {

	private static final Logger log = LoggerFactory.getLogger(SmsService.class);

	private final String smsApiUrl;

	private final String apiKey;

	private final String partnerId;

	private final String shortcode;

	private final boolean configured;

	public SmsService() {
		this.smsApiUrl = OpenMRSPropertiesUtil.getProperty("sms.api.url", null);
		this.apiKey = OpenMRSPropertiesUtil.getProperty("sms.api.key", null);
		this.partnerId = OpenMRSPropertiesUtil.getProperty("sms.partner.id", null);
		this.shortcode = OpenMRSPropertiesUtil.getProperty("sms.shortcode", null);
		this.configured = smsApiUrl != null && apiKey != null && partnerId != null && shortcode != null;
		if (!configured) {
			log.warn("SMS service is not configured. Set sms.api.url, sms.api.key, sms.partner.id and "
			        + "sms.shortcode in openmrs-runtime.properties. SMS sending is disabled until these are set.");
		}
	}

	public boolean sendSms(String phoneNumber, String message) {
		if (!configured) {
			log.error("Cannot send SMS to {}: SMS gateway properties are not configured.", phoneNumber);
			return false;
		}

		RestTemplate restTemplate = new RestTemplate();

		// Create the request body
		Map<String, String> requestBody = new HashMap<>();
		requestBody.put("apikey", apiKey);
		requestBody.put("partnerID", partnerId);
		requestBody.put("mobile", phoneNumber);
		requestBody.put("message", message);
		requestBody.put("shortcode", shortcode);
		requestBody.put("pass_type", "plain");

		try {
			// Send the POST request
			ResponseEntity<String> response = restTemplate.postForEntity(smsApiUrl, requestBody, String.class);
			return response.getStatusCode() == HttpStatus.OK;
		}
		catch (Exception e) {
			log.error("Failed to send SMS to {}", phoneNumber, e);
			return false;
		}
	}
}
