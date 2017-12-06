package com.qiandi.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.qiandi.pojo.RechargeRecordLateFeeData;
import com.qiandi.service.RechargeRecordLateFeeService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;

/*
 * 滞纳金动态表
 */
@Controller
@RequestMapping("/rechargeRecordLateFee")
public class RechargeRecordLateFeeController
{

	@Autowired
	private RechargeRecordLateFeeService rechargeRecordLateFeeService;

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, Long id, Long rechargeRecordId,
			String menberUserAccountName, String qianShangUserName, Integer pageNum, Integer pageSize)
	{
		if (pageNum == null)
		{
			pageNum = 1;
		}
		if (pageSize == null)
		{
			pageSize = 2;
		}

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("beginTime", beginTime);
		param.put("endTime", endTime);
		param.put("id", id);
		param.put("rechargeRecordId", rechargeRecordId);
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);

		PageInfo<RechargeRecordLateFeeData> pageInfo = rechargeRecordLateFeeService.pageData(pageNum, pageSize, param);

		return AjaxResult.successInstance(pageInfo);
	}
}
