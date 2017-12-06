
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
import com.qiandi.pojo.MenberUser;
import com.qiandi.pojo.MenberUserManagerData;
import com.qiandi.service.MenberUserService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;

@Controller
@RequestMapping("/menberUser")
public class MenberUserController
{
	@Autowired
	private MenberUserService menberUserService;

	@RequestMapping("/managerExportToExcel.do")
	public void managerExportToExcel(HttpServletRequest req, HttpServletResponse resp, Long[] ids, Long beginTime,
			Long endTime, Long id, String accountName, String name, String phoneNum, String managerName)
	{
		List<MenberUserManagerData> menberUserList = new ArrayList<MenberUserManagerData>();
		if (ids != null && ids.length > 0)
		{
			menberUserList = menberUserService.selectByArray(ids);
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
			menberUserList = menberUserService.selectManagerData(param);
		}

		ExportToExcelUtil<MenberUserManagerData> excelUtil = new ExportToExcelUtil<MenberUserManagerData>();
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
				excelUtil.exportExcel(headers, columns, menberUserList, out, req, "");
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
		PageInfo<MenberUserManagerData> pageInfo = menberUserService.pageManagerData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);

	}

	@RequestMapping("/list.do")
	public @ResponseBody AjaxResult search(Integer timeType, String accountName, String name, String phoneNum,
			Long qianShangUserId, Integer smsBlanceLow, Integer smsBlanceHigh, String position, Integer accountStatus,
			Integer smsType,
			Boolean auditAuthority, Boolean billingMethod, Integer creditLow, Integer creditHigh,
			Integer ableCreditLow, Integer ableCreditHigh,
			Integer totalRechargeCountLow, Integer totalRechargeCountHigh, Integer level, String province, String city,
			Integer accountPeriod, Boolean warningStatus, Integer defaultValue, Integer activeStatus, Boolean isSerious,
			HttpServletRequest req,Integer curr,Integer pageSize)
	{
		if (curr == null)
		{
			curr = 1;
		}
		if (pageSize == null)
		{
			pageSize = 1;
		}

		// 查询出所有的会员
		Long beginTime = null;
		Long endTime = null;

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("beginTime", beginTime);
		param.put("endTime", endTime);
		param.put("accountName", accountName);
		param.put("name", name);
		param.put("phoneNum", phoneNum);
		param.put("qianShangUserId", qianShangUserId);
		param.put("smsBlanceLow", smsBlanceLow);
		param.put("smsBlanceHigh", smsBlanceHigh);
		param.put("position", position);
		param.put("accountStatus", accountStatus);
		param.put("smsType", smsType);
		param.put("auditAuthority", auditAuthority);
		param.put("billingMethod", billingMethod);
		param.put("creditLow", creditLow);
		param.put("creditHigh", creditHigh);
		param.put("totalRechargeCountLow", totalRechargeCountLow);
		param.put("totalRechargeCountHigh", totalRechargeCountHigh);
		param.put("level", level);
		param.put("address", province + city);
		param.put("accountPeriod", accountPeriod);
		param.put("warningStatus", warningStatus);
		param.put("defaultValue", defaultValue);
		param.put("activeStatus", activeStatus);
		param.put("isSerious", isSerious);
		// todo :设置联表查询出结果

		PageInfo<MenberUser> menberUserList = menberUserService.pageDataByAdminUser(param, curr, pageSize,
				"createTime desc");

		return AjaxResult.successInstance(menberUserList);

	}
}
