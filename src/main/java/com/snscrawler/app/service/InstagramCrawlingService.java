package com.snscrawler.app.service;

import java.util.Map;

public interface InstagramCrawlingService {

	Map<String, Object> getInstagramCrawlingMedia(String url);
	
	Map<String, Object> getInstagramCrawlingAccount(String username);

	Map<String, Object> getInstagramListScreenshot(String query_string, String url);

	Map<String, Object> getInstagramScoringAccount(String url);

	Map<String, Object> getInstagramScoringMedia(String url);

}
