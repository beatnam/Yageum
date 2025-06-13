package com.yageum.service;

import java.time.LocalDate;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
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
	private final PasswordEncoder passwordEncoder;

	public void joinMember(MemberDTO memberDTO) {
		log.info("MemberService joinMember()");
		String encodedPassword = passwordEncoder.encode(memberDTO.getMemberPasswd());

		// 암호화된 비밀번호로 다시 세팅
		memberDTO.setMemberPasswd(encodedPassword);
		memberDTO.setMemberConsent(true);
		memberDTO.setCreateDate(LocalDate.now());
		// 이렇게 해줘도 되는지?
		memberDTO.setMemberRole("USER");
		memberDTO.setMemberState("정상");
		memberDTO.setMemberIsFirst(true);
		memberMapper.joinMember(memberDTO);

	}

	public MemberDTO loginMember(String memberId) {

		return memberMapper.loginMember(memberId);
	}

	public MemberDTO infoMember(String id) {

		return memberMapper.infoMember(id);
	}

	public void naverJoinMember(MemberDTO memberDTO) {
		memberDTO.setMemberConsent(true);
		memberDTO.setCreateDate(LocalDate.now());

		memberDTO.setMemberRole("USER");
		memberDTO.setMemberState("정상");
		memberDTO.setMemberIsFirst(true);
		memberMapper.joinMember(memberDTO);

	}

}
