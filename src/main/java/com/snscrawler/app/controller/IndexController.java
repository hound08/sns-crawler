package com.snscrawler.app.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class IndexController {

	@RequestMapping("/")
	public String index() {
		log.info("메인 화면 진입");
		
		return "index";
	}
	
}
