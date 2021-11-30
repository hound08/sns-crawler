package com.snscrawler.app.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.snscrawler.app.service.IptimeCrawlingService;

@RestController
@RequestMapping("/iptime")
public class IptimeCrawlingController {

	@Autowired
	private IptimeCrawlingService iptimeCrawlingService;
	
	/* ipTime MAC 주소 목록 가져오기 */
	@GetMapping(value = "/macaddress")
	public List<String> restApiIptimeMacAddress() {
		return iptimeCrawlingService.getIptimeMacAddress();
	}
	
}
