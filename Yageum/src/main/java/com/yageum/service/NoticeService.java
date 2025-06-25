package com.yageum.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.yageum.domain.NoticeDTO;
import com.yageum.mapper.NoticeMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Transactional
@Service
@Log
@RequiredArgsConstructor
public class NoticeService {

	
	private final NoticeMapper noticeMapper;
	
	
	
	
	public List<NoticeDTO> listNotice(){
		log.info("noticeService listNotice()");
		
		return noticeMapper.listNotice();
		
	}
	
	public Optional<NoticeDTO> findByIn(int noticeIn){
		log.info("noticeService findByIn()");

		return noticeMapper.findByIn(noticeIn);
	}

	public void insert(NoticeDTO noticeDTO) {
		log.info("noticeService insert()");

		noticeMapper.insert(noticeDTO);
		
		
	}
}
