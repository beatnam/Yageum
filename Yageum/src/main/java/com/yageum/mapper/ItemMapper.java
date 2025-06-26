package com.yageum.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Repository;

import com.yageum.domain.ItemDTO;

@Mapper
@Repository
public interface ItemMapper {

	
	
	List<ItemDTO> findAll();

	ItemDTO findByItemIn(int itemIn);

	void updateItem(ItemDTO itemFind);

	void saveItem(ItemDTO itemDTO);

	void deleteItem(ItemDTO itemFind);
	
	
	
	
	
	
	
	
}
