package com.yageum.config;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.yageum.domain.MemberDTO;
import com.yageum.mapper.MemberMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MyUserDetailsService implements UserDetailsService {

	private final MemberMapper memberMapper;

	@Override
	public UserDetails loadUserByUsername(String id) throws UsernameNotFoundException {
		// TODO Auto-generated method stub
		MemberDTO memberDTO = memberMapper.infoMember(id);
		if (memberDTO == null) {
			throw new UsernameNotFoundException("없는 회원");
		}

		return User.builder().username(memberDTO.getMemberId()).password(memberDTO.getMemberPasswd())
				.roles(memberDTO.getMemberRole()).build();
	}

}
