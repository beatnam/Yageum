package com.yageum.service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ChatGPTClient {
	
	@Value("${gpt.API_KEY}")
	private String apiKey;
	
	private String url = "https://api.openai.com/v1/chat/completions";
	private String model = "gpt-4o-mini";
	private double temperature = 0.5;
	
	public String askChatGpt(String prompt) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Authorization", "Bearer " + apiKey);
		
		Map<String, String> roleSystem = new HashMap<String, String>();
		roleSystem.put("role", "system");
		roleSystem.put("content", " 너는 소비분석사야 "); // 문법 수정
		
		Map<String, String> roleUser = new HashMap<String, String>();
		roleUser.put("role", "user");
		roleUser.put("content", prompt);
		
		List<Map<String, String>> messages = new ArrayList<Map<String,String>>();
		messages.add(roleSystem);
		messages.add(roleUser);
		
		JSONObject requestData = new JSONObject();
		requestData.put("model", model);
		requestData.put("temperature", temperature);
		requestData.put("messages", messages);
		
		HttpEntity<String> httpEntity 
		    = new HttpEntity<String>(requestData.toString(),headers);
		System.out.println("HttppEntity : " + httpEntity);
		
		RestTemplate restTemplate = new RestTemplate();
		
		//UTF-8 요청
		List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
		messageConverters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
		messageConverters.addAll(restTemplate.getMessageConverters());
		restTemplate.setMessageConverters(messageConverters);
		
		ResponseEntity<String> responseEntity =
		restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
		
		System.out.println("responseEntity" + responseEntity);
		
		String responseBody = responseEntity.getBody();
		System.out.println("responseEntity.getBody() : " + responseBody);
		
		// ⭐ JSON 응답에서 content 필드 추출 ⭐
		try {
		    JSONObject jsonResponse = new JSONObject(responseBody);
		    JSONArray choices = jsonResponse.getJSONArray("choices");
		    if (choices != null && choices.length() > 0) {
		        JSONObject firstChoice = choices.getJSONObject(0);
		        JSONObject message = firstChoice.getJSONObject("message");
		        String content = message.getString("content");
		        return content; // 추출된 텍스트만 반환
		    }
		} catch (Exception e) {
		    System.err.println("ChatGPT 응답 파싱 중 오류 발생: " + e.getMessage());
		    // 오류 발생 시 원본 JSON 또는 오류 메시지 반환
		    return "ChatGPT 응답 파싱 오류: " + responseBody;
		}
		
		return "ChatGPT 응답을 찾을 수 없습니다."; // content를 찾지 못한 경우
	}
}