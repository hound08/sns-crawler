package com.snscrawler.app.util;

import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;

import com.snscrawler.app.entity.AccountEntity;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SnsCrawlingUtil {
	
	/* 크롬 드라이버 설정 */
	public static ChromeOptions setChromeDriver(AccountEntity profile_entity, boolean is_mobile) {
		String web_driver_id = "webdriver.chrome.driver";
		// String web_driver_path = "D:/workspace/chromedriver.exe";
		String web_driver_path = "/home/lunchbus/chromedriver";
		System.setProperty(web_driver_id, web_driver_path);
		
		ChromeOptions options = new ChromeOptions();
		options.addArguments("--headless", "--disable-gpu", "--no-sandbox");
		options.addArguments("window-size=1920x1080");
		options.addArguments("lang=ko_KR");
		
		if (is_mobile) {
			options.addArguments("user-agent=Mozilla/5.0 (Linux; Android 6.0.1; Nexus 6P Build/MMB29P) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Mobile Safari/537.36");
		} else {
			options.addArguments("user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.45 Safari/537.36");
		}
		
		// 계정 필요한 크롤링일 경우 추가 설정
		if (profile_entity != null) {
			// options.addArguments("user-data-dir=C:/Users/dinnerqueen/AppData/Local/Google/Chrome/User Data/Profile " + profile_entity.getNum());
			options.addArguments("user-data-dir=/home/lunchbus/.config/google-chrome/Profile " + profile_entity.getNum());
			
			Proxy proxy = new Proxy();
			proxy.setHttpProxy(profile_entity.getProxy());
			options.setCapability("proxy", proxy);
			
			log.info("사용 프록시: " + profile_entity.getProxy());
			log.info("사용 계정: " + profile_entity.getId());
			log.info("사용 크롬 프로필: Profile " + profile_entity.getNum());
		}
		
		return options;
	}
	
	/* 문자열 null 검사 */
	public static String chkNull(String check_string) {
		return check_string == null ? "" : check_string;
	}
	
}
