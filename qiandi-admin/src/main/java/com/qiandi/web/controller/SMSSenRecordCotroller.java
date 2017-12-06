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
import com.qiandi.pojo.SMSSendRecordData;
import com.qiandi.pojo.SMSSendTemplateData;
import com.qiandi.service.SMSSendRecordService;
import com.qiandi.service.SMSSendTemplateService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;

@RequestMapping("/smsSendRecord")
@Controller
public class SMSSenRecordCotroller
{

	@Autowired
	private SMSSendTemplateService smsSendTemplateService;

	@Autowired
	private SMSSendRecordService smsSendRecordService;

	@RequestMapping("/numberExportToExcel.do")
	public void numberExportToExcel(Long submitBeginTime, Long submitEndTime, Long sendBeginTime, Long sendEndTime,
			Long id, String menberUserAccountName, String menberUserName, String qianShangUserName, String phoneNum,
			String operator, String content, String sendStatus, Boolean sendMode, Long yiDongAisleId,
			Long lianTongAisleId, Long dianXingAisleId, String recivedStatus, String province, String city, Long[] ids,
			HttpServletRequest req, HttpServletResponse resp)
	{
		List<SMSSendRecordData> list = new ArrayList<SMSSendRecordData>();
		if (ids != null && ids.length > 0)
		{
			list = smsSendRecordService.selectNumberByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("submitBeginTime", submitBeginTime);
			param.put("submitEndTime", submitEndTime);
			param.put("sendBeginTime", sendBeginTime);
			param.put("sendEndTime", sendEndTime);
			param.put("id", id);
			param.put("menberUserAccountName",
					CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
			param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
			param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
			param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
			param.put("operator", CommonUtils.isEmpty(operator) ? null : operator);
			param.put("content", CommonUtils.isEmpty(content) ? null : content);
			param.put("sendStatus", CommonUtils.isEmpty(sendStatus) ? null : sendStatus);
			param.put("sendMode", sendMode);
			param.put("yiDongAisleId", yiDongAisleId);
			param.put("lianTongAisleId", lianTongAisleId);
			param.put("dianXingAisleId", dianXingAisleId);
			param.put("recivedStatus", CommonUtils.isEmpty(recivedStatus) ? null : recivedStatus);
			param.put("province", CommonUtils.isEmpty(province) ? null : province);
			param.put("city", CommonUtils.isEmpty(city) ? null : city);

			list = smsSendRecordService.selectNumberData(param);
		}

		ExportToExcelUtil<SMSSendRecordData> excelUtil = new ExportToExcelUtil<SMSSendRecordData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "发送记录号码模式统计表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "手机号码", "会员账户级别", "所属乾商", "接收手机号", "所属运营商", "省市", "短信内容",
					"字符长度", "计费条数", "计费金额", "提交时间", "发送方式", "短信类型", "发送时间", "发送状态", "下行通道", "接收状态", "接收状态说明",
					"接收状态时间", "审核方式", "审核人" };
			String[] columns = { "id", "menberUserAccountName", "menberUserName", "menberUserPhoneNum", "level",
					"qianShangUserName", "phoneNum", "operator", "address", "content", "length", "segment", "price",
					"submitTime", "sendMode", "smsType", "sendTime", "sendStatus", "aisleName", "recivedStatus",
					"recivedStatusDescription", "recivedTime", "auditAuthority", "auditAdminUserName" };
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

	@RequestMapping("/numberDelete.do")
	public @ResponseBody AjaxResult numberDelete(Long[] ids)
	{
		if (ids == null || ids.length <= 0)
		{
			return AjaxResult.errorInstance("将要删除的记录id不能为空");
		}

		smsSendRecordService.deleteNumberByArray(ids);
		return AjaxResult.successInstance("删除成功");
	}

	@RequestMapping("/numberSearch.do")
	public @ResponseBody AjaxResult numberSearch(Long submitBeginTime, Long submitEndTime, Long sendBeginTime,
			Long sendEndTime, Long id, String menberUserAccountName, String menberUserName, String qianShangUserName,
			String phoneNum, String operator, String content, String sendStatus, Boolean sendMode, Long yiDongAisleId,
			Long lianTongAisleId, Long dianXingAisleId, String recivedStatus, String province, String city,
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
		param.put("submitBeginTime", submitBeginTime);
		param.put("submitEndTime", submitEndTime);
		param.put("sendBeginTime", sendBeginTime);
		param.put("sendEndTime", sendEndTime);
		param.put("id", id);
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
		param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
		param.put("operator", CommonUtils.isEmpty(operator) ? null : operator);
		param.put("content", CommonUtils.isEmpty(content) ? null : content);
		param.put("sendStatus", CommonUtils.isEmpty(sendStatus) ? null : sendStatus);
		param.put("sendMode", sendMode);
		param.put("yiDongAisleId", yiDongAisleId);
		param.put("lianTongAisleId", lianTongAisleId);
		param.put("dianXingAisleId", dianXingAisleId);
		param.put("recivedStatus", CommonUtils.isEmpty(recivedStatus) ? null : recivedStatus);
		param.put("province", CommonUtils.isEmpty(province) ? null : province);
		param.put("city", CommonUtils.isEmpty(city) ? null : city);

		PageInfo<SMSSendRecordData> pageInfo = smsSendRecordService.pageNumberData(pageNum, pageSize, param);

		return AjaxResult.successInstance(pageInfo);

	}


	@RequestMapping("/taskExportToExcel.do")
	public void taskExportToExcel(Long submitBeginTime, Long submitEndTime, Long sendBeginTime, Long sendEndTime, Long id,
			String menberUserAccountName, String menberUserName, String qianShangUserName, String content,
			Boolean sendMode, String sendStatus, Long yiDongAisleId, Long lianTongAisleId, Long dianXingAisleId,
			Boolean auditAuthority, String auditAdminUserName, Long[] ids, HttpServletRequest req,
			HttpServletResponse resp)
	{
		List<SMSSendTemplateData> list = new ArrayList<SMSSendTemplateData>();
		if (ids != null && ids.length > 0)
		{
			list = smsSendTemplateService.selectTaskByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("submitBeginTime", submitBeginTime);
			param.put("submitEndTime", submitEndTime);
			param.put("sendBeginTime", sendBeginTime);
			param.put("sendEndTime", sendEndTime);
			param.put("id", id);
			param.put("menberUserAccountName",
					CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
			param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
			param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
			param.put("content", CommonUtils.isEmpty(content) ? null : content);
			param.put("sendMode", sendMode);
			param.put("sendStatus", CommonUtils.isEmpty(sendStatus) ? null : sendStatus);
			param.put("yiDongAisleId", yiDongAisleId);
			param.put("lianTongAisleId", lianTongAisleId);
			param.put("dianXingAsleId", dianXingAisleId);
			param.put("auditAuthority", auditAuthority);
			param.put("auditAdminUserName", CommonUtils.isEmpty(auditAdminUserName) ? null : auditAdminUserName);

			list = smsSendTemplateService.selectTaskData(param);
		}

		ExportToExcelUtil<SMSSendTemplateData> excelUtil = new ExportToExcelUtil<SMSSendTemplateData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "发送记录任务模式统计表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "手机号码", "会员账户级别", "所属乾商", "短信内容", "字符长度", "号码数量", "计费条数",
					"计费金额", "提交时间", "发送方式", "短信类型", "发送时间", "发送状态", "成功率", "实际发送号码", "接收成功号码", "接收失败号码", "未知号码",
					"随机去除号码", "黑名单号码", "审核方式", "审核人", "移动通道", "联通通电", "电信通道", "提交后短信余额" };
			String[] columns = { "id", "menberUserAccountName", "menberUserName", "menberUserPhoneNum", "level",
					"qianShangUserName", "content", "length", "totalCount", "totalPayCount", "totalPriceCount",
					"submitTime", "sendMode", "smsType", "sendTime", "sendStatus", "successRate", "actualCount",
					"recivedSuccessCount", "recivedFailCount", "recivedUnknownCount", "splitCount", "blackCount",
					"auditAuthority", "auditAdminUserName", "yiDongAisleName", "lianTongAisleName", "dianXingAisleName",
					"smsBlance" };
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

	// 批量删除
	@RequestMapping("/taskDelete.do")
	public @ResponseBody AjaxResult taskDelete(Long[] ids)
	{
		if (ids == null || ids.length <= 0)
		{
			return AjaxResult.errorInstance("将要被删除的记录id不能为空");
		}

		smsSendTemplateService.deleteByArray(ids);

		return AjaxResult.successInstance("删除成功");
	}

	@RequestMapping("/taskSearch.do")
	public @ResponseBody AjaxResult taskSearch(Long submitBeginTime, Long submitEndTime, Long sendBeginTime,
			Long sendEndTime, Long id, String menberUserAccountName, String menberUserName, String qianShangUserName,
			String content, Boolean sendMode, String sendStatus, Long yiDongAisleId, Long lianTongAisleId,
			Long dianXingAisleId, Boolean auditAuthority, String auditAdminUserName, Integer pageNum, Integer pageSize)
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
		param.put("sendEndTime", sendEndTime);
		param.put("id", id);
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
		param.put("content", CommonUtils.isEmpty(content) ? null : content);
		param.put("sendMode", sendMode);
		param.put("sendStatus", CommonUtils.isEmpty(sendStatus) ? null : sendStatus);
		param.put("yiDongAisleId", yiDongAisleId);
		param.put("lianTongAisleId", lianTongAisleId);
		param.put("dianXingAsleId", dianXingAisleId);
		param.put("auditAuthority", auditAuthority);
		param.put("auditAdminUserName", CommonUtils.isEmpty(auditAdminUserName) ? null : auditAdminUserName);
		
		PageInfo<SMSSendTemplateData> pageInfo = smsSendTemplateService.pageTaskData(pageNum, pageSize, param);
		
		return AjaxResult.successInstance(pageInfo);
		

	}
}
