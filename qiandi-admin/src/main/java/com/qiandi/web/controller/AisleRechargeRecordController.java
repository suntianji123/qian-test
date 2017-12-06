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
import com.qiandi.pojo.Aisle;
import com.qiandi.pojo.AisleRechargeRecord;
import com.qiandi.pojo.AisleRechargeRecordData;
import com.qiandi.service.AisleRechargeRecordService;
import com.qiandi.service.AisleService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/aisleRechargeRecord")
public class AisleRechargeRecordController
{
	@Autowired
	private AisleRechargeRecordService aisleRechargeRecordService;

	@Autowired
	private AisleService aisleService;

	@RequestMapping("/exportToExcel")
	public void exportToExcel(Long beginTime, Long endTime, String aisleName, String aisleCompany,
			String rechargeStatus, String applyAdminUserName, Long id, String auditAdminUserName, String payoutCompany,
			String payoutMan, String auditResult, Long ids[], Boolean type, HttpServletRequest req,
			HttpServletResponse resp)
	{
		if (type == null)
		{
			return;
		}
		List<AisleRechargeRecordData> list = new ArrayList<AisleRechargeRecordData>();
		if (ids != null && ids.length > 0)
		{
			list = aisleRechargeRecordService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("aisleName", CommonUtils.isEmpty(aisleName) ? null : aisleName);
			param.put("aisleCompany", CommonUtils.isEmpty(aisleCompany) ? null : aisleCompany);
			param.put("rechargeStatus", CommonUtils.isEmpty(rechargeStatus) ? null : rechargeStatus);
			param.put("applyAdminUserName", CommonUtils.isEmpty(applyAdminUserName) ? null : applyAdminUserName);
			param.put("auditAdminUserName", CommonUtils.isEmpty(auditAdminUserName) ? null : auditAdminUserName);
			param.put("id", id);
			param.put("payoutCompany", CommonUtils.isEmpty(payoutCompany) ? null : payoutCompany);
			param.put("payoutMan", CommonUtils.isEmpty(payoutMan) ? null : payoutMan);
			param.put("auditResult", CommonUtils.isEmpty(auditResult) ? null : auditResult);
			list = aisleRechargeRecordService.selectData(param);
		}

		ExportToExcelUtil<AisleRechargeRecordData> excelUtil = new ExportToExcelUtil<AisleRechargeRecordData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "通道充值记录统计表");

			
			if (!type)
			{
				String[] headers = { "记录ID", "申请日期", "通道名称", "单位名称", "计费单价", "充值金额", "充值条数", "充值状态", "申请人", };
				String[] columns = { "id", "applyTimeStr", "aisleName", "aisleCompany", "aislePriceStr",
						"rechargeAmountPayAble", "rechargeCount", "rechargeStatusStr", "applyAdminUserName" };
				try
				{
					excelUtil.exportExcel(headers, columns, list, out, req, "");
				} catch (Exception e)
				{
					throw new RuntimeException("导出文件出错了！", e);
				}
			}else
			{
				String[] headers = { "记录ID", "申请日期", "通道名称", "单位名称", "计费单价", "充值金额", "充值条数", "审核结果", "充值状态", "申请人",
						"审核人",
						"出款单位",
						"出款凭证号", "出款人", "出款日期" };
				String[] columns = { "id", "applyTimeStr", "aisleName", "aisleCompany", "aislePriceStr",
						"rechargeAmountPayAble", "rechargeCount", "auditResultStr", "rechargeStatusStr",
						"applyAdminUserName",
						"auditAdminUserName", "payoutCompany", "payoutNum", "payoutMan", "payoutTimeStr" };
				try
				{
					excelUtil.exportExcel(headers, columns, list, out, req, "");
				} catch (Exception e)
				{
					throw new RuntimeException("导出文件出错了！", e);
				}
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
			return AjaxResult.errorInstance("没有选择需要删除的记录id");
		}

		aisleRechargeRecordService.deleteByArray(ids);

		return AjaxResult.successInstance("删除成功");
	}

	@	RequestMapping("/addPayoutInformation.do")
	public @ResponseBody AjaxResult addPayoutInfomation(Long id, String payoutCompany,
			String payoutNum, String payoutMan, Long payoutTime)
	{
		if (CommonUtils.isEmpty(payoutCompany))
		{
			return AjaxResult.errorInstance("出款单位不能为空");
		}
		if (CommonUtils.isEmpty(payoutNum))
		{
			return AjaxResult.errorInstance("出款凭证号不能为空");
		}
		if (CommonUtils.isEmpty(payoutMan))
		{
			return AjaxResult.errorInstance("出款人不能为空");
		}
		if (payoutTime == null || payoutTime <= 0)
		{
			return AjaxResult.errorInstance("出款时间错误");
		}

		AisleRechargeRecord aisleRechargeRecord = aisleRechargeRecordService.selectOne(id);
		if (aisleRechargeRecord == null)
		{
			return AjaxResult.errorInstance("通道充值记录不存在");
		} else
		{
			aisleRechargeRecord.setPayoutCompany(payoutCompany);
			aisleRechargeRecord.setPayoutMan(payoutMan);
			aisleRechargeRecord.setPayoutNum(payoutNum);
			aisleRechargeRecord.setPayoutTime(payoutTime);
			aisleRechargeRecord.setRechargeStatus("4");
			aisleRechargeRecordService.update(aisleRechargeRecord);
		}

		return AjaxResult.successInstance("添加出款信息成功");

	}


	// 批量更改通道状态：1.允许充值；2.拒绝充值
	@RequestMapping("/updateAuditResult.do")
	public @ResponseBody AjaxResult updateAuditResult(Long ids[], String auditResult,HttpServletRequest req )
	{
		if (ids == null || ids.length == 0)
		{
			return AjaxResult.errorInstance("没有选择需要审核的充值记录ID");
		}
		if (!"2".equals(auditResult) && !"1".equals(auditResult))
		{
			return AjaxResult.errorInstance("审核结果值不正确");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("ids", ids);
		param.put("auditAdminUserId", adminUser.getId());
		param.put("auditResult", auditResult);
		aisleRechargeRecordService.updateAuditResultByArray(param);
		return AjaxResult.successInstance("审核成功");

	}

	@RequestMapping("/applyRechargeSubmit.do")
	public @ResponseBody AjaxResult applyRechargeSubmit(Integer rechargeCount, String paymentMethod, String accountName,
			String openBank, String accountNum, Long aisleId, HttpServletRequest req)
	{
		if (rechargeCount == null || rechargeCount <= 0)
		{
			return AjaxResult.errorInstance("充值条数正确");
		}
		if (CommonUtils.isEmpty(paymentMethod))
		{
			return AjaxResult.errorInstance("付款方式不能为空");
		}
		if (!"1".equals(paymentMethod) && !"2".equals(paymentMethod) && !"3".equals(paymentMethod)
				&& !"4".equals(paymentMethod))
		{
			return AjaxResult.errorInstance("付款方式不正确");
		}
		if (CommonUtils.isEmpty(openBank))
		{
			return AjaxResult.errorInstance("开户行不能为空");
		}
		if (CommonUtils.isEmpty(accountNum))
		{
			return AjaxResult.errorInstance("账号不能为空");
		}
		if (CommonUtils.isEmpty(accountName))
		{
			return AjaxResult.errorInstance("账户不能为空");
		}
		if (aisleId == null)
		{
			return AjaxResult.errorInstance("通道记录id不能为空");
		}

		Aisle aisle = new Aisle();
		aisle.setId(aisleId);
		aisle.setIsOn(false);
		if (!aisleService.isExisted(aisle))
		{
			return AjaxResult.errorInstance("通道不存在或者处于关闭状态");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");

		AisleRechargeRecord aisleRechargeRecord = new AisleRechargeRecord();
		aisleRechargeRecord.setId(new IDUtils(0, 0).nextId());
		aisleRechargeRecord.setAccountName(accountName);
		aisleRechargeRecord.setAccountNum(accountNum);
		aisleRechargeRecord.setOpenBank(openBank);
		aisleRechargeRecord.setPaymentMethod(paymentMethod);
		aisleRechargeRecord.setApplyTime(System.currentTimeMillis());
		aisleRechargeRecord.setAisleId(aisleId);
		aisleRechargeRecord.setApplyAdminUserId(adminUser.getId());
		aisleRechargeRecord.setAuditAdminUserId(0l);
		aisleRechargeRecord.setRechargeCount(rechargeCount);
		aisleRechargeRecord.setRechargeStatus("1");
		aisleRechargeRecord.setAuditResult("3");
		aisleRechargeRecord.setPayoutCompany("");
		aisleRechargeRecord.setPayoutNum("");
		aisleRechargeRecord.setPayoutMan("");
		aisleRechargeRecord.setPayoutTime(0l);
		aisleRechargeRecordService.insert(aisleRechargeRecord);
		return AjaxResult.successInstance("申请通道充值成功，请等待审核");
	}

	@RequestMapping("/searchAisleByName.do")
	public @ResponseBody AjaxResult searchAisleByName(String name)
	{
		Map<String, Object> map = aisleService.selectItemForRecharge(name);
		map.put("id", map.get("id") + "");
		return AjaxResult.successInstance(map);
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, String aisleName, String aisleCompany,
			String rechargeStatus, String applyAdminUserName, Long id, String auditAdminUserName, String payoutCompany,
			String payoutMan, String auditResult, Integer pageNum, Integer pageSize)
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
		param.put("aisleName", CommonUtils.isEmpty(aisleName) ? null : aisleName);
		param.put("aisleCompany", CommonUtils.isEmpty(aisleCompany) ? null : aisleCompany);
		param.put("rechargeStatus", CommonUtils.isEmpty(rechargeStatus) ? null : rechargeStatus);
		param.put("applyAdminUserName", CommonUtils.isEmpty(applyAdminUserName) ? null : applyAdminUserName);
		param.put("auditAdminUserName", CommonUtils.isEmpty(auditAdminUserName) ? null : auditAdminUserName);
		param.put("id", id);
		param.put("payoutCompany", CommonUtils.isEmpty(payoutCompany) ? null : payoutCompany);
		param.put("payoutMan", CommonUtils.isEmpty(payoutMan) ? null : payoutMan);
		param.put("auditResult", CommonUtils.isEmpty(auditResult) ? null : auditResult);

		PageInfo<AisleRechargeRecordData> pageInfo = aisleRechargeRecordService.pageData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);

	}
}
