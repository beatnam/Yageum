package com.yageum.entity;

import java.sql.Date;
import java.time.LocalDate;

import com.yageum.domain.MemberDTO;
import com.yageum.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "member")
@Getter
@Setter
@ToString
public class Member {

	@Id
	@Column(name = "member_in")
	private int memberIn;
	
	@Column(name = "member_role")
	private String memberRole;
	
	@Column(name = "member_id")
	private String memberId;
	
	@Column(name = "member_passwd")
	private String memberPasswd;
	
	@Column(name = "member_name")
	private String memberName;
	
	@Column(name = "member_birth")
	private LocalDate memberBirth;
	
	@Column(name = "member_email")
	private String memberEmail;
	
	@Column(name = "member_gender")
	private String memberGender;
	
	@Column(name = "member_phone")
	private String memberPhone;
	
	@Column(name = "member_address")
	private String memberAddress;
	
	@Column(name = "create_date")
	private LocalDate createDate;
	
	@Column(name = "lastlogin_date")
	private LocalDate lastLoginDate;
	
	@Column(name = "member_consent")
	private Boolean memberConsent;
	
	@Column(name = "email_consent")
	private Boolean emailConsent;
	
	@Column(name = "member_state")
	private String memberState;
	
	@Column(name = "member_reward")
	private Integer memberReward;
	
	@Column(name = "member_isFirst")
	private Boolean memberIsFirst;
	
	public Member() {
	   }
	
	 public Member(int memberIn, String memberId, String memberPasswd, String memberName, String memberRole, 
			 LocalDate memberBirth, String memberEmail, String memberGender, String memberPhone, String memberAddress, 
			 LocalDate createDate, LocalDate lastLoginDate, Boolean memberConsent, Boolean emailConsent, 
			 String memberState, Integer memberReward, Boolean memberIsFirst) {
	      super();
	      this.memberIn = memberIn;
	      this.memberId = memberId;
	      this.memberPasswd = memberPasswd;
	      this.memberName = memberName;
	      this.memberRole = memberRole;
	      this.memberBirth = memberBirth;
	      this.memberEmail = memberEmail;
	      this.memberGender = memberGender;
	      this.memberPhone = memberPhone;
	      this.memberAddress = memberAddress;
	      this.createDate = createDate;
	      this.lastLoginDate = lastLoginDate;
	      this.memberConsent = memberConsent;
	      this.emailConsent = emailConsent;
	      this.memberState = memberState;
	      this.memberReward = memberReward;
	      this.memberIsFirst = memberIsFirst;
	   }
	 
	 public static Member setMemberEntity(MemberDTO memberDTO) {

	      Member member = new Member();
	      member.setMemberIn(memberDTO.getMemberIn());
	      member.setMemberId(memberDTO.getMemberId());
	      member.setMemberPasswd(memberDTO.getMemberPasswd());
	      member.setMemberName(memberDTO.getMemberName());
	      member.setMemberRole(memberDTO.getMemberRole());
	      member.setMemberBirth(memberDTO.getMemberBirth());
	      member.setMemberEmail(memberDTO.getMemberEmail());
	      member.setMemberGender(memberDTO.getMemberGender());
	      member.setMemberPhone(memberDTO.getMemberPhone());
	      member.setMemberAddress(memberDTO.getMemberAddress());
	      member.setCreateDate(memberDTO.getCreateDate());
	      member.setLastLoginDate(memberDTO.getLastLoginDate());
	      member.setMemberConsent(memberDTO.isMemberConsent());
	      member.setEmailConsent(memberDTO.isEmailConsent());
	      member.setMemberState(memberDTO.getMemberState());
	      member.setMemberReward(memberDTO.getMemberReward());
	      member.setMemberIsFirst(memberDTO.isMemberIsFirst());
	      
	      return member;
	   }
	
}
