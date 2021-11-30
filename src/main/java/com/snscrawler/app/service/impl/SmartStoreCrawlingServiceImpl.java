package com.snscrawler.app.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.Map;

import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.snscrawler.app.service.SmartStoreCrawlingService;
import com.snscrawler.app.util.SnsCrawlingUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmartStoreCrawlingServiceImpl implements SmartStoreCrawlingService {

	/* 스마트 스토어 게시물 크롤링 */
	@Override
	public Map<String, Object> getSmartStoreCrawlingData(String url) {
		log.info("--- 스마트 스토어 API 호출 ---");
		
		Map<String, Object> result_map = new HashMap<String, Object>();
		
		// 드라이버 설정
		ChromeDriver driver = new ChromeDriver(SnsCrawlingUtil.setChromeDriver(null, false));
		
		// 입력값 검증
		Decoder decoder = Base64.getDecoder();
		byte[] decodedBytes = decoder.decode(url);
		url = new String(decodedBytes);
		
		log.info("url : " + url);
		
		if (url == null || "".equals(url)) {
			if (driver != null) {
				driver.quit();
			}
			
			result_map.put("result", false);
			result_map.put("msg", "주소 미입력");
			
			return result_map;
		} else {
			try {
				url = url.replaceAll(" ", "");
				
				driver.get(url);
				WebDriverWait wait = new WebDriverWait(driver, 20);
				
				try {
					wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.detail_viewer")));
				} catch (Exception e) {
					log.error("오류 발생: " + url + " 상품 없음");
					
					if (driver != null) {
						driver.quit();
					}
					
					result_map.put("result", false);
					result_map.put("url", url);
					result_map.put("msg", "상품 없음");
					
					return result_map;
				}
				
				// 이미지 스크린샷 저장
				WebElement element = driver.findElement(By.cssSelector("div.detail_viewer"));    
				
				JavascriptExecutor je = (JavascriptExecutor) driver;
			    int required_width = Integer.parseInt(je.executeScript("return document.body.parentNode.scrollWidth", "").toString());
			    int required_height = Integer.parseInt(je.executeScript("return document.body.parentNode.scrollHeight", "").toString());
			    Dimension dimension = new Dimension(required_width, required_height);
			    driver.manage().window().setSize(dimension);
				
				File file_screenshot = element.getScreenshotAs(OutputType.FILE);
				Path originfile = Paths.get(file_screenshot.getAbsolutePath());
				Path newFile = Paths.get("/home/lunchbus/image/" + file_screenshot.getName());
				Files.move(originfile, newFile);
				
				result_map.put("url", newFile.toString());
			} catch (Exception e) {
				log.error("오류 발생: " + url + " 크롤링 중 오류 발생");
				e.printStackTrace();
				
				if (driver != null) {
					driver.quit();
				}
				
				result_map.put("result", false);
				result_map.put("url", url);
				result_map.put("msg", "크롤링 중 오류 발생");
				
				return result_map;
			} finally {
				if (driver != null) {
					driver.quit();
				}
			}
			
			log.info("--- 스마트 스토어 크롤링 완료 ---");
		}
		
		return result_map;
	}
	
	/* 자사몰 게시물 크롤링 */
	@Override
	public Map<String, Object> getStoreCrawlingData(String url) {
		log.info("--- 자사몰 API 호출 ---");
		
		Map<String, Object> result_map = new HashMap<String, Object>();
		
		// 드라이버 설정
		ChromeDriver driver = new ChromeDriver(SnsCrawlingUtil.setChromeDriver(null, false));
		
		// 입력값 검증
		Decoder decoder = Base64.getDecoder();
		byte[] decodedBytes = decoder.decode(url);
		url = new String(decodedBytes);
		
		if (url == null || "".equals(url)) {
			if (driver != null) {
				driver.quit();
			}
			
			result_map.put("result", false);
			result_map.put("msg", "주소 미입력");
			
			return result_map;
		} else {
			try {
				url = url.replaceAll(" ", "");
				
				driver.get(url);
				Thread.sleep(5000);
				
				// 이미지 스크린샷 저장
				JavascriptExecutor je = (JavascriptExecutor) driver;
			    int required_width = Integer.parseInt(je.executeScript("return document.body.parentNode.scrollWidth", "").toString());
			    int required_height = Integer.parseInt(je.executeScript("return document.body.parentNode.scrollHeight", "").toString());
			    Dimension dimension = new Dimension(required_width, required_height);
			    driver.manage().window().setSize(dimension);
				
				File file_screenshot = driver.getScreenshotAs(OutputType.FILE);
				Path originfile = Paths.get(file_screenshot.getAbsolutePath());
				Path newFile = Paths.get("/home/lunchbus/image/" + file_screenshot.getName());
				Files.move(originfile, newFile);
				
				result_map.put("url", newFile.toString());
			} catch (Exception e) {
				log.error("오류 발생: " + url + " 크롤링 중 오류 발생");
				e.printStackTrace();
				
				if (driver != null) {
					driver.quit();
				}
				
				result_map.put("result", false);
				result_map.put("url", url);
				result_map.put("msg", "크롤링 중 오류 발생");
				
				return result_map;
			} finally {
				if (driver != null) {
					driver.quit();
				}
			}
			
			log.info("--- 자사몰 크롤링 완료 ---");
		}
		
		return result_map;
	}
	
}