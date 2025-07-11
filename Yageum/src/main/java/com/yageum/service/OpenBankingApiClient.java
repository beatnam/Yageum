package com.yageum.service;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@Log
@RequiredArgsConstructor
public class OpenBankingApiClient {
	
	
   @Value("${openbanking.id}")
   private String client_id;
   
   @Value("${openbanking.secret}")
   private String client_secret;

   private String redirect_uri = "http://c3d2501t1p2.itwillbs.com/openbanking/callback";
   private String grant_type = "authorization_code";
   
//   헤더 정보 관리 클래스
   private HttpHeaders httpHeaders;
   
//   REST방식 API 요청
   private RestTemplate restTemplate;
   
//   토큰발급 요청
   public Map<String, String> requestToken(Map<String, String> map) {
//      ㅇ 요청 메시지 URL
//       HTTP URL  https://testapi.openbanking.or.kr/oauth/2.0/token
//       HTTP Method   POST
//       Content-Type  application/x-www-form-urlencoded; charset=UTF-8
      httpHeaders = new HttpHeaders();
      httpHeaders.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
       
//      ㅇ 요청 메시지 명세
//      code <authorization_code> Y 사용자인증 성공 후 획득한 Authorization Code
//      client_id   <client_id>   Y  오픈뱅킹에서 발급한 이용기관 앱의 Client ID
//      client_secret  <client_secret> Y 오픈뱅킹에서 발급한 이용기관 앱의 Client Secret
//      redirect_uri   <callback_uri>  Y    Access Token 을 전달받을 Callback URL
//      grant_type  고정값: authorization_code Y  3-legged 인증을 위한 권한부여 방식 지정
      MultiValueMap<String, String> parameters
      = new LinkedMultiValueMap<String, String>();
      parameters.add("code", map.get("code").toString());
      parameters.add("client_id", client_id);
      parameters.add("client_secret", client_secret);
      parameters.add("redirect_uri", redirect_uri);
      parameters.add("grant_type", grant_type);
      
//      httpHeaders, HttpBody parameters 담아서 감 => HttpEntity
      HttpEntity<MultiValueMap<String, String>> param =
            new HttpEntity<MultiValueMap<String, String>>(parameters,httpHeaders);
      
      restTemplate = new RestTemplate();
      String requestURL = "https://testapi.openbanking.or.kr/oauth/2.0/token";
      
      return restTemplate
            .exchange(requestURL, HttpMethod.POST,param, Map.class)
            .getBody();
   }
   
//   사용자 정보조회
   public Map<String, String> getUserInfo(Map<String, String> map) {
      log.info("access_token : " + map.get("access_token"));
      log.info("user_seq_no : " + map.get("user_seq_no"));
      httpHeaders = new HttpHeaders();
      httpHeaders.add("Authorization", "Bearer " + map.get("access_token"));
      
      HttpEntity<String> userInfo = 
            new HttpEntity<String>(httpHeaders);
      //get방식
      String requestURL = "https://testapi.openbanking.or.kr/v2.0/user/me";
//      UriComponents uriComponents = UriComponentsBuilder
//            .fromHttpUrl(requestURL)
//            .queryParam("user_seq_no", map.get("user_seq_no"))
//            .build();
      
      UriComponents uriComponents = UriComponentsBuilder
            .fromUriString(requestURL)//문자열 형태 URI
            .queryParam("user_seq_no", map.get("user_seq_no"))
            .build(true);//URI 인코딩 포함
      
      restTemplate = new RestTemplate();
      
      Map<String, String> map2 = restTemplate
            .exchange(uriComponents.toString(), HttpMethod.GET,userInfo,Map.class)
            .getBody();
      log.info("사용자 정보조회 map2 : " + map2.toString());
      return map2;
   }
   
//	등록계좌 조회
	public Map<String, Object> accountList(Map<String, String> map) {
		log.info("access_token : " + map.get("access_token"));
		log.info("user_seq_no : " + map.get("user_seq_no"));
		httpHeaders = new HttpHeaders();
		httpHeaders.add("Authorization", "Bearer " + map.get("access_token"));
		
		HttpEntity<String> accountList = 
				new HttpEntity<String>(httpHeaders);
		//get방식
		String requestURL = "https://testapi.openbanking.or.kr/v2.0/account/list";
//		UriComponents uriComponents = UriComponentsBuilder
//				.fromHttpUrl(requestURL)
//				.queryParam("user_seq_no", map.get("user_seq_no"))
//				.build();
		
		UriComponents uriComponents = UriComponentsBuilder
				.fromUriString(requestURL)//문자열 형태 URI
				.queryParam("user_seq_no", map.get("user_seq_no"))
				.queryParam("include_cancel_yn", map.get("include_cancel_yn"))
				.queryParam("sort_order", map.get("sort_order"))
				.build(true);//URI 인코딩 포함
		
		restTemplate = new RestTemplate();
		
		Map<String, Object> result = restTemplate
		        .exchange(uriComponents.toString(), HttpMethod.GET, accountList, Map.class)
		        .getBody();
		log.info("//등록계좌 조회 result : " + result.toString());
		return result;
	}


}
