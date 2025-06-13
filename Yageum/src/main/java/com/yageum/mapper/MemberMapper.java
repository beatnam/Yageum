package com.yageum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yageum.domain.MemberDTO;

@Mapper
@Repository
public interface MemberMapper {

	public void joinMember(MemberDTO memberDTO);

	public MemberDTO infoMember(String id);

	public MemberDTO loginMember(String memberId);

}
