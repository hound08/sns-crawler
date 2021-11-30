package com.snscrawler.app.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.snscrawler.app.service.DinnerQueenCrawlingService;
import com.snscrawler.app.util.SnsCrawlingUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class DinnerQueenCrawlingServiceImpl implements DinnerQueenCrawlingService {

	/* 디너의 여왕 블로그 리뷰 하단 배너 검사 */
	@Override
	public Map<String, Object> setDinnerReviewBannerCheck(String dining_seq, String url) {
		log.info("디너의 여왕 리뷰 하단 배너 검사 API 호출");
		
		Map<String, Object> result_map = new LinkedHashMap<>();
		
		// 드라이버 설정
		ChromeDriver driver = new ChromeDriver(SnsCrawlingUtil.setChromeDriver(null, false));
		
		Boolean is_footer_banner = false;
		int result_code = 404;
		
		// 입력값 검증
		if (dining_seq == null || "".equals(dining_seq) || url == null || "".equals(url)) {
			if (driver != null) {
				driver.quit();
			}
			
			result_map.put("result", is_footer_banner);
			result_map.put("result_code", result_code);
			result_map.put("message", "매개변수 미입력");
			
			return result_map;
		} else {
			try {
				url = url.replaceAll(" ", "");
				url = url.replaceAll("m.blog", "blog");
				url = url.replaceAll("http:", "https:");
				
				driver.get(url);
				WebDriverWait wait = new WebDriverWait(driver, 5);
				
				try {
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("mainFrame")));
				} catch (Exception e) {
					log.error("오류 발생!: " + url + " 게시물 없음");
					
					if (driver != null) {
						driver.quit();
					}
					
					result_map.put("url", url);
					result_map.put("dining_seq", dining_seq);
					result_map.put("result", is_footer_banner);
					result_map.put("result_code", result_code);
					result_map.put("message", "게시물 없음");
					
					return result_map;
				}
				
				driver.switchTo().frame(driver.findElement(By.id("mainFrame")));
				wait = new WebDriverWait(driver, 5);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("printPost1")));
				
				// 리뷰 하단 푸터 배너 적용 여부 검사
				String message = "";
				List<WebElement> image_elements = driver.findElements(By.cssSelector("#postListBody img"));
				
				for (WebElement image_element : image_elements) {
					if (image_element.getAttribute("src").indexOf("_footer") >= 0) {
						if (image_element.getAttribute("src").indexOf(dining_seq + "_footer") >= 0) {
							is_footer_banner = true;
							result_code = 200;
							message = "성공";
							break;
						} else {
							result_code = 500;
							message = "SEQ 값 다름";
						}
					}
				}
				
				if (result_code == 404) {
					message = "배너 없음";
				}
				
				result_map.put("url", url);
				result_map.put("dining_seq", dining_seq);
				result_map.put("result", is_footer_banner);
				result_map.put("result_code", result_code);
				result_map.put("message", message);
			} catch (Exception e) {
				log.error("오류 발생!: " + url + " 크롤링 중 오류 발생");
				e.printStackTrace();
				
				if (driver != null) {
					driver.quit();
				}
				
				result_map.put("url", url);
				result_map.put("dining_seq", dining_seq);
				result_map.put("result", is_footer_banner);
				result_map.put("result_code", 500);
				result_map.put("message", "크롤링 중 오류 발생");
				
				return result_map;
			} finally {
				if (driver != null) {
					driver.quit();
				}
			}
			
			log.info("---------------- 디너의여왕 리뷰 배너 크롤링 완료 ----------------");
		}
		
		return result_map;
	}
	
}