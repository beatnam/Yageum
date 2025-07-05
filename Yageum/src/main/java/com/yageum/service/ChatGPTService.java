package com.yageum.service;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ChatGPTService {
	
	private final ChatGPTClient chatGPTClient; 

	public String askChatGpt(String prompt) {
		System.out.println("ChatGPTService askChatGpt()");
		
		String result = chatGPTClient.askChatGpt(prompt);
		
		
//		{
//			  "id": "chatcmpl-BVACuI2PJDUVJ1Cz3P2u4FVds3z6D",
//			  "object": "chat.completion",
//			  "created": 1746767104,
//			  "model": "gpt-4o-mini-2024-07-18",
//			  "choices": [
//			    {
//			      "index": 0,
//			      "message": {
//			        "role": "assistant",
//			        "content": "What aspect of music are you interested in? Are you looking for recommendations, information about a specific genre or artist, music theory, or something else? Let me know how I can help!",
//			        "refusal": null,
//			        "annotations": []
//			      },
//			      "logprobs": null,
//			      "finish_reason": "stop"
//			    }
//			  ],
//			  "usage": {
//			    "prompt_tokens": 50,
//			    "completion_tokens": 39,
//			    "total_tokens": 89,
//			    "prompt_tokens_details": {
//			      "cached_tokens": 0,
//			      "audio_tokens": 0
//			    },
//			    "completion_tokens_details": {
//			      "reasoning_tokens": 0,
//			      "audio_tokens": 0,
//			      "accepted_prediction_tokens": 0,
//			      "rejected_prediction_tokens": 0
//			    }
//			  },
//			  "service_tier": "default",
//			  "system_fingerprint": "fp_01d649cfa2"
//			}


		
		// 내용만 뽑아오기
		JSONObject jsonObject = new JSONObject(result);
		JSONArray jsonArray = jsonObject.getJSONArray("choices");
		JSONObject firstChoice = jsonArray.getJSONObject(0);
		JSONObject message = firstChoice.getJSONObject("message");
		String content = message.getString("content");
		
		return content;
	}

	public String askGold(String prompt) {
		System.out.println("ChatGPTService askChatGPT");

		String result = chatGPTClient.askGold(prompt);

		// 배열의 내용만 뽑아오기
		JSONObject jsonObject = new JSONObject(result);
		JSONArray jsonArray = jsonObject.getJSONArray("choices");
		JSONObject firstChoice = jsonArray.getJSONObject(0);
		JSONObject message = firstChoice.getJSONObject("message");

		String cont = message.getString("content");

		return cont;
	}
	

}
