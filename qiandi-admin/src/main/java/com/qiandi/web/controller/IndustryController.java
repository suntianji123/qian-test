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
import com.qiandi.pojo.IdName;
import com.qiandi.pojo.IdNameData;
import com.qiandi.service.IdNameService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/industry")
public class IndustryController
{
	@Autowired
	private IdNameService idNameService;

	@RequestMapping("/exportToExcel.do")
	public void exportToExcel(Long beginTime, Long endTime, String value, Long id, String addAdminUserName,
			Long[] ids, HttpServletRequest req, HttpServletResponse resp)
	{
		List<IdNameData> list = new ArrayList<IdNameData>();
		if (ids != null && ids.length > 0)
		{
			list = idNameService.selectByArray(ids, "行业类型");
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("name", "行业类型");
			param.put("value", CommonUtils.isEmpty(value) ? null : value);
			param.put("id", id);
			param.put("addAdminUserName", CommonUtils.isEmpty(addAdminUserName) ? null : addAdminUserName);
			list = idNameService.selectData(param);
		}

		ExportToExcelUtil<IdNameData> excelUtil = new ExportToExcelUtil<IdNameData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "行业类型统计表");
			String[] headers = { "记录ID", "行业名称", "添加日期", "添加人" };
			String[] columns = { "id", "value", "createTime", "addAdminUserName" };
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
			return AjaxResult.errorInstance("将要删除的记录id不能为空");
		}

		idNameService.deleteByArray(ids, "行业类型");

		return AjaxResult.successInstance("删除成功");
	}

	@RequestMapping("/updateSubmit.do")
	public @ResponseBody AjaxResult updateSubmit(Long id, String value, HttpServletRequest req)
	{
		if (CommonUtils.isEmpty(value))
		{
			return AjaxResult.errorInstance("行业类型不能为空");
		}

		IdName idName = new IdName();
		idName.setId(id);
		idName.setName("行业类型");
		idName = idNameService.selectOne(idName);
		if (idName == null)
		{
			return AjaxResult.errorInstance("记录不存在，请确定id的正确性");
		}
		
		IdName idName1 = new IdName();
		idName1.setName("行业类型");
		idName1.setValue(value);
		idName1= idNameService.selectOne(idName1);
		if (idName1 != null && idName1.getValue().equals(idName.getValue()))
		{
			return AjaxResult.errorInstance("行业类型已经存在，不能更改为这个名称");
		}
		
		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");

		idName.setValue(value);
		idName.setAddAdminUserId(adminUser.getId());
		idName.setCreateTime(System.currentTimeMillis());

		idNameService.update(idName);
		return AjaxResult.successInstance("修改成功");
	}

	@RequestMapping("/addNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(String value, HttpServletRequest req)
	{
		if (CommonUtils.isEmpty(value))
		{
			return AjaxResult.errorInstance("行业类型名称不能为空");
		}

		IdName idName = new IdName();
		idName.setName("行业类型");
		idName.setValue(value);
		if (idNameService.isExisted(idName))
		{
			return AjaxResult.errorInstance("行业类型名称已经存在，不能重复添加");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");

		idName = new IdName();
		idName.setId(new IDUtils(0, 0).nextId());
		idName.setName("行业类型");
		idName.setValue(value);
		idName.setAddAdminUserId(adminUser.getId());
		idName.setCreateTime(System.currentTimeMillis());

		idNameService.insert(idName);
		return AjaxResult.successInstance("添加成功");
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, String value, Long id, String addAdminUserName,
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
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("beginTime", beginTime);
		param.put("endTime", endTime);
		param.put("name", "行业类型");
		param.put("value", CommonUtils.isEmpty(value) ? null : value);
		param.put("id", id);
		param.put("addAdminUserName", CommonUtils.isEmpty(addAdminUserName) ? null : addAdminUserName);
		
		PageInfo<IdNameData> pageInfo = idNameService.pageData(pageNum, pageSize, param);
		
		return AjaxResult.successInstance(pageInfo);
		
		
	}
}
