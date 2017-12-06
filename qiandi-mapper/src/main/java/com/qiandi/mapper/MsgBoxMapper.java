package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.qiandi.pojo.InBox;

public interface MsgBoxMapper {
	/**
	 * 显示所有收件箱信息
	 * @param menber
	 * @return list
	 */
	List<Map<String, Object>>  MsgBoxlist(@Param("phone") String  phone,@Param("box")InBox inBox,
			@Param("createTime")String createTime,@Param("lastLoginTime")String lastLoginTime,
			Integer pageNum,Integer pageSize,@Param("id")Integer id);

	/**
	 * 根据id显示收件箱相应的信息 
	 * @param id
	 * @return InBox
	 */
	InBox inBox(Integer id);

	/**
	 * 获取收件箱总数
	 * @param phoneNum
	 * @return int
	 */
	int getTotalCount(@Param("phone") String phoneNum,@Param("box")InBox inBox,
			@Param("createTime")String createTime,@Param("lastLoginTime")String lastLoginTime);
	
	/**
	 * 批量删除收件箱信息
	 * @param InBox
	 */
	void update( List<InBox> InBox);
	
	List<Map<String, Object>> getAllMsgBox();
}