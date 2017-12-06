package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.qiandi.pojo.SysCredits;

public interface SysCreditsMapper extends IMapper<SysCredits> {
	
	int failure(@Param("id") Long[] id, @Param("reviewTime") Long reviewTime,
			@Param("Auditresults") String Auditresults);// 审核失败

	int pass(@Param("id") Long[] id, @Param("reviewTime") Long reviewTime);// 审核成功

	List<SysCredits> selectAll(@Param("id") Long[] id);

	List<SysCredits> recording(SysCredits credits);// 审批记录

	int del(@Param("id") Long[] id);// 审批记录(批量删除)

	List<SysCredits> recordingAll(@Param("id") Long[] id);// 审批记录(批量查询)

	SysCredits credit(String accountName); // 授信设置

	int add(SysCredits credits);// 授信设置(确认)

	List<SysCredits> creditRecording(SysCredits credits);// 授信设置记录查询

	List<SysCredits> batchquery(@Param("id")Long[] id);// 授信设置记录批量查询
}
