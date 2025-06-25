package com.yageum.mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yageum.domain.MemberDTO;

@Mapper
@Repository
public interface MemberMapper {

	public void joinMember(MemberDTO memberDTO);

	public MemberDTO infoMember(String id);

	public MemberDTO loginMember(String memberId);

	public void updateDate(MemberDTO memberDTO2);

	public List<MemberDTO> listMemberLastLogin();

	public void updateMemberStraight(int memberIn);


}
