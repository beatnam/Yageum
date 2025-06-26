package com.yageum.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.yageum.domain.ItemDTO;
import com.yageum.mapper.ItemMapper;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;

@Transactional
@Service
@Log
@RequiredArgsConstructor
public class ItemService {

	private final ItemMapper itemMapper;
	
	
	public List<ItemDTO> findAll(){
		log.info("ItemService findAll()");
		
		return itemMapper.findAll();
	}


	public ItemDTO findByItemIn(int itemIn) {
		log.info("ItemService findByItemIn()");

		
		return itemMapper.findByItemIn(itemIn);
	}


	public void updateItem(ItemDTO itemFind) {
		log.info("ItemService updateItem()");

	
		itemMapper.updateItem(itemFind);
		
	}


	public void saveItem(ItemDTO itemDTO) {
		log.info("ItemService saveItem()");
		
		itemMapper.saveItem(itemDTO);
	}


	public void deleteItem(ItemDTO itemFind) {
		log.info("ItemService delete()");

		itemMapper.deleteItem(itemFind);
		
	}
	
	
	
	
	
	
	
	
}
