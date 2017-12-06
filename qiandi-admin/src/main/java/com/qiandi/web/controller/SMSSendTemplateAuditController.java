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
import com.qiandi.pojo.SMSSendTemplateAuditData;
import com.qiandi.service.IdNameService;
import com.qiandi.service.SMSSendTemplateService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;

@Controller
@RequestMapping("/smsSendTemplateAudit")
public class SMSSendTemplateAuditController
{

	@Autowired
	private IdNameService idNameService;

	@Autowired
	private SMSSendTemplateService smsSendTemplateService;

	@RequestMapping("/exportToExcel.do")
	public void exportToExcel(Long submitBeginTime, Long submitEndTime, Long sendBeginTime, Long sendEndTime, Long id,
			String menberUserAccountName, String qianShangUserName, Boolean sendMode, Long[] ids,
			HttpServletRequest req, HttpServletResponse resp)
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
			param.put("sendMode", sendMode);

			list = smsSendTemplateService.selectData(param);
		}

		ExportToExcelUtil<SMSSendTemplateAuditData> excelUtil = new ExportToExcelUtil<SMSSendTemplateAuditData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "待审核短信任务统计表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "所属乾商", "短信内容", "字符长度", "分条", "号码数量", "计费条数", "计费金额",
					"提交日期", "发送方式", "发送日期", "发送状态", "审核状态" };
			String[] columns = { "id", "menberUserAccountName", "menberUserName", 
					"qianShangUserName", "content", "length", "segment", "totalCount",
					"totalPayCount", "totalPriceCount", "submitTimeStr", "sendModeStr", "sendTimeStr", "sendStatusStr",
					"auditResultStr" };
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


	@RequestMapping("/updateAuditResult.do")
	public @ResponseBody AjaxResult updateAuditResult(Long[] ids, Long auditResultReasonId, String auditResult,
			HttpServletRequest req)
	{
		if (!"1".equals(auditResult) && !"2".equals(auditResult))
		{
			return AjaxResult.errorInstance("审核结果不在可选范围内，请确定可选参数");
		}

		if (ids == null || ids.length <= 0)
		{
			return AjaxResult.errorInstance("参数id不能为空");
		}

		if ("1".equals(auditResult) && auditResultReasonId != null)
		{
			if(idNameService.selectOne(auditResultReasonId)==null)
			{
				return AjaxResult.errorInstance("拒绝原因不合法");
			}
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");

		smsSendTemplateService.updateBoth(ids, auditResultReasonId, auditResult, adminUser.getId());



		return AjaxResult.successInstance("批量审核成功");

	}

	/**
	 * 搜索列表
	 */
	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long submitBeginTime, Long submitEndTime, Long sendBeginTime,
			Long sendEndTime, Long id, String menberUserAccountName, String qianShangUserName, Boolean sendMode,
			Integer pageSize, Integer pageNum)
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
		param.put("sendMode", sendMode);

		PageInfo<SMSSendTemplateAuditData> pageInfo = smsSendTemplateService.pageAuditData(pageNum, pageSize, param);

		return AjaxResult.successInstance(pageInfo);
	}
}
