package com.snscrawler.app.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.snscrawler.app.service.SmartStoreCrawlingService;

@RestController
@RequestMapping("/image")
public class SmartStoreCrawlingController {

	@Autowired
	private SmartStoreCrawlingService smartStoreCrawlingService;
	
	/* 스마트 스토어 크롤링 API */
	@GetMapping(value = "/smartstore")
	public Map<String, Object> restApiSmartStore(@RequestParam String url) {
		return smartStoreCrawlingService.getSmartStoreCrawlingData(url);
	}
	
	/* 자사몰 크롤링 API */
	@GetMapping(value = "/store")
	public Map<String, Object> restApiStore(@RequestParam String url) {
		return smartStoreCrawlingService.getStoreCrawlingData(url);
	}
	
}
