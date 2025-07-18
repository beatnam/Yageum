package com.yageum.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.yageum.domain.ExpenseDTO;
import com.yageum.domain.MemberDTO;
import com.yageum.entity.CategoryMain;
import com.yageum.entity.Member;
import com.yageum.mapper.MemberMapper;
import com.yageum.repository.MemberRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Transactional
@Service
@Log
@RequiredArgsConstructor
public class MemberService {

	private final MemberMapper memberMapper;
	private final PasswordEncoder passwordEncoder;
	private final MemberRepository memberRepository;

	public void joinMember(MemberDTO memberDTO) {
		log.info("MemberService joinMember()");
		String encodedPassword = passwordEncoder.encode(memberDTO.getMemberPasswd());

		// 암호화된 비밀번호로 다시 세팅
		memberDTO.setMemberPasswd(encodedPassword);
		memberDTO.setMemberConsent(true);
		memberDTO.setCreateDate(LocalDate.now());
		memberDTO.setMemberRole("USER");
		memberDTO.setMemberState("정상");
		memberDTO.setMemberIsFirst(true);
		memberDTO.setMemberReward(0);
		memberDTO.setMemberStraight(0);
		memberDTO.setMemberExpense(0);
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
		memberDTO.setMemberReward(0);
		memberDTO.setMemberStraight(0);
		memberDTO.setMemberExpense(0);
		memberMapper.joinMember(memberDTO);
	}

	// 관리자 - 유저 페이지 회원 정보 출력 시작

	public List<Member> adminInfo() {

		return memberRepository.findAll();
	}

	// 관리자 - 유저 페이지 회원 정보 출력 끝

	// 마이페이지 - 회원정보 수정 저장 로직 시작
	public Optional<Member> findByMemberId(String memberId) {
		log.info("MemberService findByMemberId()");

		return Optional.ofNullable(memberRepository.findByMemberId(memberId));

	}

	public void save(Member member) {
		log.info("MemberService save()");

		memberRepository.save(member);
	}

	public void deleteByMemberId(String id) {
		log.info("MemberService deleteByMemberId()");

		memberRepository.deleteByMemberId(id);

	}

	// 마이페이지 - 회원정보 수정 저장 로직 끝


	//마이페이지 - 회원정보 수정 저장 로직 끝


	
	//관리자 페이지 사용자 권한 부여---------------------------

	public void update(MemberDTO memberDTO) {
		log.info("MemberService update()");

		Member member = new Member();
		member.setMemberEntity(memberDTO);

		memberRepository.save(member);

	}

	public Member find(String memberId) {
		return memberRepository.findByMemberId(memberId);
	}

	public void updateDate(MemberDTO memberDTO2) {
		memberMapper.updateDate(memberDTO2);

	}


	public List<MemberDTO> listMemberLastLogin() {
		// TODO Auto-generated method stub
		return memberMapper.listMemberLastLogin();
	}

	public void updateMemberStraight(int memberIn) {
		memberMapper.updateMemberStraight(memberIn);
	}

	public List<ExpenseDTO> listMemberLastExpense() {

		return memberMapper.listMemberLastExpense();
	}

	public void updateMemberExpense(int memberIn) {
		memberMapper.updateMemberExpense(memberIn);
	}

	public void updateMemberStraightZero(int memberIn) {
		memberMapper.updateMemberStraightZero(memberIn);

	}

	public void updateMemberExpenseZero(int memberIn) {
		memberMapper.updateMemberExpenseZero(memberIn);
	}

	public MemberDTO infoMember2(String email) {
		// TODO Auto-generated method stub
		return memberMapper.infoMember2(email);
	}

	public MemberDTO infoMember3(String phone) {
		// TODO Auto-generated method stub
		return memberMapper.infoMember3(phone);
	}

	// 관리자 페이지 사용자 권한 부여----------------------------

}
