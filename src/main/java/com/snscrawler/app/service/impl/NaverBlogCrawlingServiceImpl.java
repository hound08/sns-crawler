package com.snscrawler.app.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.snscrawler.app.service.NaverBlogCrawlingService;
import com.snscrawler.app.util.SnsCrawlingUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class NaverBlogCrawlingServiceImpl implements NaverBlogCrawlingService {

	/* 네이버 블로그 게시물 크롤링 */
	@Override
	public Map<String, Object> getNaverBlogCrawlingData(String url) {
		log.info("네이버 블로그 API 호출");
		
		Map<String, Object> result_map = new LinkedHashMap<>();
		
		// 드라이버 설정
		ChromeDriver driver = new ChromeDriver(SnsCrawlingUtil.setChromeDriver(null, false));
		
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
				url = url.replaceAll("m.blog", "blog");
				
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
					result_map.put("result", false);
					result_map.put("message", "게시물 없음");
					
					return result_map;
				}
				
				driver.switchTo().frame(driver.findElement(By.id("mainFrame")));
				wait = new WebDriverWait(driver, 5);
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("printPost1")));
				
				// 게시물 제목
				String title = driver.findElement(By.cssSelector("div.se-section-documentTitle div.se-title-text span")).getText();
				
				// 게시자
				String name = driver.findElement(By.cssSelector("span.writer > span.nick")).getText();
				
				// 작성일
				String post_date = driver.findElement(By.className("se_publishDate")).getText();
				
				// 첫번째 업로드 이미지
				String img_src = driver.findElement(By.className("se-image-resource")).getAttribute("src");
				
				// 게시물 내용
				String content = driver.findElement(By.className("se-main-container")).getText();
				
				// 공감 수
				int like_cnt = 0;
				
				try {
					String like_cnt_str = SnsCrawlingUtil.chkNull(driver.findElement(By.cssSelector("tbody div.wrap_postcomment em.u_cnt")).getText());
					
					if (!"".equals(like_cnt_str)) {
						like_cnt = Integer.parseInt(like_cnt_str);
					}
				} catch (Exception e) {}
				
				// 댓글 수
				int reply_cnt = 0;
				
				try {
					String reply_cnt_str = SnsCrawlingUtil.chkNull(driver.findElement(By.id("commentCount")).getText());
					
					if (!"".equals(reply_cnt_str)) {
						reply_cnt = Integer.parseInt(reply_cnt_str);
					}
				} catch (Exception e) {}
				
				result_map.put("url", url);
				result_map.put("title", title);
				result_map.put("name", name);
				result_map.put("post_date", post_date);
				result_map.put("img_src", img_src);
				result_map.put("content", content);
				result_map.put("like_cnt", like_cnt);
				result_map.put("reply_cnt", reply_cnt);
				result_map.put("result", true);
			} catch (Exception e) {
				log.error("오류 발생!: " + url + " 크롤링 중 오류 발생");
				e.printStackTrace();
				
				if (driver != null) {
					driver.quit();
				}
				
				result_map.put("url", url);
				result_map.put("result", false);
				result_map.put("message", "크롤링 중 오류 발생");
				
				return result_map;
			} finally {
				if (driver != null) {
					driver.quit();
				}
			}
			
			log.info("---------------- 네이버 블로그 크롤링 완료 ----------------");
		}
		
		return result_map;
	}
	
}