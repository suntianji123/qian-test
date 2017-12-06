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
import com.qiandi.pojo.BalanceWarningStatus;
import com.qiandi.pojo.BalanceWarningStatusData;
import com.qiandi.service.BalanceWarningStatusService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/balanceWarningStatus")
public class BalanceWarningStatusController
{

	@Autowired
	private BalanceWarningStatusService balanceWarningStatusService;

	@RequestMapping("/addNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(HttpServletRequest req, Boolean warningStatus, Integer defaultValue,
			Integer reminderMode, Long id)
	{
		if (defaultValue == null || defaultValue < 500)
		{
			return AjaxResult.errorInstance("短信最低预警条数不能低于500条");
		}
		if (warningStatus == null)
		{
			return AjaxResult.errorInstance("预警状态不能为空");
		}
		if (reminderMode == null || reminderMode <= 0 || reminderMode > 16)
		{
			return AjaxResult.errorInstance("提醒方式错误");
		}

		BalanceWarningStatus balanceWarningStatus = balanceWarningStatusService.selectOne(id);
		if (balanceWarningStatus == null)
		{
			return AjaxResult.errorInstance("用户的预警状态不存在");
		} else
		{
			AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
			balanceWarningStatus.setId(new IDUtils(0, 0).nextId());
			balanceWarningStatus.setCreateTime(System.currentTimeMillis());
			balanceWarningStatus.setAddAdminUserId(adminUser.getId());
			balanceWarningStatus.setDefaultValue(defaultValue);
			balanceWarningStatus.setReminderMode(reminderMode);
			balanceWarningStatus.setWarningStatus(warningStatus);
			balanceWarningStatus.setAddMenberUserId(0l);
			balanceWarningStatus.setPlateForm("2");

			balanceWarningStatusService.insert(balanceWarningStatus);

		}
		return AjaxResult.successInstance("添加成功");

	}

	@RequestMapping("/delete.do")
	public @ResponseBody AjaxResult delete(Long[] ids, Long id, HttpServletRequest req)
	{

		if (ids != null && ids.length > 0)
		{
			balanceWarningStatusService.deleteByArray(ids);
		} else
		{
			if (id != null)
			{
				balanceWarningStatusService.delete(id);
			}
		}

		return AjaxResult.successInstance("删除成功！");
	}

	@RequestMapping(value = "/exportToExcel.do", method = RequestMethod.GET)
	public void exportToExcel(Long beginTime, Long endTime, Long[] ids, HttpServletRequest req,
			HttpServletResponse resp)
	{
		List<BalanceWarningStatusData> balanceWarningStatusList = new ArrayList<BalanceWarningStatusData>();
		if (ids != null && ids.length > 0)
		{
			balanceWarningStatusList = balanceWarningStatusService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			balanceWarningStatusList = balanceWarningStatusService.selectData(param);
		}

		ExportToExcelUtil<BalanceWarningStatusData> excelUtil = new ExportToExcelUtil<BalanceWarningStatusData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "收件地址统计表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "手机号码", "所属乾商", "预警状态", "预警值", "提醒方式", "设置日期", "操作人", "操作平台" };
			String[] columns = { "id", "menberUserAccountName", "menberUserName", "menberUserPhoneNum",
					"qianShangUserName", "warningStatusStr", "defaultValue", "reminderModeStr", "createTimeStr",
					"addUserName", "plateFormStr" };
			try
			{
				excelUtil.exportExcel(headers, columns, balanceWarningStatusList, out, req, "");
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

	@RequestMapping("/searchOne.do")
	public @ResponseBody AjaxResult searchOne(String menberUserAccountName)
	{
		if (CommonUtils.isEmpty(menberUserAccountName))
		{
			return AjaxResult.errorInstance("账户名不能为空");
		}

		BalanceWarningStatusData balanceWarningStatusData = balanceWarningStatusService
				.selectOneData(menberUserAccountName);
		if (balanceWarningStatusData == null)
		{
			return AjaxResult.errorInstance("会员账户不存在");
		}
		return AjaxResult.successInstance(balanceWarningStatusData);
	}

	@RequestMapping("/updateSubmit")
	public @ResponseBody AjaxResult updateSubmit(Long id, Boolean warningStatus, Integer defaultValue,
			Integer reminderMode)
	{
		if (id == null)
		{
			return AjaxResult.errorInstance("记录id不存在");
		}
		if (warningStatus == null)
		{
			return AjaxResult.errorInstance("预警状态不能为空");
		}
		if (defaultValue == null || defaultValue < 500)
		{
			return AjaxResult.errorInstance("短信预警条数不能低于500");
		}
		if (reminderMode == null || reminderMode == 0)
		{
			return AjaxResult.errorInstance("提醒方式不能为空");
		}

		// 唯一性判断
		BalanceWarningStatus balanceWarningStatus = balanceWarningStatusService.selectOne(id);
		if (balanceWarningStatus != null)
		{
			balanceWarningStatus.setDefaultValue(defaultValue);
			balanceWarningStatus.setWarningStatus(warningStatus);
			balanceWarningStatus.setReminderMode(reminderMode);
			balanceWarningStatus.setCreateTime(System.currentTimeMillis());
			balanceWarningStatus.setPlateForm("2");
			balanceWarningStatusService.update(balanceWarningStatus);
		} else
		{
			return AjaxResult.errorInstance("短信预警记录不存在");
		}
		return AjaxResult.successInstance("修改成功");
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, String menberUserAccountName,
			String menberUserName, String menberUserPhoneNum, String qianShangUserName, Long id, Boolean warningStatus,
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
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
		param.put("menberUserPhoneNum", CommonUtils.isEmpty(menberUserPhoneNum) ? null : menberUserPhoneNum);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
		param.put("id", id);
		param.put("warningStatus", warningStatus);
		PageInfo<BalanceWarningStatusData> pageInfo = balanceWarningStatusService.pageData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);


	}
}
