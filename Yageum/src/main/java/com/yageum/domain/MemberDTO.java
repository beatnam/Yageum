package com.yageum.domain;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class MemberDTO {

	private int memberIn;
	private String memberRole;
	private String memberId;
	private String memberPasswd;
	private String memberRepasswd;
	private String memberName;
	private LocalDate memberBirth;
	private String memberEmail;
	private String memberGender;
	private String memberPhone;
	private String memberAddress;
	private LocalDate createDate;
	private LocalDate lastLoginDate;
	private boolean memberConsent;
	private boolean emailConsent;
	private String memberState;
	private int memberReward;
	private boolean memberIsFirst;
	private int memberStraight;

	//비밀번호 변경용
	private String newPassword;

}
