package com.snscrawler.app.service.impl;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.snscrawler.app.entity.AccountEntity;
import com.snscrawler.app.repository.AccountRepository;
import com.snscrawler.app.service.InstagramCrawlingService;
import com.snscrawler.app.util.SnsCrawlingUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class InstagramCrawlingServiceImpl implements InstagramCrawlingService {
	
	private final AccountRepository accountRepository;
	
	/* 인스타그램 게시물 정보 크롤링 */
	@Override
	public Map<String, Object> getInstagramCrawlingMedia(String url) {
		log.info("--- 인스타그램 게시물 크롤링 API 호출 ---");
		
		StopWatch stop_watch = new StopWatch();
		stop_watch.start();
		
		Map<String, Object> result_map = new LinkedHashMap<>();
		
		// 계정 정보 가져오기
		Random random = new Random();
		List<AccountEntity> account_list = accountRepository.findByBlock(2);
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
				url = url.replaceAll("\\?utm_source=ig_web_copy_link", "");
				url = url.replaceAll("\\?utm_medium=copy_link", "");
				
				// URL 검사
				if (url.indexOf("/reel/") < 0 && url.indexOf("/p/") < 0) {
					if (driver != null) {
						driver.quit();
					}
					
					result_map.put("result", false);
					result_map.put("message", "URL 구조 이상");
					
					return result_map;
				}
				
				// JSON 정보로 게시물 정보 크롤링 시작
				driver.get(url + "?__a=1");
				String json_string = driver.getPageSource();
				
				json_string = json_string.replace("<html><head></head><body><pre style=\"word-wrap: break-word; white-space: pre-wrap;\">", "");
				json_string = json_string.replace("</pre></body></html>", "");
				
				// JSON 형식에 따른 파싱 분류
				if (json_string.indexOf("graphql") > -1) {
					// 'graphql' 형식
					JSONParser parser = new JSONParser();
					JSONObject json_object = (JSONObject) parser.parse(json_string);
					
					try {
						json_object = (JSONObject) json_object.get("graphql");
						json_object = (JSONObject) json_object.get("shortcode_media");
					} catch (Exception e) {
						log.error("오류 발생!: " + url + " 게시물 없음");
						
						if (driver != null) {
							driver.quit();
						}
						
						result_map.put("proxy", profile_entity.getProxy());
						result_map.put("account", profile_entity.getId());
						result_map.put("url", url);
						result_map.put("result", false);
						result_map.put("message", "게시물 없음");
						
						return result_map;
					}
					
					Thread.sleep(4000);
					
					// 작성자 이름
					JSONObject json_object_owner = (JSONObject) json_object.get("owner");
					String userName = json_object_owner.get("username").toString();
					
					// 작성자 ID
					String userNum = json_object_owner.get("id").toString();
					
					// 댓글 개수
					JSONObject json_object_commentCnt = (JSONObject) json_object.get("edge_media_to_parent_comment");
					int commentCnt = Integer.parseInt(json_object_commentCnt.get("count").toString());
					
					// 게시물 ID
					String postId = json_object.get("id").toString();
					
					// 게시물 썸네일
					String thumbImg = json_object.get("display_url").toString();
					thumbImg = thumbImg.replaceAll("&amp;", "&");
					
					// 내용
					String contents = "";
					
					try {
						JSONObject json_object_contents = (JSONObject) json_object.get("edge_media_to_caption");
						JSONArray json_array_contents = (JSONArray) json_object_contents.get("edges");
						json_object_contents = (JSONObject) json_array_contents.get(0);
						json_object_contents = (JSONObject) json_object_contents.get("node");
						contents = json_object_contents.get("text").toString();
						contents = contents.replaceAll("&amp;", "&");
					} catch (Exception e) {}
					
					// 좋아요 개수
					JSONObject json_object_likeCnt = (JSONObject) json_object.get("edge_media_preview_like");
					int likeCnt = Integer.parseInt(json_object_likeCnt.get("count").toString());
					
					if (likeCnt < 0) {
						likeCnt = 0;
					}
					
					// 작성일
					String taken_at_timestamp = json_object.get("taken_at_timestamp").toString();
					String postedAt = Instant.ofEpochSecond(Long.parseLong(taken_at_timestamp)).atZone(ZoneId.of("Asia/Seoul")).toString();
					postedAt = postedAt.replace("T", " ");
					postedAt = postedAt.replace("+09:00[Asia/Seoul]", "");
					
					// 게시물 주소
					String shortCode = json_object.get("shortcode").toString();
					
					// 동영상 조회수
					int videoViewCnt = 0;
					
					if (json_string.indexOf("video_view_count") > -1) {
						videoViewCnt = Integer.parseInt(json_string.substring(json_string.indexOf("video_view_count") + 18, json_string.indexOf("video_play_count") - 2));
					}
					
					// 게시물 타입
					String type = json_object.get("__typename").toString();
					
					if ("GraphSidecar".equals(type)) {
						type = "sidecar";
					} else if ("GraphImage".equals(type)) {
						type = "image";
					} else if ("GraphVideo".equals(type)) {
						type = "video";
					} else {
						type = "unknown";
					}
					
					// 게시물 정보
					result_map.put("userName", userName);
					result_map.put("commentCnt", commentCnt);
					result_map.put("postId", postId);
					result_map.put("contents", contents);
					result_map.put("likeCnt", likeCnt);
					result_map.put("postedAt", postedAt);
					result_map.put("thumbImg", thumbImg);
					result_map.put("shortCode", shortCode);
					result_map.put("result", true);
					result_map.put("userNum", userNum);
					result_map.put("videoViewCnt", videoViewCnt);
					result_map.put("url", url);
					result_map.put("type", type);
				} else {
					// 'items' 형식
					JSONParser parser = new JSONParser();
					JSONObject json_object = (JSONObject) parser.parse(json_string);
					
					try {
						JSONArray json_array_items = (JSONArray) json_object.get("items");
						json_object = (JSONObject) json_array_items.get(0);
					} catch (Exception e) {
						log.error("오류 발생!: " + url + " 게시물 없음");
						
						if (driver != null) {
							driver.quit();
						}
						
						result_map.put("proxy", profile_entity.getProxy());
						result_map.put("account", profile_entity.getId());
						result_map.put("url", url);
						result_map.put("result", false);
						result_map.put("message", "게시물 없음");
						
						return result_map;
					}
					
					Thread.sleep(4000);
					
					// 작성자 이름
					JSONObject json_object_user = (JSONObject) json_object.get("user");
					String userName = json_object_user.get("username").toString();
					
					// 작성자 ID
					String userNum = json_object_user.get("pk").toString();
					
					// 댓글 개수
					int commentCnt = 0;
					
					try {
						commentCnt = Integer.parseInt(json_object.get("comment_count").toString());
					} catch (Exception e) {}
					
					// 게시물 ID
					String postId = json_object.get("pk").toString();
					
					// 게시물 썸네일
					String thumbImg = "";
					
					try {
						JSONArray json_array_carousel_media = (JSONArray) json_object.get("carousel_media");
						JSONObject json_object_carousel_media = (JSONObject) json_array_carousel_media.get(0);
						JSONObject json_object_image_versions2 = (JSONObject) json_object_carousel_media.get("image_versions2");
						JSONArray json_array_candidates = (JSONArray) json_object_image_versions2.get("candidates");
						JSONObject json_object_candidates = (JSONObject) json_array_candidates.get(0);
						thumbImg = json_object_candidates.get("url").toString();
						thumbImg = thumbImg.replaceAll("&amp;", "&");
					} catch (Exception e) {}
					
					if ("".equals(thumbImg)) {
						try {
							JSONObject json_object_image_versions2 = (JSONObject) json_object.get("image_versions2");
							JSONArray json_array_candidates = (JSONArray) json_object_image_versions2.get("candidates");
							JSONObject json_object_candidates = (JSONObject) json_array_candidates.get(0);
							thumbImg = json_object_candidates.get("url").toString();
							thumbImg = thumbImg.replaceAll("&amp;", "&");
						} catch (Exception e) {}
					}
					
					// 내용
					String contents = "";
					
					try {
						JSONObject json_object_caption = (JSONObject) json_object.get("caption");
						contents = json_object_caption.get("text").toString();
						contents = contents.replaceAll("&amp;", "&");
					} catch (Exception e) {}
					
					// 좋아요 개수
					int likeCnt = Integer.parseInt(json_object.get("like_count").toString());
					
					if (likeCnt < 0) {
						likeCnt = 0;
					}
					
					// 작성일
					String taken_at = json_object.get("taken_at").toString();
					String postedAt = Instant.ofEpochSecond(Long.parseLong(taken_at)).atZone(ZoneId.of("Asia/Seoul")).toString();
					postedAt = postedAt.replace("T", " ");
					postedAt = postedAt.replace("+09:00[Asia/Seoul]", "");
					
					// 게시물 주소
					String shortCode = json_object.get("code").toString();
					
					// 동영상 조회수 (items 형식에선 미출력)
					int videoViewCnt = 0;
					
					/* if (json_string.indexOf("video_view_count") > -1) {
						videoViewCnt = Integer.parseInt(json_string.substring(json_string.indexOf("video_view_count") + 18, json_string.indexOf("video_play_count") - 2));
					} */
					
					// 게시물 타입 (items 형식에선 미출력)
					/* String type = json_object.get("__typename").toString();
					
					if ("GraphSidecar".equals(type)) {
						type = "sidecar";
					} else if ("GraphImage".equals(type)) {
						type = "image";
					} else if ("GraphVideo".equals(type)) {
						type = "video";
					} else {
						type = "unknown";
					} */
					
					String type = "sidecar";
					
					// 정보 취합
					result_map.put("userName", userName);
					result_map.put("commentCnt", commentCnt);
					result_map.put("postId", postId);
					result_map.put("contents", contents);
					result_map.put("likeCnt", likeCnt);
					result_map.put("postedAt", postedAt);
					result_map.put("thumbImg", thumbImg);
					result_map.put("shortCode", shortCode);
					result_map.put("result", true);
					result_map.put("userNum", userNum);
					result_map.put("videoViewCnt", videoViewCnt);
					result_map.put("url", url);
					result_map.put("type", type);
				}
			} catch (Exception e) {
				log.error("오류 발생!: " + url + " 크롤링 중 오류 발생");
				e.printStackTrace();
				
				if (driver != null) {
					driver.quit();
				}
				
				result_map.put("proxy", profile_entity.getProxy());
				result_map.put("account", profile_entity.getId());
				result_map.put("url", url);
				result_map.put("result", false);
				result_map.put("message", "크롤링 중 오류 발생");
				
				return result_map;
			} finally {
				if (driver != null) {
					driver.quit();
				}
			}
			
			stop_watch.stop();
			log.info("--- 게시물 크롤링 완료(" + stop_watch.getTotalTimeSeconds() + "초) ---");
		}
		
        return result_map;
	}
	
	/* 인스타그램 계정 정보 크롤링 */
	@Override
	public Map<String, Object> getInstagramCrawlingAccount(String username) {
		log.info("--- 인스타그램 계정 크롤링 API 호출 ---");
		
		StopWatch stop_watch = new StopWatch();
		stop_watch.start();
		
		Map<String, Object> result_map = new LinkedHashMap<>();
		
		// 계정 정보 가져오기
		Random random = new Random();
		List<AccountEntity> account_list = accountRepository.findByBlock(2);
		AccountEntity profile_entity = account_list.get(random.nextInt(account_list.size()));
		
		// 드라이버 설정
		ChromeDriver driver = new ChromeDriver(SnsCrawlingUtil.setChromeDriver(profile_entity, false));
		
		// 입력값 검증
		if (username == null || "".equals(username)) {
			if (driver != null) {
				driver.quit();
			}
			
			result_map.put("result", false);
			result_map.put("message", "계정명 미입력");
			
			return result_map;
		} else {
			try {
				username = username.replaceAll(" ", "");
				
				// 계정명 검사
				if (username.indexOf("/reel/") >= 0 || username.indexOf("/p/") >= 0) {
					if (driver != null) {
						driver.quit();
					}
					
					result_map.put("result", false);
					result_map.put("message", "계정명 이상");
					
					return result_map;
				}
				
				// JSON 정보로 계정 정보 크롤링 시작
				driver.get("https://www.instagram.com/" + username + "/?__a=1");
				String json_string = driver.getPageSource();
				
				json_string = json_string.replace("<html><head></head><body><pre style=\"word-wrap: break-word; white-space: pre-wrap;\">", "");
				json_string = json_string.replace("</pre></body></html>", "");
				JSONParser parser = new JSONParser();
				JSONObject json_object = new JSONObject();
				
				try {
					json_object = (JSONObject) parser.parse(json_string);
					json_object = (JSONObject) json_object.get("graphql");
					json_object = (JSONObject) json_object.get("user");
				} catch (Exception e) {
					log.error("오류 발생!: " + username + " 계정 없음");
					
					if (driver != null) {
						driver.quit();
					}
					
					result_map.put("proxy", profile_entity.getProxy());
					result_map.put("account", profile_entity.getId());
					result_map.put("username", username);
					result_map.put("result", false);
					result_map.put("message", "계정 없음");
					
					return result_map;
				}
				
				Thread.sleep(4000);
				
				// 계정 고유 번호
				String identifier = "";
				
				try {
					identifier = json_object.get("id").toString();
				} catch (Exception e) {}
				
				// 사용자명
				String full_name = "";
				
				try {
					full_name = json_object.get("full_name").toString();
				} catch (Exception e) {}
				
				// 소개글
				String biography = "";
				
				try {
					biography = json_object.get("biography").toString();
					biography = biography.replaceAll("&amp;", "&");
				} catch (Exception e) {}
				
				// 게시물 개수
				JSONObject json_object_media_count = (JSONObject) json_object.get("edge_owner_to_timeline_media");
				int media_count = Integer.parseInt(json_object_media_count.get("count").toString());
				
				// 팔로워 수
				JSONObject json_object_followed_by_count = (JSONObject) json_object.get("edge_followed_by");
				int followed_by_count = Integer.parseInt(json_object_followed_by_count.get("count").toString());
				
				// 팔로우 수
				JSONObject json_object_edge_follow = (JSONObject) json_object.get("edge_follow");
				int follows_count = Integer.parseInt(json_object_edge_follow.get("count").toString());
				
				// 프로필 사진 URL
				String profile_pic_url = json_object.get("profile_pic_url").toString();
				profile_pic_url = profile_pic_url.replaceAll("&amp;", "&");
				
				// 계정 정보
				Map<String, Object> result_map_account = new LinkedHashMap<String, Object>();
				result_map_account.put("identifier", identifier);
				result_map_account.put("username", username);
				result_map_account.put("full_name", full_name);
				result_map_account.put("biography", biography);
				result_map_account.put("media_count", media_count);
				result_map_account.put("followed_by_count", followed_by_count);
				result_map_account.put("follows_count", follows_count);
				result_map_account.put("profile_pic_url", profile_pic_url);
				
				result_map.put("result", true);
				result_map.put("account", result_map_account);
			} catch (Exception e) {
				log.error("오류 발생!: " + username + " 계정 크롤링 중 오류 발생");
				e.printStackTrace();
				
				if (driver != null) {
					driver.quit();
				}
				
				result_map.put("proxy", profile_entity.getProxy());
				result_map.put("account", profile_entity.getId());
				result_map.put("username", username);
				result_map.put("result", false);
				result_map.put("message", " 계정 크롤링 중 오류 발생");
				
				return result_map;
			} finally {
				if (driver != null) {
					driver.quit();
				}
			}
			
			stop_watch.stop();
			log.info("--- 계정 크롤링 완료(" + stop_watch.getTotalTimeSeconds() + "초) ---");
		}
		
        return result_map;
	}

	/* 인스타그램 목록 스크린샷 */
	@Override
	public Map<String, Object> getInstagramListScreenshot(String query_string, String url) {
		log.info("--- 인스타그램 목록 스크린샷 호출 ---");
		
		StopWatch stop_watch = new StopWatch();
		stop_watch.start();
		
		Map<String, Object> result_map = new HashMap<String, Object>();
		result_map.put("result", false);
		
		// 계정 정보 가져오기
		Random random = new Random();
		List<AccountEntity> account_list = accountRepository.findByBlock(2);
		AccountEntity profile_entity = account_list.get(random.nextInt(account_list.size()));
		
		// 드라이버 설정
		ChromeDriver driver = new ChromeDriver(SnsCrawlingUtil.setChromeDriver(profile_entity, false));
		
		// 입력값 검증
		if ("".equals(SnsCrawlingUtil.chkNull(query_string)) || "".equals(SnsCrawlingUtil.chkNull(url))) {
			log.error("오류 발생: 매개변수 미입력");
			
			if (driver != null) {
				driver.quit();
			}
			
			result_map.put("message", "매개변수 미입력");
			
			return result_map;
		}
		
		String url_query_search = "https://www.instagram.com/explore/tags/" + query_string.replaceAll(" ", "") + "/";
		
		try {
			// 검색어로 검색 시작
			driver.get(url_query_search);
			WebDriverWait wait = new WebDriverWait(driver, 30);
			
			try {
				wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("article.KC1QD")));
			} catch (Exception e) {
				log.error("오류 발생: 검색어 검색 결과 없음");
				
				if (driver != null) {
					driver.quit();
				}
				
				result_map.put("query_string", query_string);
				result_map.put("message", "검색어 검색 결과 없음");
				
				return result_map;
			}
			
			// 인기 게시물 URL과 비교
			boolean is_find = false;
			List<WebElement> url_elements = new ArrayList<WebElement>();
			url_elements = driver.findElements(By.cssSelector("div.Nnq7C > div.v1Nh3 > a"));
			int url_elements_size = url_elements.size();
			
			for (int i=0; i<url_elements_size; i++) {
				if (url_elements.get(i).getAttribute("href").indexOf(url) > -1) {
					// 행렬 계산
					int count_number = i + 1;
					double division_number = (i / 3.0);
					int row_number = 1;
					int column_number = 1;
					
					if (division_number > 1 && division_number <= 2) {
						row_number = 2;
					} else if (division_number > 2 && division_number <= 3) {
						row_number = 3;
					} else if (division_number > 3) {
						break;
					}
					
					column_number = count_number - (row_number - 1) * 3;
					
					// 해당 포스트 강조 표시
					JavascriptExecutor je = (JavascriptExecutor) driver;
					je.executeScript("document.querySelector('div.Nnq7C:nth-child(" + row_number + ") > div.v1Nh3:nth-child(" + column_number + ") > a > div.eLAPa').style.border = '10px solid red'", "");
					je.executeScript("window.scrollBy(0, 220)", "");
					
					// 스크린샷 찍기
					File file_screenshot = driver.getScreenshotAs(OutputType.FILE);
					Path originfile = Paths.get(file_screenshot.getAbsolutePath());
					Path newFile = Paths.get("/home/lunchbus/image/" + file_screenshot.getName());
					Files.move(originfile, newFile);
					
					result_map.put("file_path", newFile.toString());
					result_map.put("result", true);
					is_find = true;
					
					break;
				}
			}
			
			if (!is_find) {
				log.error("검색 결과: 인기 게시물에 해당 포스트 없음");
				
				result_map.put("query_string", query_string);
				result_map.put("url", url);
				result_map.put("message", "인기 게시물에 해당 포스트 없음");
			}
		} catch (Exception e) {
			log.error("오류 발생: 스크린샷 처리 중 오류 발생");
			e.printStackTrace();
			
			result_map.put("query_string", query_string);
			result_map.put("message", "스크린샷 처리 중 오류 발생");
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
		
		stop_watch.stop();
		log.info("--- 인스타그램 목록 스크린샷 완료(" + stop_watch.getTotalTimeSeconds() + "초) ---");
		
		return result_map;
	}

	/* 인스타그램 활동 점수 - 계정 */
	@Override
	public Map<String, Object> getInstagramScoringAccount(String url) {
		log.info("--- 인스타그램 계정 활동 점수 호출: " + url + " ---");
		
		StopWatch stop_watch = new StopWatch();
		stop_watch.start();
		
		Map<String, Object> result_map = new HashMap<String, Object>();
		
		// 인스타그램 계정 정보 가져오기
		Random random = new Random();
		List<AccountEntity> account_list = accountRepository.findByBlock(2);
		AccountEntity profile_entity = account_list.get(random.nextInt(account_list.size()));
		
		// 드라이버 설정
		ChromeDriver driver = new ChromeDriver(SnsCrawlingUtil.setChromeDriver(profile_entity, false));
		
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
		
		if (!"/".equals(url.substring(url.length() - 1))) {
			url += "/";
		}
		
		try {
			// 크롤링 시작
			driver.get(url + "?__a=1");
			String json_string = driver.getPageSource();
			
			json_string = json_string.replace("<html><head></head><body><pre style=\"word-wrap: break-word; white-space: pre-wrap;\">", "");
			json_string = json_string.replace("</pre></body></html>", "");
			
			// JSON 형식에 따른 파싱 분류
			if (json_string.indexOf("graphql") > -1) {
				// 'graphql' 형식
				JSONParser parser = new JSONParser();
				JSONObject json_object = new JSONObject();
				
				try {
					json_object = (JSONObject) parser.parse(json_string);
					json_object = (JSONObject) json_object.get("graphql");
					json_object = (JSONObject) json_object.get("user");
				} catch (Exception e) {
					log.error("오류 발생!: " + url + " 정보 찾을 수 없음");
					
					if (driver != null) {
						driver.quit();
					}
					
					result_map.put("proxy", profile_entity.getProxy());
					result_map.put("account", profile_entity.getId());
					result_map.put("url", url);
					result_map.put("result", false);
					result_map.put("message", "정보 찾을 수 없음");
					
					return result_map;
				}
				
				Thread.sleep(4000);
				
				// 사용자명
				String username = json_object.get("username").toString();
				
				// 프로필 사진 URL
				String profile_pic_url = json_object.get("profile_pic_url").toString();
				profile_pic_url = profile_pic_url.replaceAll("&amp;", "&");
				
				// 팔로워 수
				JSONObject json_object_followed_by_count = (JSONObject) json_object.get("edge_followed_by");
				int followed_by_count = Integer.parseInt(json_object_followed_by_count.get("count").toString());
				
				// 전체 정보 취합
				Map<String, Object> counts_map = new HashMap<>();
				counts_map.put("followed_by", followed_by_count);
				
				Map<String, Object> data_map = new HashMap<>();
				data_map.put("username", username);
				data_map.put("profile_picture", profile_pic_url);
				data_map.put("counts", counts_map);
				
				result_map.put("result", true);
				result_map.put("data", data_map);
			} else {
				// 'items' 형식
				JSONParser parser = new JSONParser();
				JSONObject json_object = (JSONObject) parser.parse(json_string);
				
				try {
					JSONArray json_array_items = (JSONArray) json_object.get("items");
					json_object = (JSONObject) json_array_items.get(0);
				} catch (Exception e) {
					log.error("오류 발생!: " + url + " 정보 찾을 수 없음");
					
					if (driver != null) {
						driver.quit();
					}
					
					result_map.put("proxy", profile_entity.getProxy());
					result_map.put("account", profile_entity.getId());
					result_map.put("url", url);
					result_map.put("result", false);
					result_map.put("message", "정보 찾을 수 없음");
					
					return result_map;
				}
				
				Thread.sleep(4000);
				
				// 작성자 이름
				JSONObject json_object_user = (JSONObject) json_object.get("user");
				String username = json_object_user.get("username").toString();
				
				// 사용자명으로 추출 작업 진행
				driver.get("https://www.instagram.com/" + username + "/?__a=1");
				json_string = driver.getPageSource();
				
				json_string = json_string.replace("<html><head></head><body><pre style=\"word-wrap: break-word; white-space: pre-wrap;\">", "");
				json_string = json_string.replace("</pre></body></html>", "");
				
				try {
					json_object = (JSONObject) parser.parse(json_string);
					json_object = (JSONObject) json_object.get("graphql");
					json_object = (JSONObject) json_object.get("user");
				} catch (Exception e) {
					log.error("오류 발생!: " + username + " 계정 없음");
					
					if (driver != null) {
						driver.quit();
					}
					
					result_map.put("proxy", profile_entity.getProxy());
					result_map.put("account", profile_entity.getId());
					result_map.put("username", username);
					result_map.put("result", false);
					result_map.put("message", "계정 없음");
					
					return result_map;
				}
				
				// 프로필 사진 URL
				String profile_pic_url = json_object.get("profile_pic_url").toString();
				profile_pic_url = profile_pic_url.replaceAll("&amp;", "&");
				
				// 팔로워 수
				JSONObject json_object_followed_by_count = (JSONObject) json_object.get("edge_followed_by");
				int followed_by_count = Integer.parseInt(json_object_followed_by_count.get("count").toString());
				
				// 전체 정보 취합
				Map<String, Object> counts_map = new HashMap<>();
				counts_map.put("followed_by", followed_by_count);
				
				Map<String, Object> data_map = new HashMap<>();
				data_map.put("username", username);
				data_map.put("profile_picture", profile_pic_url);
				data_map.put("counts", counts_map);
				
				result_map.put("result", true);
				result_map.put("data", data_map);
			}
		} catch (Exception e) {
			log.error("오류 발생: 크롤링 중 오류 발생");
			e.printStackTrace();
			
			if (driver != null) {
				driver.quit();
			}
			
			result_map.put("result", false);
			result_map.put("url", url);
			result_map.put("message", "크롤링 중 오류 발생");
			
			return result_map;
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
		
		stop_watch.stop();
		log.info("--- 인스타그램 계정 활동 점수 처리 완료(" + stop_watch.getTotalTimeSeconds() + "초) ---");
		
		return result_map;
	}
	
	/* 인스타그램 활동 점수 - 게시물 */
	@Override
	public Map<String, Object> getInstagramScoringMedia(String url) {
		log.info("--- 인스타그램 게시물 활동 점수 호출: " + url + " ---");
		
		StopWatch stop_watch = new StopWatch();
		stop_watch.start();
		
		Map<String, Object> result_map = new HashMap<String, Object>();
		
		// 인스타그램 계정 정보 가져오기
		Random random = new Random();
		List<AccountEntity> account_list = accountRepository.findByBlock(2);
		AccountEntity profile_entity = account_list.get(random.nextInt(account_list.size()));
		
		// 드라이버 설정
		ChromeDriver driver = new ChromeDriver(SnsCrawlingUtil.setChromeDriver(profile_entity, false));
		
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
		
		if (!"/".equals(url.substring(url.length() - 1))) {
			url += "/";
		}
		
		try {
			// 크롤링 시작
			driver.get(url + "?__a=1");
			String json_string = driver.getPageSource();
			
			json_string = json_string.replace("<html><head></head><body><pre style=\"word-wrap: break-word; white-space: pre-wrap;\">", "");
			json_string = json_string.replace("</pre></body></html>", "");
			
			// JSON 형식에 따른 파싱 분류
			if (json_string.indexOf("graphql") > -1) {
				// 'graphql' 형식
				JSONParser parser = new JSONParser();
				JSONObject json_object = new JSONObject();
				
				try {
					json_object = (JSONObject) parser.parse(json_string);
					json_object = (JSONObject) json_object.get("graphql");
					json_object = (JSONObject) json_object.get("user");
				} catch (Exception e) {
					log.error("오류 발생!: " + url + " 정보 찾을 수 없음");
					
					if (driver != null) {
						driver.quit();
					}
					
					result_map.put("proxy", profile_entity.getProxy());
					result_map.put("account", profile_entity.getId());
					result_map.put("url", url);
					result_map.put("result", false);
					result_map.put("message", "정보 찾을 수 없음");
					
					return result_map;
				}
				
				Thread.sleep(4000);
				
				// 활동 점수 처리
				JSONObject json_object_owner_to_timeline_media = (JSONObject) json_object.get("edge_owner_to_timeline_media");
				JSONArray json_array_owner_to_timeline_media_edges = (JSONArray) json_object_owner_to_timeline_media.get("edges");
				JSONObject json_object_node = new JSONObject();
				List<Map<String, Object>> data_map_list = new ArrayList<>();
				
				for (int i=0; i<json_array_owner_to_timeline_media_edges.size(); i++) {
					json_object_node = (JSONObject) json_array_owner_to_timeline_media_edges.get(i);
					json_object_node = (JSONObject) json_object_node.get("node");
					
					// 작성일
					String created_time = json_object_node.get("taken_at_timestamp").toString();
					
					// 댓글 수
					JSONObject json_object_count = (JSONObject) json_object_node.get("edge_media_to_comment");
					int count_comments = Integer.parseInt(json_object_count.get("count").toString());
					
					// 좋아요 수
					json_object_count = (JSONObject) json_object_node.get("edge_liked_by");
					int count_likes = Integer.parseInt(json_object_count.get("count").toString());
					
					// data 정보 취합
					Map<String, Object> comments_map = new HashMap<>();
					comments_map.put("count", count_comments);
					
					Map<String, Object> likes_map = new HashMap<>();
					likes_map.put("count", count_likes);
					
					Map<String, Object> data_map = new HashMap<>();
					data_map.put("created_time", created_time);
					data_map.put("comments", comments_map);
					data_map.put("likes", likes_map);
					
					data_map_list.add(data_map);
				}
				
				// 전체 정보 취합
				result_map.put("result", true);
				result_map.put("data", data_map_list);
			} else {
				// 'items' 형식
				JSONParser parser = new JSONParser();
				JSONObject json_object = (JSONObject) parser.parse(json_string);
				
				try {
					JSONArray json_array_items = (JSONArray) json_object.get("items");
					json_object = (JSONObject) json_array_items.get(0);
				} catch (Exception e) {
					log.error("오류 발생!: " + url + " 정보 찾을 수 없음");
					
					if (driver != null) {
						driver.quit();
					}
					
					result_map.put("proxy", profile_entity.getProxy());
					result_map.put("account", profile_entity.getId());
					result_map.put("url", url);
					result_map.put("result", false);
					result_map.put("message", "정보 찾을 수 없음");
					
					return result_map;
				}
				
				Thread.sleep(4000);
				
				// 사용자명
				JSONObject json_object_user = (JSONObject) json_object.get("user");
				String username = json_object_user.get("username").toString();
				
				// 사용자명으로 추출 작업 진행
				driver.get("https://www.instagram.com/" + username + "/?__a=1");
				json_string = driver.getPageSource();
				
				json_string = json_string.replace("<html><head></head><body><pre style=\"word-wrap: break-word; white-space: pre-wrap;\">", "");
				json_string = json_string.replace("</pre></body></html>", "");
				
				try {
					json_object = (JSONObject) parser.parse(json_string);
					json_object = (JSONObject) json_object.get("graphql");
					json_object = (JSONObject) json_object.get("user");
				} catch (Exception e) {
					log.error("오류 발생!: " + username + " 계정 없음");
					
					if (driver != null) {
						driver.quit();
					}
					
					result_map.put("proxy", profile_entity.getProxy());
					result_map.put("account", profile_entity.getId());
					result_map.put("username", username);
					result_map.put("result", false);
					result_map.put("message", "계정 없음");
					
					return result_map;
				}
				
				// 활동 점수 처리
				JSONObject json_object_owner_to_timeline_media = (JSONObject) json_object.get("edge_owner_to_timeline_media");
				JSONArray json_array_owner_to_timeline_media_edges = (JSONArray) json_object_owner_to_timeline_media.get("edges");
				JSONObject json_object_node = new JSONObject();
				List<Map<String, Object>> data_map_list = new ArrayList<>();
				
				for (int i=0; i<json_array_owner_to_timeline_media_edges.size(); i++) {
					json_object_node = (JSONObject) json_array_owner_to_timeline_media_edges.get(i);
					json_object_node = (JSONObject) json_object_node.get("node");
					
					// 작성일
					String created_time = json_object_node.get("taken_at_timestamp").toString();
					
					// 댓글 수
					JSONObject json_object_count = (JSONObject) json_object_node.get("edge_media_to_comment");
					int count_comments = Integer.parseInt(json_object_count.get("count").toString());
					
					// 좋아요 수
					json_object_count = (JSONObject) json_object_node.get("edge_liked_by");
					int count_likes = Integer.parseInt(json_object_count.get("count").toString());
					
					// data 정보 취합
					Map<String, Object> comments_map = new HashMap<>();
					comments_map.put("count", count_comments);
					
					Map<String, Object> likes_map = new HashMap<>();
					likes_map.put("count", count_likes);
					
					Map<String, Object> data_map = new HashMap<>();
					data_map.put("created_time", created_time);
					data_map.put("comments", comments_map);
					data_map.put("likes", likes_map);
					
					data_map_list.add(data_map);
				}
				
				// 전체 정보 취합
				result_map.put("result", true);
				result_map.put("data", data_map_list);
			}
		} catch (Exception e) {
			log.error("오류 발생: 크롤링 중 오류 발생");
			e.printStackTrace();
			
			if (driver != null) {
				driver.quit();
			}
			
			result_map.put("result", false);
			result_map.put("url", url);
			result_map.put("message", "크롤링 중 오류 발생");
			
			return result_map;
		} finally {
			if (driver != null) {
				driver.quit();
			}
		}
		
		stop_watch.stop();
		log.info("--- 인스타그램 게시물 활동 점수 처리 완료(" + stop_watch.getTotalTimeSeconds() + "초) ---");
		
		return result_map;
	}
	
}