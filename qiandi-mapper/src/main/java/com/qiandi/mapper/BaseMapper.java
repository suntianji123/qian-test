package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.qiandi.pojo.DataBase;


public interface BaseMapper extends IMapper<DataBase> {
	//数据报表信息
	List<Map<String, Object>> findAllBase(@Param("phone") String phone,@Param("createTime") String createTime,
			@Param("lastLoginTime")String lastLoginTime,Integer pageNum,Integer pageSize,@Param("id")Integer id);
  //查看合计
	List<Map<String, Object>>findCount (@Param("phone") String phone,@Param("createTime") String createTime, @Param("lastLoginTime")String lastLoginTime);
	DataBase   findByid(@Param("phone") String phone,@Param("id")Integer id);
}
