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
import com.qiandi.pojo.SMSSendTemplateAuditData;
import com.qiandi.service.SMSSendTemplateService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;

@Controller
@RequestMapping("/smsSendTemplateDisable")
public class SMSSendTemplateDisable
{
	@Autowired
	private SMSSendTemplateService smsSendTemplateService;

	@RequestMapping("/exportToExcel.do")
	public void exportToExcel(Long submitBeginTime, Long submitEndTime, Long sendBeginTime, Long sendEndTime, Long id,
			String menberUserAccountName, String qianShangUserName, Boolean isDisabled, HttpServletRequest req,
			HttpServletResponse resp, Long[] ids)
	{
		List<SMSSendTemplateAuditData> list = new ArrayList<SMSSendTemplateAuditData>();
		if (ids != null && ids.length > 0)
		{
			list = smsSendTemplateService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("submitBeginTime", submitBeginTime);
			param.put("submitEndTime", submitEndTime);
			param.put("sendBeginTime", sendBeginTime);
			param.put("submitEndTime", submitEndTime);
			param.put("menberUserAccountName",
					CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
			param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
			param.put("isDisabled", isDisabled);
			param.put("sendMode", true);
			param.put("isDisabledTime", System.currentTimeMillis());

			list = smsSendTemplateService.selectData(param);
		}

		ExportToExcelUtil<SMSSendTemplateAuditData> excelUtil = new ExportToExcelUtil<SMSSendTemplateAuditData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "待审核短信任务统计表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "所属乾商", "短信内容", "字符长度", "分条", "号码数量", "计费条数", "计费金额", "提交日期",
					"发送方式", "发送日期", "发送状态", "禁用状态" };
			String[] columns = { "id", "menberUserAccountName", "menberUserName", "qianShangUserName", "content",
					"length", "segment", "totalCount", "totalPayCount", "totalPriceCount", "submitTimeStr",
					"sendModeStr", "sendTimeStr", "sendStatusStr", "isDisabledStr" };
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

	@RequestMapping("/updateIsDisabled.do")
	public @ResponseBody AjaxResult updateIsDisabled(Long[] ids,Boolean isDisabled)
	{
		if(ids==null||ids.length<=0)
		{
			return AjaxResult.errorInstance("没有选择需要操作的id");
		}
		if (isDisabled == null)
		{
			return AjaxResult.errorInstance("没有选择需要更新的状态");
		}

		smsSendTemplateService.updateIsDisabled(ids, isDisabled);

		return AjaxResult.successInstance("修改成功");

	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Integer pageNum, Integer pageSize, Long submitBeginTime, Long submitEndTime,
			Long sendBeginTime, Long sendEndTime, Long id, String menberUserAccountName, String qianShangUserName,
			Boolean isDisabled)
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
		param.put("submitBeginTime", submitBeginTime);
		param.put("submitEndTime", submitEndTime);
		param.put("sendBeginTime", sendBeginTime);
		param.put("submitEndTime", submitEndTime);
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
		param.put("isDisabled", isDisabled);
		param.put("sendMode", true);
		param.put("isDisabledTime", System.currentTimeMillis());
		PageInfo<SMSSendTemplateAuditData> pageInfo = smsSendTemplateService.pageAuditData(pageNum, pageSize, param);

		return AjaxResult.successInstance(pageInfo);
	}
}