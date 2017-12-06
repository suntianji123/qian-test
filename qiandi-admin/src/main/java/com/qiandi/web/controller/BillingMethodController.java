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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.BillingMethod;
import com.qiandi.pojo.BillingMethodData;
import com.qiandi.pojo.MenberUserSetting;
import com.qiandi.service.BillingMethodService;
import com.qiandi.service.MenberUserService;
import com.qiandi.service.MenberUserSettingService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/billingMethod")
public class BillingMethodController
{
	@Autowired
	private BillingMethodService billingMethodService;

	@Autowired
	private MenberUserService menberUserSevice;

	@Autowired
	private MenberUserSettingService menberUserSettingService;

	@RequestMapping("/updateSubmit.do")
	public @ResponseBody AjaxResult updateSubmit(Long id, Boolean billingMethod, HttpServletRequest req)
	{
		MenberUserSetting menberUserSetting = menberUserSettingService.selectOne(id);
		if (menberUserSetting != null)
		{
			AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
			BillingMethod param = new BillingMethod();
			IDUtils idUtils = new IDUtils(0, 0);
			param.setId(idUtils.nextId());
			param.setMenberUserId(menberUserSetting.getMenberUserId());
			param.setCreateTime(System.currentTimeMillis());
			param.setAddAdminUserId(adminUser.getId());
			menberUserSetting.setBillingMethod(billingMethod);
			billingMethodService.updateBoth(menberUserSetting, param);
		}

		return AjaxResult.successInstance("修改成功");
	}

	@RequestMapping("/update.do")
	public @ResponseBody AjaxResult update(String phoneNum)
	{
		if (!CommonUtils.isPhone(phoneNum))
		{
			return AjaxResult.errorInstance("请输入正确的手机号");
		}
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("phoneNum", phoneNum);
		Map<String, Object> map = menberUserSevice.selectOneByPhoneNumForBillingMethod(param);
		return AjaxResult.successInstance(map);
		
	}

	@RequestMapping(value = "/exportToExcel.do", method = RequestMethod.GET)
	public void exportToExcel(Long beginTime, Long endTime, Long[] ids, HttpServletRequest req,
			HttpServletResponse resp)
	{
		List<BillingMethodData> smsTypeList = new ArrayList<BillingMethodData>();
		if (ids != null && ids.length > 0)
		{
			smsTypeList = billingMethodService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			smsTypeList = billingMethodService.selectData(param);
		}

		ExportToExcelUtil<BillingMethodData> excelUtil = new ExportToExcelUtil<BillingMethodData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "计费方式设置记录表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "会员账户级别", "所属乾商", "计费方式", "设置日期", "设置人" };
			String[] columns = { "id", "menberUserAccountName", "menberUserName", "level",
					"qianShangUserName", "billingMethodName", "createTimeStr", "addAdminUserName" };
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
	public @ResponseBody AjaxResult deleteById(Long[] ids, Long id, HttpServletRequest req)
	{

		if (ids != null && ids.length > 0)
		{
			billingMethodService.deleteByArray(ids);
		} else
		{
			if (id != null)
			{
				billingMethodService.delete(id);
			}
		}

		return AjaxResult.successInstance("删除成功！");
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, Long id, String menberUserAccountName,
			String qianShangUserName, Integer pageNum, Integer pageSize)
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
		param.put("menberUserAccountName", menberUserAccountName);
		param.put("qianShangUserName", qianShangUserName);
		
		PageInfo<BillingMethodData> pageInfo = billingMethodService.pageData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);
	}
}
