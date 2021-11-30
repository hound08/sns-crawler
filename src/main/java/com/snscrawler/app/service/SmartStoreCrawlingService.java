package com.snscrawler.app.service;

import java.util.Map;

public interface SmartStoreCrawlingService {

	Map<String, Object> getSmartStoreCrawlingData(String url);
	
	Map<String, Object> getStoreCrawlingData(String url);

}
