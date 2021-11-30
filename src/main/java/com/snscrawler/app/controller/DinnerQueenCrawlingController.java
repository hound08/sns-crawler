package com.snscrawler.app.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.snscrawler.app.service.DinnerQueenCrawlingService;

@RestController
@RequestMapping("/dinner")
public class DinnerQueenCrawlingController {

	@Autowired
	private DinnerQueenCrawlingService dinnerQueenCrawlingService;
	
	/* 리뷰 글 하단 배너 검사 */
	@GetMapping(value = "/review/banner")
	public Map<String, Object> restApiDinnerReviewBannerCheck(@RequestParam String dining_seq, @RequestParam String url) {
		return dinnerQueenCrawlingService.setDinnerReviewBannerCheck(dining_seq, url);
	}
	
}
