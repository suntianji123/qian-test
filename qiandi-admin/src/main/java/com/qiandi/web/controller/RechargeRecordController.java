package com.qiandi.web.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.github.pagehelper.PageInfo;
import com.qiandi.pojo.AccountOrder;
import com.qiandi.pojo.AccountOrderData;
import com.qiandi.pojo.AdminReceiptAccount;
import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.IdName;
import com.qiandi.pojo.IncomeSettlement;
import com.qiandi.pojo.LateFeeFormula;
import com.qiandi.pojo.MenberUser;
import com.qiandi.pojo.MenberUserItemForRecharge;
import com.qiandi.pojo.MenberUserSetting;
import com.qiandi.pojo.QianShangUser;
import com.qiandi.pojo.RechargeRecord;
import com.qiandi.pojo.RechargeRecordDataForAdmin;
import com.qiandi.pojo.Repayment;
import com.qiandi.service.AccountOrderService;
import com.qiandi.service.AdminReceiptAccountService;
import com.qiandi.service.IdNameService;
import com.qiandi.service.IncomeSettlementService;
import com.qiandi.service.MenberUserService;
import com.qiandi.service.MenberUserSettingService;
import com.qiandi.service.QianShangUserService;
import com.qiandi.service.RechargeRecordService;
import com.qiandi.service.RepaymentService;
import com.qiandi.table.IncomeSettlementDataForRechargeRecord;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;
import com.qiandi.util.JsonUtils;
import com.qiandi.util.UploadUtils;

@Controller
@RequestMapping("/rechargeRecord")
public class RechargeRecordController
{
	@Autowired
	private MenberUserService menberUserService;

	@Autowired
	private RechargeRecordService rechargeRecordService;

	@Autowired
	private MenberUserSettingService menberUserSettingService;

	@Autowired
	private AdminReceiptAccountService adminReceiptAccountService;

	@Autowired
	private QianShangUserService qianShangUserService;

	@Autowired
	private IncomeSettlementService incomeSettlementService;

	@Autowired
	private AccountOrderService accountOrderService;

	@Autowired
	private RepaymentService repaymentService;

	@Autowired
	private IdNameService idNameService;

	public static void main(String[] args)
	{
		String str = "acde";
		System.out.println(str.indexOf("cd") != -1);
		System.out.println(str.substring(0, 1));
		System.out.println(str.length());
	}

	/*
	 * 测试定时计算滞纳金，更新rechargeRecord接口
	 */
	@RequestMapping("/test.do")
	public @ResponseBody AjaxResult test()
	{
		// 1. 查询所有没有还清款的订单（支付方式为授信额度，还款状态为没有还清除）
		// 2.当前时间大于最迟还款时间，将逾期天数+1，根据逾期天数，查询出百分比，计算待付滞纳金=待还款金额*百分比+待付滞纳金

		Repayment param1 = new Repayment();
		param1.setRepaymentResult(false);
		List<Repayment> repaymentList = repaymentService.selectList(param1);

		// 查询滞纳金计算标准
		IdName param = new IdName();
		param.setName("滞纳金计算公式");
		List<IdName> idNameList = idNameService.selectList(param);

		if (!CommonUtils.isEmpty(repaymentList))
		{
			for (int i = 0; i < repaymentList.size(); i++)
			{
				Repayment repayment = repaymentList.get(i);
				int overdueCount = repayment.getOverdueCount() + 1;
				int percent = 0;
				int percentOne = repayment.getPercentOne();
				int percentTwo = repayment.getPercentTwo();
				int percentThree = repayment.getPercentThree();
				int percentFour = repayment.getPercentFour();
				repayment.setOverdueCount(overdueCount);

				for (IdName idName : idNameList)
				{
					LateFeeFormula lateFeeFormula = JsonUtils.toBean(idName.getValue(), LateFeeFormula.class);
					String type = lateFeeFormula.getType();
					if (type.indexOf("<N≤") != -1 && type.length() == 7)
					{
						int begin = Integer.parseInt(String.valueOf(type.substring(0, 2)));
						int end = Integer.parseInt(String.valueOf(type.substring(5, 7)));
						if (overdueCount > begin && overdueCount <= end)
						{
							percent = lateFeeFormula.getPercent();
							if (begin == 3 && end == 6)
							{
								percentTwo = percent;
							} else if (begin == 6 && end == 9)
							{
								percentThree = percent;
							}
						}
						
					} else if (type.indexOf(">") != -1 && type.length() == 4)
					{
						int begin = Integer.parseInt(String.valueOf(type.substring(2, 4)));
						if (overdueCount > begin)
						{
							percent = lateFeeFormula.getPercent();
							percentFour = percent;
						}
					} else if (type.indexOf("≤") != -1 && type.length() == 4)
					{
						int begin = Integer.parseInt(String.valueOf(type.substring(2, 4)));
						if (overdueCount <= begin)
						{
							percent = lateFeeFormula.getPercent();
							percentOne = percent;

						}
					}

				}

				repayment.setPercent(percent);
				repayment.setPercentOne(percentOne);
				repayment.setPercentTwo(percentTwo);
				repayment.setPercentThree(percentThree);
				repayment.setPercentFour(percentFour);
				int temp = (int) (percent
						* (repayment.getRemainingRepaymentCount()) * 0.01);
				int remainingLateFeeCount = repayment.getRemainingLateFeeCount()
						+ temp;
				repayment.setRemainingLateFeeCount(remainingLateFeeCount);
				repayment.setTotalLateFeeCount(temp + repayment.getTotalLateFeeCount());
				repaymentList.set(i, repayment);
			}

			repaymentService.updateLateFeeItem(repaymentList);
		}

		return AjaxResult.successInstance("测试成功");
	}

	/*
	 * 对公付款，对私付款审核提交
	 */
	@RequestMapping("/updateAccountOrderResult.do")
	public @ResponseBody AjaxResult updateAccountOrderResult(Long id, String result, HttpServletRequest req)
	{
		if (id == null)
		{
			return AjaxResult.errorInstance("审核的打款记录不能为空");
		}
		if (!"1".equals(result) && !"2".equals(result))
		{
			return AjaxResult.errorInstance("审核结果的值必须为通过或者拒绝");
		}

		AccountOrder accountOrder = accountOrderService.selectOne(id);

		if (accountOrder == null)
		{
			return AjaxResult.errorInstance("打款记录不存在");
		}

		if (!"0".equals(accountOrder.getResult()))
		{
			return AjaxResult.errorInstance("这条打款记录已经审核过了，不能再次审核");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");

		accountOrder.setResult(result);
		accountOrder.setAuditAdminUserId(adminUser.getId());
		accountOrder.setCreateTime(System.currentTimeMillis());

		long rechargeRecordId = accountOrder.getRechargeRecordId();

		RechargeRecord rechargeRecord = rechargeRecordService.selectOne(rechargeRecordId);

		if (rechargeRecord == null)
		{
			return AjaxResult.errorInstance("这条打款记录不属于任何充值记录");
		}



		

		// 审核拒绝
		if ("1".equals(result))
		{
			rechargeRecord.setStatus("1");
			rechargeRecord.setCreateTime(System.currentTimeMillis());
			rechargeRecordService.updateByAccount(rechargeRecord, accountOrder, null, null, null);
		} else if ("2".equals(result))
		{

			// 审核通过

			long menberUserId = rechargeRecord.getMenberUserId();
			MenberUser menberUser = menberUserService.selectOne(menberUserId);
			MenberUserSetting menberUserSetting = new MenberUserSetting();
			menberUserSetting.setMenberUserId(menberUserId);
			menberUserSetting = menberUserSettingService.selectOne(menberUserSetting);
			int smsBlance = 0;
			int totalRechargeCount = 0;
			int rechargeCount = rechargeRecord.getRechargeCount();
			if (menberUserSetting.getSmsBlance() != null)
			{
				smsBlance = menberUserSetting.getSmsBlance() + rechargeCount;
				totalRechargeCount = menberUserSetting.getTotalRechargeCount() + rechargeCount;
			}
			
			
			menberUserSetting.setTotalRechargeCount(totalRechargeCount);
			menberUserSetting.setSmsBlance(smsBlance);
			
			// 审核通过
			rechargeRecord.setSmsBlance(smsBlance);
			rechargeRecord.setStatus("0");
			rechargeRecord.setCreateTime(System.currentTimeMillis());

			// 计算乾商收益，插入收益清算表
			int handsel = 0;
			int rebate = 0;
			int price = rechargeRecord.getPrice();
			int settlementPercent = rechargeRecord.getSettlementPercent();
			int settlementPrice = rechargeRecord.getSettlementPrice();
			int pipelinePrice = rechargeRecord.getPipelinePrice();

			// 计算个人收益
			double liquidation = (double) (((price - settlementPrice) * 0.001 * rechargeCount
					- handsel * settlementPrice * 0.001 - rebate) * settlementPercent) * 0.01;
			// 计算管道收益
			double pipeLiquidation = pipelinePrice * 0.001 * rechargeCount;

			rechargeRecord.setLiquidationStatus(false);
			rechargeRecord.setTotalLiquidation(liquidation + pipeLiquidation);
			rechargeRecord.setLiquidationTime(0l);

			long qianShangUserId = 0L;
			Long qianShangUserTemp = menberUser.getQiangShangUserId();
			if (qianShangUserTemp != null && qianShangUserTemp != 0)
			{
				qianShangUserId = qianShangUserTemp;
			}

			QianShangUser qianShangUser = qianShangUserService.selectOne(qianShangUserId);
			long pipeQianShangUserId = 0L;
			Long pipeQianShangUserIdTemp = qianShangUser.getLeaderId();
			if (pipeQianShangUserIdTemp != null && pipeQianShangUserIdTemp != 0)
			{
				pipeQianShangUserId = pipeQianShangUserIdTemp;
			}

			// 设置乾商个人收益
			IDUtils idUtils = new IDUtils(0, 0);
			IncomeSettlement personIncomeSettlement = new IncomeSettlement();
			personIncomeSettlement.setId(idUtils.nextId());
			personIncomeSettlement.setCreateTime(System.currentTimeMillis());
			personIncomeSettlement.setHandsel(handsel);
			personIncomeSettlement.setLiquidation(liquidation);
			personIncomeSettlement.setLiquidationStatus(false);
			personIncomeSettlement.setQianShangUserId(qianShangUserId);
			personIncomeSettlement.setRebate(rebate);
			personIncomeSettlement.setRechargeRecordId(rechargeRecordId);
			personIncomeSettlement.setType("0");

			// 设置乾商管道收益
			IncomeSettlement pipeIncomeSettlement = new IncomeSettlement();
			pipeIncomeSettlement.setId(idUtils.nextId());
			pipeIncomeSettlement.setCreateTime(System.currentTimeMillis());
			pipeIncomeSettlement.setHandsel(handsel);
			pipeIncomeSettlement.setLiquidation(pipeLiquidation);
			pipeIncomeSettlement.setLiquidationStatus(false);
			pipeIncomeSettlement.setQianShangUserId(pipeQianShangUserId);
			pipeIncomeSettlement.setRebate(rebate);
			pipeIncomeSettlement.setRechargeRecordId(rechargeRecordId);
			pipeIncomeSettlement.setType("1");

			// 更新会员配置表、充值记录，插入个人收益、管道收益
			rechargeRecordService.updateByAccount(rechargeRecord, accountOrder, menberUserSetting,
					personIncomeSettlement,
					pipeIncomeSettlement);
			

		}

		return AjaxResult.successInstance("审核成功");

	}

	/*
	 * 加载单个对公付款，对私付款图片
	 */
	@RequestMapping("/loadAccountOrder.do")
	public @ResponseBody AjaxResult loadAccountOrder(Long id)
	{
		if(id==null)
		{
			return AjaxResult.errorInstance("查询的付款记录id不能为空");
		}
		
		
		AccountOrderData accountOrderData = accountOrderService.selectOneData(id);
		if (accountOrderData == null)
		{
			return AjaxResult.errorInstance("付款记录不存在");
		}
		
		
		

		return AjaxResult.successInstance(accountOrderData);
	}

	/*
	 * 查询订单结算状态
	 */
	@RequestMapping("/searchIncomeSettlement.do")
	public @ResponseBody AjaxResult searchSettlement(Long rechargeRecordId)
	{
		if(rechargeRecordId==null)
		{
			return AjaxResult.errorInstance("需要查询的记录id不能为空");
		}
		
		List<IncomeSettlementDataForRechargeRecord> incomeSettlementList = incomeSettlementService
				.selectItemByRechargeRecordId(rechargeRecordId);
		return AjaxResult.successInstance(incomeSettlementList);
	}

	/*
	 * 导出到excel
	 */
	@RequestMapping("/exportToExcel.do")
	public void exportToExcel(Integer beginTime, Integer endTime, String menberUserAccountName,
			String menberUserName, String phoneNum, String qianShangUserName, String rechargeType, String status,
			String rechargePlateForm, String operateUserName, String paymentMethod, String repaymentStatus,
			Boolean billing, Long[] ids, HttpServletRequest req, HttpServletResponse resp)
	{
		List<RechargeRecordDataForAdmin> list = new ArrayList<RechargeRecordDataForAdmin>();
		if (ids != null && ids.length > 0)
		{
			list = rechargeRecordService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("menberUserAccountName",
					CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
			param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
			param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
			param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
			param.put("rechargeType", CommonUtils.isEmpty(rechargeType) ? null : rechargeType);
			param.put("status", CommonUtils.isEmpty(status) ? null : status);
			param.put("rechargePlateForm", CommonUtils.isEmpty(rechargePlateForm) ? null : rechargePlateForm);
			param.put("operateUserName", CommonUtils.isEmpty(operateUserName) ? null : operateUserName);
			param.put("paymentMethod", CommonUtils.isEmpty(paymentMethod) ? null : paymentMethod);
			param.put("repaymentStatus", CommonUtils.isEmpty(repaymentStatus) ? null : repaymentStatus);
			param.put("billing", billing);
			list = rechargeRecordService.selectAdminData(param);
		}

		ExportToExcelUtil<RechargeRecordDataForAdmin> excelUtil = new ExportToExcelUtil<RechargeRecordDataForAdmin>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "充值记录统计表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "手机号", "会员账户级别", "所属乾商", "计费单价", "结算单价", "本次充值短信", "充值后短信余额",
					"充值类型", "充值时间", "充值状态", "充值平台", "操作员", "应付金额", "付款方式", "付款状态", "付款时间", "应付滞纳金", "逾期天数", "收益",
					"清算状态", "清算时间", "开票需求", "充值次数" };
			String[] columns = { "id", "menberUserAccountName", "menberUserName", "phoneNum", "level",
					"qianShangUserName", "price", "settlementPrice", "rechargeCount", "smsBlance", "rechargeType",
					"createTime", "status", "rechargePlateForm", "operateUserName", "paymentValue", "paymentMethod",
					"repaymentStatus", "repaymentTime", "amountPayAble", "overdueCount", "totalLiquidation",
					"liquidationStatus", "liquidationTime", "billing", "count" };
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

	/*
	 * 删除
	 */
	@RequestMapping("/delete.do")
	public @ResponseBody AjaxResult delete(Long[] ids)
	{
		if (ids == null || ids.length <= 0)
		{
			return AjaxResult.errorInstance("将要删除的记录id不能为空");
		}

		rechargeRecordService.deleteByArray(ids);

		return AjaxResult.successInstance("删除成功");

	}

	/*
	 * 搜索充值记录列表
	 */
	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Integer beginTime, Integer endTime, String menberUserAccountName,
			String menberUserName, String phoneNum, String qianShangUserName, String rechargeType, String status,
			String rechargePlateForm, String operateUserName, String paymentMethod, String repaymentStatus,
			Boolean billing, Integer pageSize, Integer pageNum)
	{
		if(pageSize==null)
		{
			pageSize = 2;
		}
		if(pageNum==null)
		{
			pageNum = 1;
		}

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("beginTime", beginTime);
		param.put("endTime", endTime);
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
		param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
		param.put("rechargeType", CommonUtils.isEmpty(rechargeType) ? null : rechargeType);
		param.put("status", CommonUtils.isEmpty(status) ? null : status);
		param.put("rechargePlateForm", CommonUtils.isEmpty(rechargePlateForm) ? null : rechargePlateForm);
		param.put("operateUserName", CommonUtils.isEmpty(operateUserName) ? null : operateUserName);
		param.put("paymentMethod", CommonUtils.isEmpty(paymentMethod) ? null : paymentMethod);
		param.put("repaymentStatus", CommonUtils.isEmpty(repaymentStatus) ? null : repaymentStatus);
		param.put("billing", billing);

		PageInfo<RechargeRecordDataForAdmin> pageInfo = rechargeRecordService.pageAdminData(pageNum, pageSize, param);

		return AjaxResult.successInstance(pageInfo);

	}


	/*
	 * 通过授信额度付款
	 */
	@RequestMapping("/payByCredit.do")
	public @ResponseBody AjaxResult payByCredit(String rechargeType, String paymentMethod, Integer subTotal,
			Boolean billing, Long menberUserId, HttpServletRequest req)
	{
		if (!"1".equals(rechargeType) && !"2".equals(rechargeType) && !"3".equals(rechargeType)
				&& !"4".equals(rechargeType) && !"5".equals(rechargeType) && !"6".equals(rechargeType))
		{
			return AjaxResult.errorInstance("没有选择充值类型");
		}

		if (!"5".equals(paymentMethod))
		{
			return AjaxResult.errorInstance("付款方式必须为授信额度");
		}
		if (subTotal == null || subTotal <= 0)
		{
			return AjaxResult.errorInstance("充值条数必须大于0");
		}
		if (menberUserId == null || menberUserId <= 0)
		{
			return AjaxResult.errorInstance("充值会员记录id不能为空");
		}
		MenberUser menberUser = menberUserService.selectOne(menberUserId);
		if (menberUser == null)
		{
			return AjaxResult.errorInstance("会员不存在");
		}

		//判断可用授信额度是否足够
		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		int price = 0;
		if (menberUser.getPrice() != null)
		{
			price = menberUser.getPrice();
		}
		int settlementPrice = 0;
		if (menberUser.getSettlementPrice() != null)
		{
			settlementPrice = menberUser.getSettlementPrice();
		}
		int settlementPercent = 0;
		if (menberUser.getSettlementPercent() != null)
		{
			settlementPercent = menberUser.getSettlementPercent();
		}
		int pipelinePrice = 0;
		if (menberUser.getPipelinePrice() != null)
		{
			pipelinePrice = menberUser.getPipelinePrice();
		}
		
		// 查询会员的可用授信额度
		MenberUserSetting menberUserSetting = new MenberUserSetting();
		menberUserSetting.setMenberUserId(menberUserId);
		menberUserSetting = menberUserSettingService.selectOne(menberUserSetting);

		int availableQuota = 0;

		if (menberUserSetting.getAvailableQuota() != null)
		{
			availableQuota = menberUserSetting.getAvailableQuota();
		}
		if (availableQuota < subTotal * price)
		{
			return AjaxResult.errorInstance("这个会员的可用授信额度不足，不能选择授信额度付款");
		}
		
		IDUtils idUtils = new IDUtils(0, 0);
		long rechargeRecordId = idUtils.nextId();
		RechargeRecord rechargeRecord = new RechargeRecord();
		rechargeRecord.setId(rechargeRecordId);
		rechargeRecord.setCouponId(0l);
		rechargeRecord.setMenberUserId(menberUserId);
		rechargeRecord.setOperateMenberUserId(0L);
		rechargeRecord.setOperateQianShangUserId(0l);
		rechargeRecord.setOperateAdminUserId(adminUser.getId());
		rechargeRecord.setOrderId(0L);
		rechargeRecord.setPrice(price);
		rechargeRecord.setSettlementPrice(settlementPrice);
		rechargeRecord.setSettlementPercent(settlementPercent);
		rechargeRecord.setPipelinePrice(pipelinePrice);
		rechargeRecord.setPaymentMethod(paymentMethod);
		rechargeRecord.setRechargeType(rechargeType);
		rechargeRecord.setRepaymentStatus("1".equals(rechargeType) ? "2" : "0");
		rechargeRecord.setRepaymentTime(0l);
		rechargeRecord.setPaymentValue(subTotal * price);
		rechargeRecord.setRechargeCount(subTotal);
		rechargeRecord.setBilling(billing);
		rechargeRecord.setRechargePlateForm("2");
		rechargeRecord.setCreateTime(System.currentTimeMillis());
		rechargeRecord.setHandsel(0);
		rechargeRecord.setStatus("0");
		int smsBlance = 0;

		if (menberUser.getSmsBlance() != null)
		{
			smsBlance += menberUser.getSmsBlance() + subTotal;
		}
		int count = 0;
		if (menberUser.getCount() != null)
		{
			count += menberUser.getCount() + 1;
		}
		rechargeRecord.setSmsBlance(smsBlance);
		rechargeRecord.setCount(count);


		menberUserSetting.setMenberUserId(menberUserId);
		menberUserSetting = menberUserSettingService.selectOne(menberUserSetting);
		menberUserSetting.setSmsBlance(smsBlance);
		menberUserSetting.setCount(count);
		int totalRechargeCount = 0;
		if (menberUserSetting.getTotalRechargeCount() != null)
		{
			totalRechargeCount += menberUser.getTotalRechargeCount() + subTotal;
		}
		menberUserSetting.setTotalRechargeCount(totalRechargeCount);

		// 修改可用授信额度
		menberUserSetting.setAvailableQuota(availableQuota - subTotal * price);

		// 插入到滞纳金动态表
		// RechargeRecordLateFee rechargeRecordLateFee = new
		// RechargeRecordLateFee();
		// rechargeRecordLateFee.setId(idUtils.nextId());
		// rechargeRecordLateFee.setActualPayTime(0l);
		// rechargeRecordLateFee.setAmountPayAble(0);
		// rechargeRecordLateFee.setIsPay(false);
		// rechargeRecordLateFee.setCreateTime(System.currentTimeMillis());
		// rechargeRecordLateFee.setLastestPayTime(
		// System.currentTimeMillis() + 60 * 60 * 24 * 1000 *
		// (menberUserSetting.getAccountPeriod()));
		// rechargeRecordLateFee.setOverdueCount(0);
		// rechargeRecordLateFee.setPercent(0);
		// rechargeRecordLateFee.setRechargeRecordId(rechargeRecordId);
		// rechargeRecordLateFee.setStatus(true);

		rechargeRecord.setRepaymentCertificate("");
		rechargeRecord.setFailReasonId(0l);
//		rechargeRecord.setLastestRepaymentTime(
//				System.currentTimeMillis() + 60 * 60 * 24 * 1000 * (menberUserSetting.getAccountPeriod()));
//		rechargeRecord.setActualRepaymentTime(0l);
		rechargeRecord.setHavingLiquidation(0.0);// 已清算收益

		// 计算乾商收益，插入收益清算表
		int handsel = 0;
		int rebate = 0;
		// 计算个人收益
		double liquidation = (double) (((price - settlementPrice) * 0.001 * subTotal - handsel * settlementPrice * 0.001
				- rebate) * settlementPercent) * 0.01;
		// 计算管道收益
		double pipeLiquidation = pipelinePrice * 0.001 * subTotal;

		rechargeRecord.setLiquidationStatus(false);
		rechargeRecord.setTotalLiquidation(liquidation + pipeLiquidation);
		rechargeRecord.setLiquidationTime(0l);

		long qianShangUserId = 0L;
		Long qianShangUserTemp = menberUser.getQiangShangUserId();
		if (qianShangUserTemp != null && qianShangUserTemp != 0)
		{
			qianShangUserId = qianShangUserTemp;
		}

		QianShangUser qianShangUser = qianShangUserService.selectOne(qianShangUserId);
		long pipeQianShangUserId = 0L;
		Long pipeQianShangUserIdTemp = qianShangUser.getLeaderId();
		if (pipeQianShangUserIdTemp != null && pipeQianShangUserIdTemp != 0)
		{
			pipeQianShangUserId = pipeQianShangUserIdTemp;
		}

		// 设置乾商个人收益
		IncomeSettlement personIncomeSettlement = new IncomeSettlement();
		personIncomeSettlement.setId(idUtils.nextId());
		personIncomeSettlement.setCreateTime(System.currentTimeMillis());
		personIncomeSettlement.setHandsel(handsel);
		personIncomeSettlement.setLiquidation(liquidation);
		personIncomeSettlement.setLiquidationStatus(false);
		personIncomeSettlement.setQianShangUserId(qianShangUserId);
		personIncomeSettlement.setRebate(rebate);
		personIncomeSettlement.setRechargeRecordId(rechargeRecordId);
		personIncomeSettlement.setType("0");

		// 设置乾商管道收益
		IncomeSettlement pipeIncomeSettlement = new IncomeSettlement();
		pipeIncomeSettlement.setId(idUtils.nextId());
		pipeIncomeSettlement.setCreateTime(System.currentTimeMillis());
		pipeIncomeSettlement.setHandsel(handsel);
		pipeIncomeSettlement.setLiquidation(pipeLiquidation);
		pipeIncomeSettlement.setLiquidationStatus(false);
		pipeIncomeSettlement.setQianShangUserId(pipeQianShangUserId);
		pipeIncomeSettlement.setRebate(rebate);
		pipeIncomeSettlement.setRechargeRecordId(rechargeRecordId);
		pipeIncomeSettlement.setType("1");
		
		
		//插入还款信息
		Repayment repayment = new Repayment();
		repayment.setId(idUtils.nextId());
		repayment.setActualRepaymentTime(0l);
		repayment.setHavingLateFeeCount(0);
		repayment.setLastestRepaymentTime(System.currentTimeMillis() + 60 * 60 * 24 * 1000 * (menberUserSetting.getAccountPeriod()));;
		repayment.setOverdueCount(0);
		repayment.setPercent(0);
		repayment.setRechargeRecordId(rechargeRecordId);
		repayment.setRemainingLateFeeCount(0);
		repayment.setRepaymentResult(false);
		repayment.setRemainingRepaymentCount(subTotal * price);
		repayment.setTotalLateFeeCount(0);
		repayment.setPercentOne(0);
		repayment.setPercentTwo(0);
		repayment.setPercentThree(0);
		repayment.setPercentFour(0);
		repayment.setWaitAuditLateFeeCount(0);
		repayment.setWaitAuditRepaymentCount(0);

		rechargeRecordService.insertByCredit(rechargeRecord, menberUserSetting,
				personIncomeSettlement, pipeIncomeSettlement, repayment);


		return AjaxResult.successInstance("充值成功");

	}

	/*
	 * 通过对公付款、对私付款
	 */
	@RequestMapping("/payByAccount.do")
	public @ResponseBody AjaxResult payByAccount(String rechargeType, String paymentMethod, Integer subTotal,
			Boolean billing, Long menberUserId, 
			Long adminReceiptAccountId, HttpServletRequest req)
	{
		if (!"1".equals(rechargeType) && !"2".equals(rechargeType) && !"3".equals(rechargeType)
				&& !"4".equals(rechargeType) && !"5".equals(rechargeType) && !"6".equals(rechargeType))
		{
			return AjaxResult.errorInstance("没有选择充值类型");
		}

		if (!"3".equals(paymentMethod) && !"4".equals(paymentMethod))
		{
			return AjaxResult.errorInstance("付款方式必须为对公或对私转账");
		}
		if (subTotal == null || subTotal <= 0)
		{
			return AjaxResult.errorInstance("充值条数必须大于0");
		}
		if (adminReceiptAccountId == null)
		{
			return AjaxResult.errorInstance("收款账户不能为空");
		}
		if (menberUserId == null || menberUserId <= 0)
		{
			return AjaxResult.errorInstance("充值会员记录id不能为空");
		}
		AdminReceiptAccount adminReceiptAccount = new AdminReceiptAccount();
		adminReceiptAccount.setId(adminReceiptAccountId);
		adminReceiptAccount.setType("3".equals(paymentMethod) ? false : true);
		adminReceiptAccount = adminReceiptAccountService.selectOne(adminReceiptAccount);
		if (adminReceiptAccount == null)
		{
			return AjaxResult.errorInstance("对公或对私账户不存在");
		}
		MenberUser menberUser = menberUserService.selectOne(menberUserId);
		if (menberUser == null)
		{
			return AjaxResult.errorInstance("会员不存在");
		}

		if (billing == null)
		{
			billing = false;
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		int price = 0;
		if (menberUser.getPrice() != null)
		{
			price = menberUser.getPrice();
		}
		int settlementPrice = 0;
		if (menberUser.getSettlementPrice() != null)
		{
			settlementPrice = menberUser.getSettlementPrice();
		}
		int settlementPercent = 0;
		if (menberUser.getSettlementPercent() != null)
		{
			settlementPercent = menberUser.getSettlementPercent();
		}
		int pipelinePrice = 0;
		if (menberUser.getPipelinePrice() != null)
		{
			pipelinePrice = menberUser.getPipelinePrice();
		}

		RechargeRecord rechargeRecord = new RechargeRecord();
		IDUtils idUtils = new IDUtils(0, 0);
		long rechargeRecordId = idUtils.nextId();
		rechargeRecord.setId(rechargeRecordId);
		rechargeRecord.setCouponId(0l);
		rechargeRecord.setMenberUserId(menberUserId);
		rechargeRecord.setOperateMenberUserId(0L);
		rechargeRecord.setOperateQianShangUserId(0l);
		rechargeRecord.setOperateAdminUserId(adminUser.getId());
		rechargeRecord.setOrderId(0L);
		rechargeRecord.setPrice(price);
		rechargeRecord.setSettlementPrice(settlementPrice);
		rechargeRecord.setSettlementPercent(settlementPercent);
		rechargeRecord.setPipelinePrice(pipelinePrice);
		rechargeRecord.setPaymentMethod(paymentMethod);
		rechargeRecord.setRechargeType(rechargeType);
		rechargeRecord.setRepaymentStatus("1".equals(rechargeType) ? "3" : "0");
		rechargeRecord.setRepaymentTime(0l);
		rechargeRecord.setPaymentValue(subTotal * price);
		rechargeRecord.setRechargeCount(subTotal);
		rechargeRecord.setBilling(billing);
		rechargeRecord.setRechargePlateForm("2");
		rechargeRecord.setCreateTime(System.currentTimeMillis());
		rechargeRecord.setHandsel(0);
		rechargeRecord.setStatus("2");

		rechargeRecord.setLiquidationStatus(false);
		rechargeRecord.setTotalLiquidation(0.0);
		rechargeRecord.setLiquidationTime(0l);
		int smsBlance = 0;

		if (menberUser.getSmsBlance() != null)
		{
			smsBlance += menberUser.getSmsBlance();
		}
		int count = 0;
		if (menberUser.getCount() != null)
		{
			count += menberUser.getCount() + 1;
		}
		rechargeRecord.setSmsBlance(smsBlance);
		rechargeRecord.setCount(count);

		MenberUserSetting menberUserSetting = new MenberUserSetting();
		menberUserSetting.setMenberUserId(menberUserId);
		menberUserSetting = menberUserSettingService.selectOne(menberUserSetting);
		// menberUserSetting.setSmsBlance(smsBlance);
		menberUserSetting.setCount(count);
		//



		// 获取上传于accountOrder的数据
		MultipartHttpServletRequest multipartHttpServletReq = (MultipartHttpServletRequest) req;
		MultipartFile file = multipartHttpServletReq.getFile("up-file");

		if (file == null)
		{
			return AjaxResult.errorInstance("请上传图片！");
		}
		String fileName = file.getOriginalFilename();
		if (CommonUtils.isEmpty(fileName))
		{
			return AjaxResult.errorInstance("请上传图片");
		}
		String extension = UploadUtils.getExtension(fileName);
		String savePath = req.getServletContext().getRealPath("/upload");
		File fileSave = new File(savePath);
		String savaImgPath = null;
		if (!fileSave.exists() && !fileSave.isDirectory())
		{
			fileSave.mkdirs();
		}
		try
		{
			savaImgPath = savePath + "\\" + UUID.randomUUID().toString() + extension;
			file.transferTo(new File(savaImgPath));
		} catch (IllegalStateException | IOException e)
		{
			throw new RuntimeException("拷贝文件出错");
		}

		// AccountOrder accountOrder = new AccountOrder();
		// accountOrder.setId(idUtils.nextId());
		// accountOrder.setAmountPayAble(amountPayAble);
		// accountOrder.setCreateTime(System.currentTimeMillis());
		// accountOrder.setAuditAdminUserId(0l);
		// accountOrder.setImgPath(savaImgPath);
		// accountOrder.setPayAccountName(accountName);
		// accountOrder.setPayAccountNum(accountNum);
		// accountOrder.setReceiptAccountId(adminReceiptAccountId);
		// accountOrder.setRechargeRecordId(rechargeRecordId);
		// accountOrder.setResult("0");

		rechargeRecord.setRepaymentCertificate(savaImgPath);
		rechargeRecord.setFailReasonId(0l);
		rechargeRecord.setHavingLiquidation(0.0);// 已清算收益

		// 更新会员短信余额、充值次数
		rechargeRecordService.insertByAdminByAccount(rechargeRecord, menberUserSetting);

		return AjaxResult.successInstance("请等待审核");
	}


	/*
	 * 通过微信或者支付宝付款
	 */
	@RequestMapping("/payByWeiXinOrZhiFuBao.do")
	public @ResponseBody AjaxResult payByWeiXinOrZhiFuBao(String rechargeType, String paymentMethod, Integer subTotal,
			Boolean billing, Long menberUserId, HttpServletRequest req)
	{
		if (!"1".equals(rechargeType) && !"2".equals(rechargeType) && !"3".equals(rechargeType)
				&& !"4".equals(rechargeType) && !"5".equals(rechargeType) && !"6".equals(rechargeType))
		{
			return AjaxResult.errorInstance("没有选择充值类型");
		}

		if (!"1".equals(paymentMethod) && !"2".equals(paymentMethod))
		{
			return AjaxResult.errorInstance("付款方式必须为微信或者支付宝");
		}
		if (subTotal == null || subTotal <= 0)
		{
			return AjaxResult.errorInstance("充值条数必须大于0");
		}
		if (menberUserId == null || menberUserId <= 0)
		{
			return AjaxResult.errorInstance("充值会员记录id不能为空");
		}
		MenberUser menberUser = menberUserService.selectOne(menberUserId);
		if (menberUser == null)
		{
			return AjaxResult.errorInstance("会员不存在");
		}

		if (billing == null)
		{
			billing = false;
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		int price = 0;
		if(menberUser.getPrice()!=null)
		{
			price = menberUser.getPrice();
		}
		int settlementPrice = 0;
		if (menberUser.getSettlementPrice() != null)
		{
			settlementPrice = menberUser.getSettlementPrice();
		}
		int settlementPercent = 0;
		if (menberUser.getSettlementPercent() != null)
		{
			settlementPercent = menberUser.getSettlementPercent();
		}
		int pipelinePrice = 0;
		if (menberUser.getPipelinePrice() != null)
		{
			pipelinePrice = menberUser.getPipelinePrice();
		}

		IDUtils idUtils = new IDUtils(0,0);
		RechargeRecord rechargeRecord = new RechargeRecord();
		long rechargeRecordId = idUtils.nextId();
		rechargeRecord.setId(rechargeRecordId);
		rechargeRecord.setCouponId(0l);
		rechargeRecord.setMenberUserId(menberUserId);
		rechargeRecord.setOperateMenberUserId(0L);
		rechargeRecord.setOperateQianShangUserId(0l);
		rechargeRecord.setOperateAdminUserId(adminUser.getId());
		rechargeRecord.setOrderId(0L);
		rechargeRecord.setPrice(price);
		rechargeRecord.setSettlementPrice(settlementPrice);
		rechargeRecord.setSettlementPercent(settlementPercent);
		rechargeRecord.setPipelinePrice(pipelinePrice);
		rechargeRecord.setPaymentMethod(paymentMethod);
		rechargeRecord.setRechargeType(rechargeType);
		// rechargeType="1",购买；rechargeType!="1",
		rechargeRecord.setRepaymentStatus("1".equals(rechargeType) ? "2" : "0");
		rechargeRecord.setRepaymentTime(0l);
		rechargeRecord.setPaymentValue(subTotal * price);
		rechargeRecord.setRechargeCount(subTotal);
		rechargeRecord.setBilling(billing);
		rechargeRecord.setRechargePlateForm("2");
		rechargeRecord.setCreateTime(System.currentTimeMillis());
		rechargeRecord.setHandsel(0);
		rechargeRecord.setStatus("0");
		rechargeRecord.setRepaymentCertificate("");
		rechargeRecord.setFailReasonId(0l);
		rechargeRecord.setHavingLiquidation(0.0);// 已清算收益
		int smsBlance = 0;

		if (menberUser.getSmsBlance() != null)
		{
			smsBlance += menberUser.getSmsBlance() + subTotal;
		}
		int count = 0;
		if (menberUser.getCount() != null)
		{
			count += menberUser.getCount() + 1;
		}
		rechargeRecord.setSmsBlance(smsBlance);
		rechargeRecord.setCount(count);

		MenberUserSetting menberUserSetting = new MenberUserSetting();
		menberUserSetting.setMenberUserId(menberUserId);
		menberUserSetting = menberUserSettingService.selectOne(menberUserSetting);
		menberUserSetting.setSmsBlance(smsBlance);
		menberUserSetting.setCount(count);
		int totalRechargeCount = 0;
		if (menberUserSetting.getTotalRechargeCount() != null)
		{
			totalRechargeCount += menberUser.getTotalRechargeCount() + subTotal;
		}
		menberUserSetting.setTotalRechargeCount(totalRechargeCount);

		// 计算乾商收益，插入收益清算表
		int handsel = 0;
		int rebate = 0;
		// 计算个人收益
		double liquidation = (double) (((price - settlementPrice) * 0.001 * subTotal - handsel * settlementPrice * 0.001
				- rebate) * settlementPercent) * 0.01;
		// 计算管道收益
		double pipeLiquidation = pipelinePrice * 0.001 * subTotal;
		
		rechargeRecord.setLiquidationStatus(false);
		rechargeRecord.setTotalLiquidation(liquidation + pipeLiquidation);
		rechargeRecord.setLiquidationTime(0l);


		long qianShangUserId = 0L;
		Long qianShangUserTemp = menberUser.getQiangShangUserId();
		if (qianShangUserTemp != null && qianShangUserTemp != 0)
		{
			qianShangUserId = qianShangUserTemp;
		}
		
		QianShangUser qianShangUser = qianShangUserService.selectOne(qianShangUserId);
		long pipeQianShangUserId = 0L;
		Long pipeQianShangUserIdTemp = qianShangUser.getLeaderId();
		if (pipeQianShangUserIdTemp != null && pipeQianShangUserIdTemp != 0)
		{
			pipeQianShangUserId = pipeQianShangUserIdTemp;
		}

		// 设置乾商个人收益
		IncomeSettlement personIncomeSettlement = new IncomeSettlement();
		personIncomeSettlement.setId(idUtils.nextId());
		personIncomeSettlement.setCreateTime(System.currentTimeMillis());
		personIncomeSettlement.setHandsel(handsel);
		personIncomeSettlement.setLiquidation(liquidation);
		personIncomeSettlement.setLiquidationStatus(false);
		personIncomeSettlement.setQianShangUserId(qianShangUserId);
		personIncomeSettlement.setRebate(rebate);
		personIncomeSettlement.setRechargeRecordId(rechargeRecordId);
		personIncomeSettlement.setType("0");

		// 设置乾商管道收益
		IncomeSettlement pipeIncomeSettlement = new IncomeSettlement();
		pipeIncomeSettlement.setId(idUtils.nextId());
		pipeIncomeSettlement.setCreateTime(System.currentTimeMillis());
		pipeIncomeSettlement.setHandsel(handsel);
		pipeIncomeSettlement.setLiquidation(pipeLiquidation);
		pipeIncomeSettlement.setLiquidationStatus(false);
		pipeIncomeSettlement.setQianShangUserId(pipeQianShangUserId);
		pipeIncomeSettlement.setRebate(rebate);
		pipeIncomeSettlement.setRechargeRecordId(rechargeRecordId);
		pipeIncomeSettlement.setType("1");


		// 更新会员短信余额、充值次数
		rechargeRecordService.insertByAdminWeiXinOrZhiFuBao(rechargeRecord, menberUserSetting, personIncomeSettlement,
				pipeIncomeSettlement);

		return AjaxResult.successInstance("充值成功");
	}


	/*
	 * 加载会员账户信息
	 */
	@RequestMapping("/loadMenberUserItem.do")
	public @ResponseBody AjaxResult loadMenberUserItem(String accountName)
	{
		if (accountName == null)
		{
			return AjaxResult.errorInstance("账户名不能为空");
		}
		if (!CommonUtils.isPhone(accountName))
		{
			return AjaxResult.errorInstance("账户名必须为手机号");
		}
		// 查询出账户状态、真实姓名、手机号、授信额度、可用授信额度、账期、短信余额、最低充值标准、计费单价

		MenberUserItemForRecharge menberUserItemForRecharge = menberUserService.selectItemForRecharge(accountName);

		if (menberUserItemForRecharge == null)
		{
			return AjaxResult.errorInstance("账户信息不存在");
		}

		return AjaxResult.successInstance(menberUserItemForRecharge);

	}


}
