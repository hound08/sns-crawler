package com.snscrawler.app.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.snscrawler.app.service.NaverBlogCrawlingService;

@RestController
@RequestMapping("/naver")
public class NaverBlogCrawlingController {

	@Autowired
	private NaverBlogCrawlingService naverBlogCrawlingService;
	
	/* 네이버 블로그 크롤링 API */
	@GetMapping(value = "/media")
	public Map<String, Object> restApiNaverBlog(@RequestParam String url) {
		return naverBlogCrawlingService.getNaverBlogCrawlingData(url);
	}
	
}
