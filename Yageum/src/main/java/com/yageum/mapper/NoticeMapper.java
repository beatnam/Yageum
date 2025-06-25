package com.yageum.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yageum.domain.NoticeDTO;

@Mapper
@Repository
public interface NoticeMapper {

	//공지 게시판에 뿌려줄 리스트
	List<NoticeDTO> listNotice();
	
	//게시판에서 선택한 게시물 내용 가져오기
	Optional<NoticeDTO> findByIn(int noticeIn);

	void insert(NoticeDTO noticeDTO);
}
