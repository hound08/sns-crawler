package com.snscrawler.app.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.snscrawler.app.entity.AccountEntity;
import com.snscrawler.app.repository.AccountRepository;
import com.snscrawler.app.service.YouTubeCrawlingService;
import com.snscrawler.app.util.SnsCrawlingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class YouTubeCrawlingServiceImpl implements YouTubeCrawlingService {
	
	private final AccountRepository accountRepository;
	
	/* 유튜브 동영상 정보 크롤링 */
	@Override
	public Map<String, Object> getYouTubeVideoCrawlingData(String url) {
		log.info("--- 유튜브 동영상 정보 크롤링 API 호출 ---");
		
		StopWatch stop_watch = new StopWatch();
		stop_watch.start();
		
		Map<String, Object> result_map = new LinkedHashMap<>();
		
		// 계정 정보 가져오기
		Random random = new Random();
		List<AccountEntity> account_list = accountRepository.findByBlock(0);
		AccountEntity profile_entity = account_list.get(random.nextInt(account_list.size()));
		
		// 드라이버 설정
		ChromeDriver driver = new ChromeDriver(SnsCrawlingUtil.setChromeDriver(profile_entity, false));
		
		// 입력값 검증
		if (url == null || "".equals(url)) {
			if (driver != null) {
				driver.quit();
			}
			
			result_map.put("result", false);
			result_map.put("message", "URL 미입력");
			
			return result_map;
		} else {
			try {
				url = url.replaceAll(" ", "");
				
				// Javascript 실행기 설정
				JavascriptExecutor je = (JavascriptExecutor) driver;
				
				// 크롤링 시작
				driver.get(url);
				WebDriverWait wait = new WebDriverWait(driver, 20);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div#meta-contents div#top-row div#upload-info div#container div#text-container a.yt-simple-endpoint")));
				
				// 작성자
				String ownerName = driver.findElement(By.cssSelector("div#meta-contents div#top-row div#upload-info div#container div#text-container a.yt-simple-endpoint")).getText();
				
				// 제목
				String videoTitle = "";
				
				try {
					videoTitle = driver.findElement(By.cssSelector("#container > h1 > yt-formatted-string span.style-scope")).getText();
				} catch (Exception e) {
					videoTitle = driver.findElement(By.cssSelector("#container > h1 > yt-formatted-string")).getText();
				}
				
				// 작성일
				String postingDate = "";
				
				try {
					postingDate = driver.findElement(By.cssSelector("#watch7-content > meta:nth-child(21)")).getAttribute("content");
				} catch (Exception e) {
					postingDate = driver.findElement(By.cssSelector("#watch7-content > meta:nth-child(17)")).getAttribute("content");
				}
				
				// 조회 수
				int viewCount = 0;
				
				try {
					viewCount = Integer.parseInt(driver.findElement(By.cssSelector("#watch7-content > meta:nth-child(19)")).getAttribute("content"));
				} catch (Exception e) {
					viewCount = Integer.parseInt(driver.findElement(By.cssSelector("#watch7-content > meta:nth-child(15)")).getAttribute("content"));
				}
				
				// 좋아요 수
				int likeCount = 0;
				String likeCount_string = driver.findElement(By.cssSelector("div#menu-container div#top-level-buttons-computed yt-formatted-string#text")).getAttribute("aria-label");
				
				if (likeCount_string != null && !"".equals(likeCount_string)) {
					likeCount_string = likeCount_string.replace("좋아요 ", "");
					likeCount_string = likeCount_string.replace("개", "");
					likeCount_string = likeCount_string.replace(",", "");
					likeCount = Integer.parseInt(likeCount_string);
				}
				
				// 댓글 수
				// 댓글 목록 불러오기 위해 화면 스크롤
				je.executeScript("window.scrollBy(0, 1000)", "");
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("ytd-comments#comments #count")));
				int commentCount = 0;
				String commentCount_string = driver.findElement(By.cssSelector("ytd-comments#comments #count > yt-formatted-string > span:nth-child(2)")).getText();
				
				if (commentCount_string != null && !"".equals(commentCount_string)) {
					commentCount_string = commentCount_string.replace(",", "");
					commentCount = Integer.parseInt(commentCount_string);
				}
				
				// 프로필 사진
				String ownerImg = driver.findElement(By.cssSelector("div#meta-contents div#container div#top-row yt-img-shadow#avatar img#img")).getAttribute("src");
				
				// 동영상 썸네일
				String videoThumbnail = driver.findElement(By.cssSelector("#watch7-content > link:nth-child(11)")).getAttribute("href");
				
				// 내용 취합
				Map<String, Object> videoInfo = new LinkedHashMap<>();
				videoInfo.put("ownerName", ownerName);
				videoInfo.put("videoTitle", videoTitle);
				videoInfo.put("postingDate", postingDate);
				videoInfo.put("viewCount", viewCount);
				videoInfo.put("likeCount", likeCount);
				videoInfo.put("commentCount", commentCount);
				videoInfo.put("ownerImg", ownerImg);
				videoInfo.put("videoThumbnail", videoThumbnail);
				
				result_map.put("result", true);
				result_map.put("message", "Success");
				result_map.put("url", url);
				result_map.put("videoInfo", videoInfo);
			} catch (Exception e) {
				log.error("오류 발생!: " + url + " 크롤링 중 오류 발생");
				e.printStackTrace();
				
				if (driver != null) {
					driver.quit();
				}
				
				result_map.put("proxy", profile_entity.getProxy());
				result_map.put("account", profile_entity.getId());
				result_map.put("result", false);
				result_map.put("message", "크롤링 중 오류 발생");
				result_map.put("url", url);
				
				return result_map;
			} finally {
				if (driver != null) {
					driver.quit();
				}
			}
			
			stop_watch.stop();
			log.info("--- 유튜브 동영상 크롤링 완료(" + stop_watch.getTotalTimeSeconds() + "초) ---");
		}
		
        return result_map;
	}
	
}