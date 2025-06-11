package com.yageum.service;

import java.util.Map;

import org.springframework.stereotype.Service;

import com.yageum.domain.MemberDTO;
import com.yageum.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Service
@Log
@RequiredArgsConstructor
public class MemberService {

	private final MemberMapper memberMapper;
	
	
	public void joinMember(MemberDTO memberDTO) {
		log.info("MemberService joinMember()");
		
		memberMapper.joinMember(memberDTO);
		
	}

}
