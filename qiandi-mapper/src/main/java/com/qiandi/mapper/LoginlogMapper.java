package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.qiandi.pojo.LoginLog;
import com.qiandi.pojo.MenberUser;

public interface LoginlogMapper extends IMapper<LoginLog>{
	/**
	 * 显示所有收件箱信息
	 * 
	 * @param menber
	 * @return list
	 */
	List<Map<String, Object>> loginlogs(@Param("us") MenberUser menber, @Param("id") Long id,@Param("log") LoginLog log,
			@Param("createTime") String createTime, @Param("lastLoginTime") String lastLoginTime, Integer pageNum,
			Integer pageSize);

	/**
	 * 查看详细信息
	 * @param id
	 */
	LoginLog findByid(Long id);
	/**
	 * 添加日志
	 * @param id
	 */
	int add(LoginLog log);
	
	Long findDateTime();
	
	
	//导出
	List<Map<String, Object>> loginInfoExport(Map<String,Object> export);
}
