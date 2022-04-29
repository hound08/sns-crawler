package com.snscrawler.app.service.impl;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.snscrawler.app.service.YouTubeCrawlingService;
import com.snscrawler.app.util.SnsCrawlingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeCrawlingServiceImpl implements YouTubeCrawlingService {
	
	/* 유튜브 동영상 정보 크롤링 */
	@Override
	public Map<String, Object> getYouTubeVideoCrawlingData(String url) {
		log.info("--- 유튜브 컨텐츠 크롤링 호출: " + url + " ---");
		
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
			WebDriverWait wait = new WebDriverWait(driver, 20);
			
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.slim-video-information-content")));
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
			
			// 제목
			String script_code = driver.findElement(By.xpath("//script[@type='application/ld+json']")).getAttribute("innerHTML");
			JSONParser parser = new JSONParser();
			JSONObject json_object = (JSONObject) parser.parse(script_code);
			String title = json_object.get("name").toString();
			
			// 썸네일
			JSONArray json_array = (JSONArray) json_object.get("thumbnailUrl");
			String thumbnail_url = json_array.get(0).toString();
			
			// 작성자
			String writer = json_object.get("author").toString();
			
			// 프로필 이미지
			String profile_url = driver.findElement(By.cssSelector("a.slim-owner-icon-and-title img")).getAttribute("src");
			
			// 작성일
			String reg_date = json_object.get("uploadDate").toString();
			
			// 조회 수
			int viewCount = Integer.parseInt(json_object.get("interactionCount").toString());
			
			// 좋아요 수
			int like_cnt = 0;
			
			try {
				String like_cnt_string = driver.findElement(By.cssSelector("div.slim-video-action-bar-actions button.c3-material-button-button")).getAttribute("aria-label");
				like_cnt = Integer.parseInt(like_cnt_string.replaceAll("[^\\d]", ""));
			} catch (Exception e) {}
			
			// 댓글 수
			int comment_cnt = 0;
			
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("button.cbox span.formatted-string-text")));
				String comment_cnt_string = driver.findElement(By.cssSelector("button.cbox span.formatted-string-text")).getText();
				comment_cnt = Integer.parseInt(comment_cnt_string.replaceAll("[^\\d]", ""));
			} catch (Exception e) {}
			
			// 정보 취합
			Map<String, Object> videoInfo = new LinkedHashMap<>();
			videoInfo.put("ownerName", writer);
			videoInfo.put("videoTitle", title);
			videoInfo.put("postingDate", reg_date);
			videoInfo.put("viewCount", viewCount);
			videoInfo.put("likeCount", like_cnt);
			videoInfo.put("commentCount", comment_cnt);
			videoInfo.put("ownerImg", profile_url);
			videoInfo.put("videoThumbnail", thumbnail_url);
			
			result_map.put("result", true);
			result_map.put("message", "Success");
			result_map.put("url", url);
			result_map.put("videoInfo", videoInfo);
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
		log.info("--- 유튜브 컨텐츠 크롤링 완료(" + stop_watch.getTotalTimeSeconds() + "초) ---");
		
		return result_map;
	}
	
}