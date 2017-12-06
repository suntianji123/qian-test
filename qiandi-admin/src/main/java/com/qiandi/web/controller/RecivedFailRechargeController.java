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
import com.qiandi.pojo.RecivedFailRechargeData;
import com.qiandi.service.RecivedFailRechargeService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;

@Controller
@RequestMapping("/recivedFailRecharge")
public class RecivedFailRechargeController
{
	@Autowired
	private RecivedFailRechargeService recivedFailRechargeService;

	@RequestMapping("/exportToExcel.do")
	public void exportToExcel(Long submitBeginTime, Long submitEndTime, Long beginTime, Long endTime, Long id,
			Long rechargeRecordId, String menberUserAccountName, String menberUserName, String menberUserPhoneNum,
			String qianShangUserName, Integer recivedFailCount, Boolean returnStatus, HttpServletRequest req,
			HttpServletResponse resp, Long[] ids)
	{
		List<RecivedFailRechargeData> list = new ArrayList<RecivedFailRechargeData>();
		if (ids != null && ids.length > 0)
		{
			list = recivedFailRechargeService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("submitBeginTime", submitBeginTime);
			param.put("submitEndTime", submitEndTime);
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("id", id);
			param.put("rechargeRecordId", rechargeRecordId);
			param.put("menberUserAccountName",
					CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
			param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
			param.put("menberUserPhoneNum", CommonUtils.isEmpty(menberUserPhoneNum) ? null : menberUserPhoneNum);
			param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
			param.put("recivedFailCount", recivedFailCount);
			param.put("returnStatus", returnStatus);

			list = recivedFailRechargeService.selectData(param);
		}

		ExportToExcelUtil<RecivedFailRechargeData> excelUtil = new ExportToExcelUtil<RecivedFailRechargeData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "接收失败返回统计表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "手机号码", "会员账户级别", "所属乾商", "充值记录ID", "提交日期", "接收失败条数", "返回充值条数",
					"返回充值状态", "返回充值日期" };
			String[] columns = { "id", "menberUserAccountName", "menberUserName", "phoneNum", "level",
					"qianShangUserName", "rechargeRecordId", "submitTime", "recivedFailCount", "returnCount",
					"returnStatus", "createTime" };
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

		recivedFailRechargeService.deleteByArray(ids);

		return AjaxResult.successInstance("删除成功");
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long submitBeginTime, Long submitEndTime, Long beginTime, Long endTime,
			Long id, Long rechargeRecordId, String menberUserAccountName, String menberUserName, Integer pageNum,
			Integer pageSize, String menberUserPhoneNum, String qianShangUserName, Integer recivedFailCount,
			Boolean returnStatus)
	{
		if(pageNum==null)
		{
			pageNum =1;
		}
		if(pageSize==null)
		{
			pageSize = 2;
		}

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("submitBeginTime", submitBeginTime);
		param.put("submitEndTime", submitEndTime);
		param.put("beginTime", beginTime);
		param.put("endTime", endTime);
		param.put("id", id);
		param.put("rechargeRecordId", rechargeRecordId);
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
		param.put("menberUserPhoneNum", CommonUtils.isEmpty(menberUserPhoneNum) ? null : menberUserPhoneNum);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
		param.put("recivedFailCount", recivedFailCount);
		param.put("returnStatus", returnStatus);
		

		PageInfo<RecivedFailRechargeData> pageInfo = recivedFailRechargeService.pageData(pageNum, pageSize, param);

		return AjaxResult.successInstance(pageInfo);

	}
}
