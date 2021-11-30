/* document ready */
$(function() {
	// 로딩바 설정
	var loading_mask = $('<div id="div_loading"></div>').appendTo(document.body).hide();
	var loading_image = $('<img id="img_loading" alt="로딩" src="/image/loading.gif">').appendTo(document.body).hide();
	var mask_width = $(document).width();
	var mask_height = $(document).height();
	
	loading_mask.css({'width':mask_width,'height':mask_height});
	loading_image.css("top", "350px");
	loading_image.css("left", Math.max(0, (($(document).width() - loading_image.outerWidth()) / 2) + $(document).scrollLeft()) + "px");
	
	// SNS 종류 라디오 버튼 선택 시 처리
	$('input[name=sns_type]').on('change', function() {
		fnSetFormToSnsType(this);
	});
	
	// 인스타그램 하위 라디오 버튼 선택 시 처리
	$('input[name=insta_type]').on('change', function() {
		fnSetFormToInstaType(this);
	});
	
	// 설정 입력값 검증
	var forms = document.querySelectorAll('.needs-validation');

	Array.prototype.slice.call(forms).forEach(function(form) {
		$('#button_submit').on('click', function(event) {
			if (!form.checkValidity()) {
				event.preventDefault();
				event.stopPropagation();
			} else {
				if (confirm('크롤링을 시작하시겠습니까?')) {
					// 로딩바 출력
					loading_mask.fadeTo('fast', 0.4);
					loading_image.fadeTo('fast', 1);
					
					fnCrawlingStart();
				}
			}

			form.classList.add('was-validated');
		});
	});
	
	// 결과 복사 버튼 클릭 설정
	$('#button_copy').on('click', function() {
		fnResultCopy();
	});
});

/* SNS 종류에 따른 양식 설정 */
function fnSetFormToSnsType(radio_object) {
	$('#sns_url').val('');
	
	if (radio_object.value === 'insta') {
		// 인스타그램
		var insta_type = $('input[name=insta_type]:checked').val();
		$('input[name=insta_type]').attr('disabled', false);
		
		if (insta_type === 'media') {
			// 게시물 정보
			$('.label_input').html('게시물 URL <span class="text-muted">(공개 계정만 가능)</span>');
			$('#sns_url').attr('placeholder', 'https://www.instagram.com/p/CDEFGHIJKLM/');
			$('.div_result_insta_media').show('fast');
			$('.div_result_insta_account').hide('fast');
			$('.div_result_naver_media').hide('fast');
			$('.div_result_youtube_videoInfo').hide('fast');
			$('.div_input_data').text('크롤링 할 게시물의 URL를 입력해주세요.');
		} else if (insta_type === 'account') {
			// 계정 정보
			$('.label_input').text('계정명');
			$('#sns_url').attr('placeholder', 'identification');
			$('.div_result_insta_media').hide('fast');
			$('.div_result_insta_account').show('fast');
			$('.div_result_naver_media').hide('fast');
			$('.div_result_youtube_videoInfo').hide('fast');
			$('.div_input_data').text('크롤링 할 계정명을 입력해주세요.');
		}
	} else if (radio_object.value === 'naver') {
		// 네이버 블로그
		$('input[name=insta_type]').attr('disabled', true);
		$('.label_input').text('게시물 URL');
		$('#sns_url').attr('placeholder', 'https://blog.naver.com/identification/123456789012');
		$('.div_result_insta_media').hide('fast');
		$('.div_result_insta_account').hide('fast');
		$('.div_result_naver_media').show('fast');
		$('.div_result_youtube_videoInfo').hide('fast');
		$('.div_input_data').text('크롤링 할 게시물의 URL를 입력해주세요.');
	} else if (radio_object.value === 'youtube') {
		// 유튜브 동영상
		$('input[name=insta_type]').attr('disabled', true);
		$('.label_input').text('동영상 URL');
		$('#sns_url').attr('placeholder', 'https://www.youtube.com/watch?v=ABCDEFGHIJK');
		$('.div_result_insta_media').hide('fast');
		$('.div_result_insta_account').hide('fast');
		$('.div_result_naver_media').hide('fast');
		$('.div_result_youtube_videoInfo').show('fast');
		$('.div_input_data').text('크롤링 할 동영상의 URL를 입력해주세요.');
	}
}

/* 인스타그램 하위 종류에 따른 양식 설정 */
function fnSetFormToInstaType(radio_object) {
	$('#sns_url').val('');
	
	if (radio_object.value === 'media') {
		// 게시물 정보
		$('.label_input').html('게시물 URL <span class="text-muted">(공개 계정만 가능)</span>');
		$('#sns_url').attr('placeholder', 'https://www.instagram.com/p/CDEFGHIJKLM/');
		$('.div_result_insta_media').show('fast');
		$('.div_result_insta_account').hide('fast');
		$('.div_input_data').text('크롤링 할 게시물의 URL를 입력해주세요.');
	} else if (radio_object.value === 'account') {
		// 계정 정보
		$('.label_input').text('계정명');
		$('#sns_url').attr('placeholder', 'identification');
		$('.div_result_insta_media').hide('fast');
		$('.div_result_insta_account').show('fast');
		$('.div_input_data').text('크롤링 할 계정명을 입력해주세요.');
	}
}

/* 크롤링 시작 */
function fnCrawlingStart() {
	var sns_type = $('input[name=sns_type]:checked').val();
	var sns_type_detail = $('input[name=insta_type]:checked').val();
	var input_value = $('#sns_url').val();
	
	if (sns_type != 'insta') {
		sns_type_detail = 'media';
		
		if (sns_type == 'youtube') {
			sns_type_detail = 'videoInfo';
		}
	}
	
	// 크롤링 API 호출
	// var host = 'localhost:8000';
	// var host = '52.79.54.33:8000';
	// var host = 'insta.crawl2.dinnerqueen.net:8888';
	// var host = 'insta.crawl3.dinnerqueen.net:8888';
	var host = 'insta.crawl4.dinnerqueen.net:8888';
	
	if (sns_type_detail == 'account') {
		var url = 'http://' + host + '/' + sns_type + '/' + sns_type_detail + '/?username=' + input_value;
	} else {
		var url = 'http://' + host + '/' + sns_type + '/' + sns_type_detail + '/?url=' + input_value;
	}
	
	fetch(url).then(res => res.json())
	.then(response => {
		// 성공
		$('.div_result span').text('-');
		$('#textarea_result').val('');
		
		if (response.result) {
			if (sns_type == 'insta' && sns_type_detail == 'media') {
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_result').text('성공');
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_userName').text(response.userName);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_commentCnt').text(response.commentCnt);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_postId').text(response.postId);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_contents').text(response.contents);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_likeCnt').text(response.likeCnt);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_postedAt').text(response.postedAt);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_thumbImg').html('<a href="' + response.thumbImg + '" target="_blank">' + response.thumbImg + '</a>');
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_shortCode').text(response.shortCode);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_userNum').text(response.userNum);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_videoViewCnt').text(response.videoViewCnt);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_url').html('<a href="' + response.url + '" target="_blank">' + response.url + '</a>');
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_type').text(response.type);
			} else if (sns_type == 'insta' && sns_type_detail == 'account') {
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_result').text('성공');
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_identifier').text(response.account.identifier);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_username').text(response.account.username);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_full_name').text(response.account.full_name);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_biography').text(response.account.biography);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_media_count').text(response.account.media_count);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_followed_by_count').text(response.account.followed_by_count);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_follows_count').text(response.account.follows_count);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_profile_pic_url').html('<a href="' + response.account.profile_pic_url + '" target="_blank">' + response.account.profile_pic_url + '</a>');
			} else if (sns_type == 'naver') {
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_result').text('성공');
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_url').html('<a href="' + response.url + '" target="_blank">' + response.url + '</a>');
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_title').text(response.title);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_name').text(response.name);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_post_date').text(response.post_date);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_like_cnt').text(response.like_cnt);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_reply_cnt').text(response.reply_cnt);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_img_src').html('<a href="' + response.img_src + '" target="_blank">' + response.img_src + '</a>');
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_content').text(response.content);
			} else if (sns_type == 'youtube') {
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_result').text('성공');
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_ownerName').text(response.videoInfo.ownerName);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_videoTitle').text(response.videoInfo.videoTitle);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_postingDate').text(response.videoInfo.postingDate);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_viewCount').text(response.videoInfo.viewCount);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_likeCount').text(response.videoInfo.likeCount);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_commentCount').text(response.videoInfo.commentCount);
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_ownerImg').html('<a href="' + response.videoInfo.ownerImg + '" target="_blank">' + response.videoInfo.ownerImg + '</a>');
				$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_videoThumbnail').html('<a href="' + response.videoInfo.videoThumbnail + '" target="_blank">' + response.videoInfo.videoThumbnail + '</a>');
			}
		} else {
			$('.div_result_' + sns_type + '_' + sns_type_detail + ' .span_result').text(response.message);
		}
		
		$('#textarea_result').val(JSON.stringify(response));
	})
	.catch(error => {
		// 실패
		console.error(error);
		alert('처리 중 오류가 발생하였습니다.');
	})
	.finally(function() {
		// 로딩바 제거
		$('#div_loading').fadeOut('fast');
	    $('#img_loading').fadeOut('fast');
	});
}

/* 결과 복사하기 처리 */
function fnResultCopy() {
	var obj = document.getElementById('textarea_result');
	
	if (!obj.value) {
		alert('결과 값이 없습니다.');
	} else {
		obj.select(); 
		document.execCommand('copy');
		obj.setSelectionRange(0, 0);
		
		alert('복사되었습니다.');
	}
}
