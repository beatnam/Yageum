package com.yageum.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yageum.domain.ExpenseDTO;
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

	public List<ExpenseDTO> listMemberLastExpense();

	public void updateMemberExpense(int memberIn);

	public void updateMemberStraightZero(int memberIn);

	public void updateMemberExpenseZero(int memberIn);

	public MemberDTO infoMember2(String email);

	public MemberDTO infoMember3(String phone);
	

}
