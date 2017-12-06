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
import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.SensitiveWord;
import com.qiandi.pojo.SensitiveWordData;
import com.qiandi.service.SensitiveWordService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/sensitiveWord")
public class SensitiveWordController
{
	@Autowired
	private SensitiveWordService sensitiveWordService;

	@RequestMapping("/exportToExcel.do")
	public void exportToExcel(Long[] ids, Long id, Long beginTime, Long endTime, String name,
			String addAdminUserName, HttpServletRequest req, HttpServletResponse resp)
	{
		List<SensitiveWordData> list = new ArrayList<SensitiveWordData>();
		if (ids != null && ids.length > 0)
		{
			list = sensitiveWordService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("id", id);
			param.put("name", CommonUtils.isEmpty(name) ? null : name);
			param.put("addAdminUserName", CommonUtils.isEmpty(addAdminUserName) ? null : addAdminUserName);
			list = sensitiveWordService.selectData(param);
		}

		ExportToExcelUtil<SensitiveWordData> excelUtil = new ExportToExcelUtil<SensitiveWordData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "敏感词汇统计表");
			String[] headers = { "记录ID", "敏感词", "添加日期", "添加人" };
			String[] columns = { "id", "name", "createTimeStr", "addAdminUserName" };
			try
			{
				excelUtil.exportExcel(headers, columns, list, out, req, "");
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

	@RequestMapping("/delete.do")
	public @ResponseBody AjaxResult delete(Long[] ids)
	{
		if (ids == null || ids.length <= 0)
		{
			return AjaxResult.errorInstance("将要被删除的记录id不能为空");
		}

		sensitiveWordService.deleteByArray(ids);

		return AjaxResult.successInstance("删除成功");
	}

	@RequestMapping("/addNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(String name, HttpServletRequest req)
	{
		if (CommonUtils.isEmpty(name))
		{
			return AjaxResult.errorInstance("敏感词汇名字不能为空");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");

		SensitiveWord sensitiveWord = new SensitiveWord();
		sensitiveWord.setName(name);
		if (sensitiveWordService.isExisted(sensitiveWord))
		{
			return AjaxResult.errorInstance("这个敏感词已经存在，不能重复添加");
		}

		sensitiveWord = new SensitiveWord();
		sensitiveWord.setId(new IDUtils(0, 0).nextId());
		sensitiveWord.setAddAdminUserId(adminUser.getId());
		sensitiveWord.setName(name);
		sensitiveWord.setCreateTime(System.currentTimeMillis());

		sensitiveWordService.insert(sensitiveWord);
		return AjaxResult.successInstance("添加成功");
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long id, Long beginTime, Long endTime, String name, String addAdminUserName,
			Integer pageNum, Integer pageSize)
	{
		if (pageNum == null)
		{
			pageNum = 1;
		}
		if (pageSize == null)
		{
			pageSize = 2;
		}
		
		Map<String,Object> param =  new HashMap<String,Object>();
		param.put("beginTime", beginTime);
		param.put("endTime", endTime);
		param.put("id", id);
		param.put("name", CommonUtils.isEmpty(name) ? null : name);
		param.put("addAdminUserName", CommonUtils.isEmpty(addAdminUserName) ? null : addAdminUserName);
		
		PageInfo<SensitiveWordData> pageInfo = sensitiveWordService.pageData(pageNum, pageSize, param);

		
		return AjaxResult.successInstance(pageInfo);
	}
}
