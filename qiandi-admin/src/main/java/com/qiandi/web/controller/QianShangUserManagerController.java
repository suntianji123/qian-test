package com.qiandi.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.qiandi.pojo.QianShangUserManagerData;
import com.qiandi.service.QianShangUserService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;

@Controller
@RequestMapping("/qianShangUserManager")
public class QianShangUserManagerController
{

	@Autowired
	private QianShangUserService qianShangUserService;

	@RequestMapping("/managerExportToExcel.do")
	public void managerExportToExcel(HttpServletRequest req, HttpServletResponse resp, Long[] ids, Long beginTime,
			Long endTime, Long id, String accountName, String name, String phoneNum, String managerName)
	{
		List<QianShangUserManagerData> qianShangUserList = new ArrayList<QianShangUserManagerData>();
		if (ids != null && ids.length > 0)
		{
			qianShangUserList = qianShangUserService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("accountName", CommonUtils.isEmpty(accountName) ? null : accountName);
			param.put("name", CommonUtils.isEmpty(name) ? null : name);
			param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
			param.put("id", id);
			param.put("managerName", CommonUtils.isEmpty(managerName) ? null : managerName);
			qianShangUserList = qianShangUserService.selectManagerData(param);
		}

		ExportToExcelUtil<QianShangUserManagerData> excelUtil = new ExportToExcelUtil<QianShangUserManagerData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "乾商管理员统计表");
			String[] headers = { "记录ID", "登录名", "真实姓名", "手机号", "所属部门", "职位", "邮箱", "QQ", "微信", "创建日期", "所属平台",
					"所属平台账号" };
			String[] columns = { "id", "accountName", "name", "phoneNum", "department", "position", "email", "qqNum",
					"weiXinNum", "createTimeStr", "plateForm", "managerName" };
			try
			{
				excelUtil.exportExcel(headers, columns, qianShangUserList, out, req, "");
			} catch (Exception e)
			{
				throw new RuntimeException("导出文件出错了！", e);
			}
		} catch (IOException e)
		{
			throw new RuntimeException("导出文件出错了！", e);
		} finally
		{

			try
			{
				out.flush();
				out.close();
			} catch (IOException e)
			{
				throw new RuntimeException("关闭资源出错了！", e);
			}
		}
	}

	@RequestMapping("/managerSearch.do")
	public @ResponseBody AjaxResult managerSearch(Long beginTime, Long endTime, Long id, String accountName,
			String name, String phoneNum, String managerName, Integer pageNum, Integer pageSize)
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
		param.put("accountName", CommonUtils.isEmpty(accountName) ? null : accountName);
		param.put("name", CommonUtils.isEmpty(name) ? null : name);
		param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
		param.put("id", id);
		param.put("managerName", CommonUtils.isEmpty(managerName) ? null : managerName);
		PageInfo<QianShangUserManagerData> pageInfo = qianShangUserService.pageManagerData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);

	}

}
