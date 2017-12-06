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
import com.qiandi.pojo.SMSType;
import com.qiandi.pojo.SMSTypeData;
import com.qiandi.service.SMSTypeService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/smsType")
public class SMSTypeController
{
	@Autowired
	private SMSTypeService smsTypeService;

	@RequestMapping(value = "/exportToExcel.do")
	public void exportToExcel(Long beginTime, Long endTime, Long[] ids, Long id, HttpServletRequest req,
			HttpServletResponse resp)
	{
		List<SMSTypeData> smsTypeList = new ArrayList<SMSTypeData>();
		if (ids != null && ids.length > 0)
		{
			smsTypeList = smsTypeService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("id", id);
			smsTypeList = smsTypeService.selectData(param);
		}
		
		ExportToExcelUtil<SMSTypeData> excelUtil = new ExportToExcelUtil<SMSTypeData>();
        OutputStream out = null;  
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "信息类型统计表");
			String[] headers = { "记录ID", "信息类型名称", "添加日期", "操作人" };
			String[] columns = { "id", "name", "createTimeStr", "addAdminUserName" };
			try
			{
				excelUtil.exportExcel(headers, columns, smsTypeList, out, req, "");
			} catch (Exception e)
			{
				throw new RuntimeException("导出文件出错了！", e);
			}
		} catch (IOException e)
		{
			throw new RuntimeException("导出文件出错了！", e);
		}finally
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


	@RequestMapping("/updateSubmit.do")
	public @ResponseBody AjaxResult updateSubmit(Long id, String name, HttpServletRequest req)
	{
		if (CommonUtils.isEmpty(name))
		{
			return AjaxResult.errorInstance("信息名称不能为空");
		}

		SMSType smsType = new SMSType();
		smsType.setName(name);
		if (smsTypeService.isExisted(smsType))
		{
			return AjaxResult.errorInstance("信息名称已经存在，不能重复添加");
		}

		smsType = new SMSType();
		smsType = smsTypeService.selectOne(id);
		if(smsType!=null)
		{
			AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
			smsType.setName(name);
			smsType.setAddAdminUserId(adminUser.getId());
			smsTypeService.update(smsType);
		}
		return AjaxResult.successInstance("修改成功");
	}

	@RequestMapping("/delete.do")
	public @ResponseBody AjaxResult deleteById(Long[] ids, Long id, HttpServletRequest req)
	{

		if (ids != null && ids.length > 0)
		{
			smsTypeService.deleteByArray(ids);
		} else
		{
			if (id != null)
			{
				smsTypeService.delete(id);
			}
		}

		return AjaxResult.successInstance("删除成功！");
	}

	@RequestMapping(value = "/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, Long id, Integer pageNum, Integer pageSize,
			HttpServletRequest req, HttpServletResponse resp) throws IOException
	{

		if (pageNum == null)
		{
			pageNum = 1;
		}
		if(pageSize==null)
		{
			pageSize = 2;
		}
		

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("beginTime", beginTime);
		param.put("endTime", endTime);
		param.put("id", id);


		PageInfo<SMSTypeData> pageInfo = smsTypeService.pageData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);
	}

	@RequestMapping("/addNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(String name, HttpServletRequest req)
	{
		if (CommonUtils.isEmpty(name))
		{
			return AjaxResult.errorInstance("信息类型名称不能为空");
		}


		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");

		SMSType smsType = new SMSType();
		// 唯一性检查
		smsType.setName(name);
		if (smsTypeService.isExisted(smsType))
		{
			return AjaxResult.errorInstance("信息类型已存在，不能重复添加！");
		}

		IDUtils idUtils = new IDUtils(0, 0);
		smsType = new SMSType();
		smsType.setId(idUtils.nextId());
		smsType.setCreateTime(System.currentTimeMillis());
		smsType.setAddAdminUserId(adminUser.getId());
		smsType.setName(name);
		smsTypeService.insert(smsType);
		return AjaxResult.successInstance("ok");
	}

}
