package com.snscrawler.app.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;

import com.snscrawler.app.service.IptimeCrawlingService;
import com.snscrawler.app.util.SnsCrawlingUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class IptimeCrawlingServiceImpl implements IptimeCrawlingService {

	/* ipTime MAC 주소 목록 가져오기 */
	@Override
	public List<String> getIptimeMacAddress() {
		log.info("ipTime MAC 주소 목록 가져오기 API 호출");
		
		List<String> result_list = new ArrayList<String>();
		
		// 드라이버 설정
		ChromeDriver driver = new ChromeDriver(SnsCrawlingUtil.setChromeDriver(null, false));
		
		try {
			driver.get("http://ip/login/login.cgi");
			WebDriverWait wait = new WebDriverWait(driver, 10);
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("submit_bt")));
			
			driver.findElement(By.name("username")).sendKeys("username");
			driver.findElement(By.name("passwd")).sendKeys("passwd");
			driver.findElement(By.id("submit_bt")).click();
			
			wait.until(ExpectedConditions.visibilityOfElementLocated(By.name("main_body")));
			driver.switchTo().frame(driver.findElement(By.name("main_body")));
			driver.switchTo().frame(driver.findElement(By.name("navi_menu_advance")));
			driver.findElement(By.id("advance_setup_td")).click();
			driver.findElement(By.id("netconf_setup_td")).click();
			driver.findElement(By.id("netconf_lansetup_3_td")).click();
			
			driver.switchTo().defaultContent();
			driver.switchTo().frame(driver.findElement(By.name("main_body")));
			driver.switchTo().frame(driver.findElement(By.name("main")));
			driver.switchTo().frame(driver.findElement(By.name("lan_pcinfo")));
			
			List<WebElement> mac_elements = driver.findElements(By.cssSelector("table.lansetup_main_table tbody td:nth-child(2)"));
			
			for (WebElement mac_element : mac_elements) {
				result_list.add(mac_element.getText());
			}
		} catch (Exception e) {
			log.error("크롤링 중 오류 발생");
			e.printStackTrace();
			
			if (driver != null) {
				driver.quit();
			}
			
			result_list.add("크롤링 중 오류 발생");
			result_list.add(e.getMessage());
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
		
		log.info("---------------- ipTime MAC 주소 크롤링 종료 ----------------");
		
		return result_list;
	}
	
}