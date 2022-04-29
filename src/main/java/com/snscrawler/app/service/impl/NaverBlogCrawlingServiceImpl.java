package com.snscrawler.app.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.snscrawler.app.service.NaverBlogCrawlingService;
import com.snscrawler.app.util.SnsCrawlingUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NaverBlogCrawlingServiceImpl implements NaverBlogCrawlingService {

	/* 네이버 블로그 게시물 크롤링 */
	@Override
	public Map<String, Object> getNaverBlogCrawlingData(String url) {
		log.info("--- 네이버 블로그 컨텐츠 크롤링 호출: " + url + " ---");
		
		StopWatch stop_watch = new StopWatch();
		stop_watch.start();
		
		Map<String, Object> result_map = new HashMap<String, Object>();
		
		// 드라이버 설정
		ChromeDriver driver = new ChromeDriver(SnsCrawlingUtil.setChromeDriver(null, true));
		
		// 입력값 검증
		if (SnsCrawlingUtil.chkNull(url).equals("")) {
			log.error("오류 발생: URL 미입력");
			
			if (driver != null) {
				driver.quit();
			}
			
			result_map.put("result", false);
			result_map.put("message", "URL 미입력");
			
			return result_map;
		}
		
		try {
			// 컨텐츠 크롤링 시작
			driver.get(url);
			WebDriverWait wait = new WebDriverWait(driver, 10);
			
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("viewTypeSelector")));
			} catch (Exception e) {
				log.error("오류 발생: 컨텐츠 존재하지 않음");
				
				if (driver != null) {
					driver.quit();
				}
				
				result_map.put("result", false);
				result_map.put("contents_url", url);
				result_map.put("message", "컨텐츠 존재하지 않음");
				
				return result_map;
			}
			
			// 내용
			String contents = "";
			
			try {
				contents = driver.findElement(By.cssSelector("div.se-main-container")).getText();
			} catch (Exception e) {
				contents = driver.findElement(By.id("viewTypeSelector")).getText();
			}
			
			// 썸네일
			String thumbnail_url = driver.findElement(By.xpath("//meta[@property='og:image']")).getAttribute("content");
			
			// 좋아요 수
			int like_cnt = 0;
			
			try {
				like_cnt = Integer.parseInt(driver.findElement(By.cssSelector("div.section_t1 em.u_cnt")).getText());
			} catch (Exception e) {}
					
			// 댓글 수
			int comment_cnt = 0;
			
			try {
				comment_cnt = Integer.parseInt(driver.findElement(By.cssSelector("div.section_t1 a.btn_reply em")).getText());
			} catch (Exception e) {}
			
			// 정보 취합
			result_map.put("code", 200);
			
			Map<String, Object> result_data_map = new HashMap<>();
			result_data_map.put("text", contents);
			result_data_map.put("pic", thumbnail_url);
			result_data_map.put("thumb", thumbnail_url);
			result_data_map.put("likes", like_cnt);
			result_data_map.put("comments", comment_cnt);
			result_data_map.put("shares", 0);
			result_data_map.put("embed", driver.getPageSource());
			result_data_map.put("crawl_res", "");
		} catch (Exception e) {
			log.error("오류 발생: 컨텐츠 크롤링 중 오류 발생");
			e.printStackTrace();
			
			if (driver != null) {
				driver.quit();
			}
			
			result_map.put("result", false);
			result_map.put("contents_url", url);
			result_map.put("message", "컨텐츠 크롤링 중 오류 발생");
			
			return result_map;
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
		
		stop_watch.stop();
		log.info("--- 네이버 블로그 컨텐츠 크롤링 완료(" + stop_watch.getTotalTimeSeconds() + "초) ---");
		
		return result_map;
	}
	
}