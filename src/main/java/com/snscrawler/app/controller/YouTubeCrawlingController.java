package com.snscrawler.app.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.snscrawler.app.service.YouTubeCrawlingService;

@RestController
@RequestMapping("/youtube")
public class YouTubeCrawlingController {

	@Autowired
	private YouTubeCrawlingService youTubeCrawlingService;
	
	/* 유튜브 크롤링 API */
	@GetMapping(value = "/videoInfo")
	public Map<String, Object> restApiYouTubeVideo(@RequestParam String url) {
		return youTubeCrawlingService.getYouTubeVideoCrawlingData(url);
	}
	
}
