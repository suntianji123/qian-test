package com.qiandi.web.controller;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.qiandi.dao.RepaymentRecordDAO;
import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.IdName;
import com.qiandi.pojo.IncomeSettlement;
import com.qiandi.pojo.MenberUser;
import com.qiandi.pojo.QianShangUser;
import com.qiandi.pojo.RechargeRecord;
import com.qiandi.pojo.RechargeRecordForUpdate;
import com.qiandi.pojo.Repayment;
import com.qiandi.pojo.RepaymentRecord;
import com.qiandi.pojo.RepaymentRecordDataForAudit;
import com.qiandi.service.IdNameService;
import com.qiandi.service.MenberUserService;
import com.qiandi.service.QianShangUserService;
import com.qiandi.service.RechargeRecordService;
import com.qiandi.service.RepaymentRecordService;
import com.qiandi.service.RepaymentService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.IDUtils;
import com.qiandi.util.UploadUtils;
import com.qiandi.web.util.AdminUtils;

@Controller
@RequestMapping("/repaymentRecord")
public class RepaymentRecordController
{

	@Autowired
	private RepaymentService repaymentService;

	@Autowired
	private RepaymentRecordService repaymentRecordService;

	@Autowired
	private RechargeRecordService rechargeRecordService;

	@Autowired
	private QianShangUserService qianShangUserService;

	@Autowired
	private MenberUserService menberUserService;

	@Autowired
	private IdNameService idNameService;

	private RepaymentRecordDAO repaymentRecordDAO = new RepaymentRecordDAO();

	/*
	 * 批量对还款记录、对公、对私付款进行审核
	 */
	@RequestMapping("/auditRepayment.do")
	public @ResponseBody AjaxResult auidtRepayment(Long[] repaymentRecordIds, Long failReasonId, String repaymentStatus,HttpServletRequest req)
	{
		if (repaymentRecordIds == null || repaymentRecordIds.length <= 0)
		{
			return AjaxResult.errorInstance("被审核的还款记录id不能为空");
		}
		if (!"1".equals(repaymentStatus) && !"2".equals(repaymentStatus))
		{
			return AjaxResult.errorInstance("审核结果值不正确");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		long adminUserId = adminUser.getId();
		
		if ("1".equals(repaymentStatus))
		{
			// 审核失败
			if (failReasonId == null || failReasonId <= 0)
			{
				return AjaxResult.errorInstance("审核失败原因不能为空");
			}
			
			IdName idName = new IdName();
			idName.setId(failReasonId);
			idName.setName("还款审核拒绝原因");
			if (!idNameService.isExisted(idName))
			{
				return AjaxResult.errorInstance("拒绝原因不合法");
			}

			// 查询出这些记录的逾期天数对应的总滞纳金之和，同时设置等待审核的还款金额下降，修改repayment
			List<RepaymentRecordDataForAudit> repaymentRecordList = repaymentRecordService
					.selectListById(repaymentRecordIds);
			Set<Long> repaymentIdSet = new HashSet<Long>();
			// 计算对应repayment应还滞纳金总和
			Map<String, Integer> map = new HashMap<String, Integer>();
			if (!CommonUtils.isEmpty(repaymentRecordList))
			{
				for (RepaymentRecordDataForAudit repaymentRecordAudit : repaymentRecordList)
				{
					int overdueCountItem = repaymentRecordAudit.getOverdueCountItem();
					int overdueCountSum = repaymentRecordAudit.getOverdueCountSum();
					int percent = repaymentRecordAudit.getPercent();
					int percentOne = repaymentRecordAudit.getPercentOne();
					int percentTwo = repaymentRecordAudit.getPercentTwo();
					int percentThree = repaymentRecordAudit.getPercentThree();
					int paymentRepaymentCount = repaymentRecordAudit.getPaymentRepaymentCount();
					int shouldLateFee = 0;
					if (overdueCountItem <= 3)
					{
						if (overdueCountSum <= 3)
						{
							shouldLateFee = (int) ((overdueCountSum - overdueCountItem) * percent * 0.01
									* paymentRepaymentCount);
						} else if (overdueCountSum > 3 && overdueCountSum <= 6)
						{
							shouldLateFee = (int) (((overdueCountSum - 3) * percent
									+ (3 - overdueCountItem) * percentOne) * 0.01 * paymentRepaymentCount);
						} else if (overdueCountSum > 6 && overdueCountSum <= 9)
						{
							shouldLateFee = (int) (((overdueCountSum - 6) * percent + 3 * percentTwo
									+ (3 - overdueCountItem) * percentOne) * 0.01 * paymentRepaymentCount);
						} else if (overdueCountSum > 9)
						{
							shouldLateFee = (int) (((overdueCountSum - 9) * percent + 3 * percentThree + 3 * percentTwo
									+ (3 - overdueCountItem) * percentOne) * 0.01 * paymentRepaymentCount);
						}

					} else if (overdueCountItem > 3 && overdueCountItem <= 6)
					{
						if (overdueCountSum > 3 && overdueCountSum <= 6)
						{
							shouldLateFee = (int) ((overdueCountSum - overdueCountItem) * percent * 0.01
									* paymentRepaymentCount);
						} else if (overdueCountSum > 6 && overdueCountSum <= 9)
						{
							shouldLateFee = (int) (((overdueCountSum - 6) * percent
									+ (6 - overdueCountItem) * percentTwo) * 0.01 * paymentRepaymentCount);
						} else if (overdueCountSum > 9)
						{
							shouldLateFee = (int) (((overdueCountSum - 9) * percent + 3 * percentThree
									+ (6 - overdueCountItem) * percentTwo) * 0.01
									* paymentRepaymentCount);
						}

					} else if (overdueCountItem > 6 && overdueCountItem <= 9)
					{

						if (overdueCountSum > 6 && overdueCountSum <= 9)
						{
							shouldLateFee = (int) (((overdueCountSum - overdueCountItem) * percent) * 0.01
									* paymentRepaymentCount);
						} else if (overdueCountSum > 9)
						{
							shouldLateFee = (int) (((overdueCountSum - 9) * percent
									+ (9 - overdueCountItem) * percentThree) * 0.01 * paymentRepaymentCount);
						}

					} else if (overdueCountItem > 9)
					{
						shouldLateFee = (int) (((overdueCountSum - overdueCountItem) * percent) * 0.01
								* paymentRepaymentCount);
					}


					long repaymentId = repaymentRecordAudit.getRepaymentId();
					repaymentIdSet.add(repaymentId);
					String key1 = repaymentId + "_waitAuditRepaymentCount";
					String key2 = repaymentId + "_waitAuditLateFeeCount";
					String key3 = repaymentId + "_cutHavingLateFeeCount";
					if (!map.containsKey(key1)&&!map.containsKey(key2))
					{
						map.put(key1, 0);
						map.put(key2, 0);
						map.put(key3, 0);
					}
					map.put(key1, map.get(key1) + paymentRepaymentCount);
					map.put(key2, map.get(key2) + shouldLateFee );
					map.put(key3, map.get(key3) + repaymentRecordAudit.getPaymentLateFeeCount());
				}
			}

			if (CommonUtils.isEmpty(repaymentIdSet))
			{
				return AjaxResult.errorInstance("没有对应的充值记录");
			}

			Long[] repaymentIds = new Long[repaymentIdSet.size()];
			Iterator<Long> iterator = repaymentIdSet.iterator();
			int i = 0;
			while (iterator.hasNext())
			{
				long id = iterator.next();
				repaymentIds[i++] = id;
			}

			// 查询处所有repayment
			List<Repayment> repaymentList = repaymentService.selectListByIds(repaymentIds);

			for (Map.Entry<String, Integer> entry : map.entrySet())
			{
				String key = entry.getKey();
				Integer value = entry.getValue();
				long repaymentId = Long.parseLong(key.substring(0, key.lastIndexOf("_")));
				Repayment repayment = new Repayment();
				repayment.setId(repaymentId);
				if (repaymentList.contains(repayment))
				{
					int index = repaymentList.indexOf(repayment);
					repayment = repaymentList.get(index);
					if (key.endsWith("waitAuditRepaymentCount"))
					{
						repayment.setWaitAuditRepaymentCount(repayment.getWaitAuditRepaymentCount() - value);
						repayment.setRemainingRepaymentCount(repayment.getRemainingRepaymentCount() + value);
					} else if (key.endsWith("waitAuditLateFeeCount"))
					{
						// repayment.setWaitAuditLateFeeCount(repayment.getWaitAuditLateFeeCount()
						// - value);
						repayment.setRemainingLateFeeCount(repayment.getRemainingLateFeeCount() + value);
						repayment.setTotalLateFeeCount(repayment.getTotalLateFeeCount() + value);
					} else if (key.endsWith("cutHavingLateFeeCount"))
					{
						repayment.setRemainingLateFeeCount(repayment.getRemainingLateFeeCount() + value);
						// repayment.setTotalLateFeeCount(repayment.getTotalLateFeeCount()
						// - value);
						repayment.setHavingLateFeeCount(repayment.getHavingLateFeeCount() - value);
					}

					repaymentList.set(index, repayment);
				}


			}

			// 更新还款记录，审核结果为失败
			repaymentRecordService.updateRepaymentStatus(repaymentRecordIds, repaymentStatus, failReasonId,
					adminUserId, repaymentList);

		} else if ("2".equals(repaymentStatus))
		{
			// 审核通过、更新状态、计算收益

			// 1.查询出这些审核记录对应的充值记录
			List<RechargeRecordForUpdate> rechargeRecordList = rechargeRecordService
					.selectListByRepaymentRecordId(repaymentRecordIds);
			List<RepaymentRecordDataForAudit> repaymentRecordList = repaymentRecordService
					.selectListById(repaymentRecordIds);
			List<Repayment> repaymentList = repaymentService.selectListByRepaymentRecordId(repaymentRecordIds);
			List<IncomeSettlement> incomeSettlementList = new ArrayList<IncomeSettlement>();
			// 查询处所有repayment

			if (CommonUtils.isEmpty(rechargeRecordList))
			{
				return AjaxResult.errorInstance("没有对应的充值记录");
			}

			IDUtils idUtils = new IDUtils(0, 0);
			// RechargeRecordForUpdate rechargeRecord =
			// rechargeRecordList.get(i);

				for (int j = 0; j < repaymentRecordList.size(); j++)
				{
				RepaymentRecordDataForAudit repaymentRecord = repaymentRecordList.get(j);
				RechargeRecordForUpdate rechargeRecord = new RechargeRecordForUpdate();
				rechargeRecord.setRepaymentId(repaymentRecord.getRepaymentId());
				int index1 = 0;

				for (int i = 0; i < rechargeRecordList.size(); i++)
				{
					RechargeRecordForUpdate param = rechargeRecordList.get(i);
					if (rechargeRecord.getRepaymentId().equals(param.getRepaymentId()))
					{
						index1 = rechargeRecordList.indexOf(param);
						rechargeRecord = param;
					}
				}

					long repaymentId = repaymentRecord.getRepaymentId();
					Repayment repayment = new Repayment();
					repayment.setId(repaymentId);
					if (repaymentList.contains(repayment))
					{
						int index = repaymentList.indexOf(repayment);
						repayment = repaymentList.get(index);
						repayment.setWaitAuditRepaymentCount(
								repayment.getWaitAuditRepaymentCount() - repaymentRecord.getPaymentRepaymentCount());
						if (repayment.getWaitAuditRepaymentCount().equals(0)
								&& repayment.getRemainingRepaymentCount().equals(0))
						{
							repayment.setActualRepaymentTime(System.currentTimeMillis());
							repayment.setRepaymentResult(true);
						}

						repaymentList.set(index, repayment);
					}

					// 计算乾商收益，插入收益清算表
					int handsel = 0;
					int rebate = 0;
					int paymentValue = rechargeRecord.getPaymentValue();
					int rechargeCount = rechargeRecord.getRechargeCount();
					int price = rechargeRecord.getPrice();
					int settlementPrice = rechargeRecord.getSettlementPrice();
					int settlementPercent = rechargeRecord.getSettlementPercent();
					int pipelinePrice = rechargeRecord.getPipelinePrice();
					long rechargeRecordId = rechargeRecord.getId();
					int paymentRepaymentCount = repaymentRecord.getPaymentRepaymentCount();
					// 计算个人收益
					double liquidation = ((double) paymentRepaymentCount / paymentValue) * rechargeCount
							* (price - settlementPrice) * settlementPercent * 0.01 * 0.001;
					// 计算管道收益
					double pipeLiquidation = pipelinePrice * 0.001 * ((double) paymentRepaymentCount / paymentValue)
							* rechargeCount;

					double havingLiquidation = pipeLiquidation + liquidation + rechargeRecord.getHavingLiquidation();
					double totalLiquidation = rechargeRecord.getTotalLiquidation();
				rechargeRecord.setLiquidationStatus(false);
					rechargeRecord.setLiquidationTime(0l);
					rechargeRecord.setHavingLiquidation(havingLiquidation);
					rechargeRecord.setLiquidationTime(System.currentTimeMillis());
					if (havingLiquidation >= totalLiquidation)
					{
						rechargeRecord.setLiquidationStatus(true);
					}

					long qianShangUserId = rechargeRecord.getQianShangUserId();

					long pipeQianShangUserId = rechargeRecord.getQianShangUserLeaderId();

					// 设置乾商个人收益

					IncomeSettlement personIncomeSettlement = new IncomeSettlement();
					personIncomeSettlement.setId(idUtils.nextId());
					personIncomeSettlement.setCreateTime(System.currentTimeMillis());
					personIncomeSettlement.setHandsel(handsel);
					personIncomeSettlement.setLiquidation(liquidation);
				personIncomeSettlement.setLiquidationStatus(true);
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
				pipeIncomeSettlement.setLiquidationStatus(true);
					pipeIncomeSettlement.setQianShangUserId(pipeQianShangUserId);
					pipeIncomeSettlement.setRebate(rebate);
					pipeIncomeSettlement.setRechargeRecordId(rechargeRecordId);
					pipeIncomeSettlement.setType("1");

					incomeSettlementList.add(personIncomeSettlement);
					incomeSettlementList.add(pipeIncomeSettlement);

				if (index1 != 0)
				{
					rechargeRecordList.set(index1, rechargeRecord);
				}

				}




			repaymentRecordService.insertNull();
			// 批量插入收益、更改还款审核状态、批量更改充值记录的
			Connection conn;
			try
			{
				conn = AdminUtils.getConnection();
				repaymentRecordDAO.updateAndInsert(conn, repaymentRecordIds, repaymentStatus, adminUserId,
						rechargeRecordList, incomeSettlementList, repaymentList);
			} catch (SQLException e)
			{
				throw new RuntimeException(e);
			}

		}

		return AjaxResult.successInstance("审批成功");
	}

	@RequestMapping("/addNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(String paymentMethod, Integer paymentRepaymentCount,
			Long repaymentId, HttpServletRequest req)
	{
		if (repaymentId == null)
		{
			return AjaxResult.errorInstance("还款记录id不能为空");
		}
		if (!"1".equals(paymentMethod) && !"2".equals(paymentMethod) && !"3".equals(paymentMethod)
				&& !"4".equals(paymentMethod))
		{
			return AjaxResult.errorInstance("付款方式不正确");
		}
		if (paymentRepaymentCount == null || paymentRepaymentCount <= 0)
		{
			return AjaxResult.errorInstance("还款金额不能为空");
		}
		
		Repayment repayment = repaymentService.selectOne(repaymentId);
		if (repayment == null)
		{
			return AjaxResult.errorInstance("还款记录不能为空");
		}
		if (repayment.getRemainingRepaymentCount().equals(0) || repayment.getRepaymentResult())
		{
			return AjaxResult.errorInstance("这个充值记录已经还清，您不能再次还款");
		}
		
		int percent = repayment.getPercent();
		int percentOne = repayment.getPercentOne();
		int percentTwo = repayment.getPercentTwo();
		int percentThree = repayment.getPercentThree();
		int overdueCount = repayment.getOverdueCount();
		int shouldLateFee = 0;
		if (overdueCount <= 3)
		{
			shouldLateFee = (int) (overdueCount * percent * 0.01 * paymentRepaymentCount);
		} else if (overdueCount > 3 && overdueCount <= 6)
		{
			shouldLateFee = (int) (((overdueCount - 3) * percent + 3 * percentOne) * 0.01 * paymentRepaymentCount);
		} else if (overdueCount > 6 && overdueCount <= 9)
		{
			shouldLateFee = (int) (((overdueCount - 6) * percent + 3 * percentOne + 3 * percentTwo) * 0.01
					* paymentRepaymentCount);
		} else if (overdueCount > 9)
		{
			shouldLateFee = (int) (((overdueCount - 9) * percent + 3 * percentOne + 3 * percentTwo + 3 * percentThree)
					* 0.01 * paymentRepaymentCount);
		}
		
		// 插入到还款记录，更新剩余滞纳金、已付滞纳金、剩余待还款、插入到收益清算表、计算已清算收益

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		// 微信或支付宝付款
		RepaymentRecord repaymentRecord = new RepaymentRecord();
		IDUtils idUtils = new IDUtils(0, 0);
		repaymentRecord.setId(idUtils.nextId());
		repaymentRecord.setCreateTime(System.currentTimeMillis());
		repaymentRecord.setOperateAdminUserId(adminUser.getId());
		repaymentRecord.setOperateQianShangUserId(0l);
		repaymentRecord.setFailReasonId(0l);
		repaymentRecord.setPaymentLateFeeCount(shouldLateFee);
		repaymentRecord.setPaymentMethod(paymentMethod);
		repaymentRecord.setPaymentRepaymentCount(paymentRepaymentCount);
		repaymentRecord.setRepaymentId(repaymentId);
		repaymentRecord.setReviewAdminUserId(0l);
		repaymentRecord.setReviewTime(0l);
		repaymentRecord.setOverdueCount(overdueCount);

		if ("1".equals(paymentMethod) || "2".equals(paymentMethod))
		{
			repaymentRecord.setRepaymentStatus("2");
			repaymentRecord.setRepaymentCertificate("");
			repayment.setRemainingRepaymentCount(repayment.getRemainingRepaymentCount() - paymentRepaymentCount);

		} else
		{	
			repaymentRecord.setRepaymentStatus("0");
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
			if (!fileName.contains("."))
			{
				return AjaxResult.errorInstance("上传的文件格式不正确");
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
			repaymentRecord.setRepaymentCertificate(savaImgPath);

			// 对公付款、对私付款，设置待审核的还款的滞纳金总数、总还款金额
			repayment.setWaitAuditRepaymentCount(repayment.getWaitAuditRepaymentCount() + paymentRepaymentCount);
			repayment.setWaitAuditLateFeeCount(repayment.getWaitAuditLateFeeCount()+shouldLateFee);
			repayment.setRemainingRepaymentCount(repayment.getRemainingRepaymentCount() - paymentRepaymentCount);

		}

		// 更新滞纳金、剩余待还款
		repayment.setRemainingLateFeeCount(repayment.getRemainingLateFeeCount() - shouldLateFee);
		repayment.setHavingLateFeeCount(repayment.getHavingLateFeeCount() + shouldLateFee);
		

		if(repayment.getRemainingRepaymentCount()<=0)
		{
			repayment.setRepaymentResult(true);
		}

		// 插入到收益清算记录
		RechargeRecord rechargeRecord = rechargeRecordService.selectOne(repayment.getRechargeRecordId());
		if (rechargeRecord == null)
		{
			return AjaxResult.errorInstance("充值记录不存在");
		}
		
		MenberUser menberUser = menberUserService.selectOne(rechargeRecord.getMenberUserId());
		
		// 计算乾商收益，插入收益清算表
		int handsel = 0;
		int rebate = 0;
		int paymentValue = rechargeRecord.getPaymentValue();
		int rechargeCount = rechargeRecord.getRechargeCount();
		int price = rechargeRecord.getPrice();
		int settlementPrice = rechargeRecord.getSettlementPrice();
		int settlementPercent = rechargeRecord.getSettlementPercent();
		int pipelinePrice = rechargeRecord.getPipelinePrice();
		long rechargeRecordId = rechargeRecord.getId();
		// 计算个人收益
		double liquidation = ((double) paymentRepaymentCount / paymentValue) * rechargeCount * (price - settlementPrice)
				* settlementPercent * 0.01 * 0.001;
		// 计算管道收益
		double pipeLiquidation = pipelinePrice * 0.001 * ((double) paymentRepaymentCount / paymentValue)
				* rechargeCount;

		double havingLiquidation = pipeLiquidation + liquidation + rechargeRecord.getHavingLiquidation();
		double totalLiquidation = rechargeRecord.getTotalLiquidation();
		rechargeRecord.setLiquidationStatus(false);
		rechargeRecord.setLiquidationTime(0l);
		rechargeRecord.setHavingLiquidation(havingLiquidation);
		rechargeRecord.setLiquidationTime(System.currentTimeMillis());
		if (havingLiquidation >= totalLiquidation)
		{
			rechargeRecord.setLiquidationStatus(true);
		}

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
		personIncomeSettlement.setLiquidationStatus(true);
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
		pipeIncomeSettlement.setLiquidationStatus(true);
		pipeIncomeSettlement.setQianShangUserId(pipeQianShangUserId);
		pipeIncomeSettlement.setRebate(rebate);
		pipeIncomeSettlement.setRechargeRecordId(rechargeRecordId);
		pipeIncomeSettlement.setType("1");
		
		
		
		


		repaymentRecordService.insertAndUpdateRepaymentItem(paymentMethod, repaymentRecord, repayment, rechargeRecord,
				personIncomeSettlement, pipeIncomeSettlement);
		
		
		
		
		
		return AjaxResult.successInstance("还款成功");
	}
}
