package com.snscrawler.app.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.snscrawler.app.service.InstagramCrawlingService;

@RestController
@RequestMapping("/insta")
public class InstagramCrawlingController {

	@Autowired
	private InstagramCrawlingService instagramCrawlingService;
	
	/* 인스타그램 게시물 정보 크롤링 API */
	@GetMapping(value = "/media")
	public Map<String, Object> restApiInstagramMedia(@RequestParam String url) {
		return instagramCrawlingService.getInstagramCrawlingMedia(url);
	}
	
	/* 인스타그램 계정 정보 크롤링 API */
	@GetMapping(value = "/account")
	public Map<String, Object> restApiInstagramAccount(@RequestParam String username) {
		return instagramCrawlingService.getInstagramCrawlingAccount(username);
	}
	
	/* 인스타그램 목록 스크린샷 */
	@GetMapping(value = "/screenshot")
	public Map<String, Object> restApiInstagramListScreenshot(@RequestParam String query_string, @RequestParam String url) {
		return instagramCrawlingService.getInstagramListScreenshot(query_string, url);
	}
	
	/* 인스타그램 활동 점수 - 계정 */
	@GetMapping(value = "/scoring_account")
	public Map<String, Object> restApiInstagramScoringAccount(@RequestParam String url) {
		return instagramCrawlingService.getInstagramScoringAccount(url);
	}
	
	/* 인스타그램 활동 점수 - 게시물 */
	@GetMapping(value = "/scoring_media")
	public Map<String, Object> restApiInstagramScoringMedia(@RequestParam String url) {
		return instagramCrawlingService.getInstagramScoringMedia(url);
	}
	
}
