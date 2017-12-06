package com.qiandi.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.qiandi.pojo.RepaymentAuditData;
import com.qiandi.service.RepaymentService;
import com.qiandi.util.AjaxResult;

@Controller
@RequestMapping("/repayment")
public class RepaymentController
{
	@Autowired
	private RepaymentService repaymentService;

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Integer beginTime, Integer endTime, Integer pageNum, Integer pageSize)
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

		PageInfo<RepaymentAuditData> pageInfo = repaymentService.pageAuditData(pageNum, pageSize, param);

		return AjaxResult.successInstance(pageInfo);

	}

}
