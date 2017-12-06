package com.qiandi.web.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.CreditApproval;
import com.qiandi.pojo.CreditApprovalData;
import com.qiandi.pojo.IdName;
import com.qiandi.pojo.MenberUserSettingCreditData;
import com.qiandi.pojo.MenberUserSettingCreditDataForUpdate;
import com.qiandi.service.CreditApprovalService;
import com.qiandi.service.IdNameService;
import com.qiandi.service.MenberUserSettingService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/creditApproval")
public class CreditApprovalCotroller
{
	@Autowired
	private CreditApprovalService creditApprovalService;

	@Autowired
	private MenberUserSettingService menberUserSettingService;

	@Autowired
	private IdNameService idNameService;

	/*
	 * 查询所有审批记录记录
	 */
	@RequestMapping("/searchResult.do")
	public @ResponseBody AjaxResult searchResult(Long id, String menberUserAccountName, String qianShangUserName,
			Long applyBeginTime, Long applyEndTime, Long reviewBeginTime, Long reviewEndTime, String phoneNum,
			Integer quotaBegin, Integer quotaEnd, Integer accountPeriodBegin, Integer accountPeriodEnd,
			String reviewStatus, Integer pageSize, Integer pageNum)
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
		param.put("id", id);
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
		param.put("reviewStatus", "2");
		param.put("reviewStatus", CommonUtils.isEmpty(reviewStatus) ? null : reviewStatus);
		param.put("accountPeriodBegin", accountPeriodBegin);
		param.put("accountPeriodEnd", accountPeriodEnd);
		param.put("quotaBegin", quotaBegin);
		param.put("quotaEnd", quotaEnd);
		param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
		param.put("applyBeginTime", applyBeginTime);
		param.put("applyEndTime", applyBeginTime);
		param.put("reviewBeginTime", reviewBeginTime);
		param.put("reviewEndTime", reviewEndTime);

		PageInfo<CreditApprovalData> pageInfo = creditApprovalService.pageData(pageNum, pageSize, param);

		return AjaxResult.successInstance(pageInfo);

	}

	/*
	 * 批量通过或拒绝
	 */
	@RequestMapping("/updateReviewStatus.do")
	public @ResponseBody AjaxResult updateReviewStatus(Long[] ids, String reviewStatus, Long failReasonId,HttpServletRequest req)
	{
		if (ids == null || ids.length <= 0)
		{
			return AjaxResult.errorInstance("没有选择将要审批的记录id");
		}

		if (!"1".equals(reviewStatus) && !"2".equals(reviewStatus))
		{
			return AjaxResult.errorInstance("审批结果值不正确");
		}

		// 更改为最新的授信额度
		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		long adminUserId = adminUser.getId();

		
		if ("1".equals(reviewStatus))
		{

			// 查询出原来的授信额度
			List<MenberUserSettingCreditDataForUpdate> list = creditApprovalService
					.selectMenberUserSettingCreditDataForUpdate(ids);

			// 更新授信额度的审核状态，更新配置表授信额度信息
			creditApprovalService.updateReviewStatusAndMenberUserSetting(ids, list, reviewStatus, adminUserId, 0l);
			

		} else if ("2".equals(reviewStatus))
		{
			// 拒绝
			if (failReasonId == null)
			{
				return AjaxResult.errorInstance("审核拒绝原因不合法");
			}

			// 拒绝原因是否合法
			IdName idName = new IdName();
			idName.setName("授信额度审核拒绝原因");
			idName.setId(failReasonId);
			if (!idNameService.isExisted(idName))
			{
				return AjaxResult.errorInstance("授信额度拒绝原因不合法");
			}
			
			creditApprovalService.updateReviewStatuByArray(ids, reviewStatus, adminUserId, failReasonId);
			
			
			
		}

		return AjaxResult.successInstance("审批成功");
	}

	/*
	 * 查询所有待审批记录
	 */
	@RequestMapping("/searchAudit.do")
	public @ResponseBody AjaxResult searchAudit(Long id, String menberUserAccountName, String qianShangUserName,
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
		param.put("id", id);
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
		param.put("reviewStatus", "0");

		PageInfo<CreditApprovalData> pageInfo = creditApprovalService.pageData(pageNum, pageSize, param);

		return AjaxResult.successInstance(pageInfo);

	}

	/*
	 * 加载账户信息
	 */
	@RequestMapping("/loadMenberUserItem.do")
	public @ResponseBody AjaxResult loadMenberUserItem(String menberUserAccountName)
	{
		if (CommonUtils.isEmpty(menberUserAccountName))
		{
			return AjaxResult.errorInstance("账户名不能为空");
		}

		MenberUserSettingCreditData menberUserSettingCreditData = menberUserSettingService
				.selectCreditItem(menberUserAccountName);
		if (menberUserSettingCreditData == null)
		{
			return AjaxResult.errorInstance("账户信息不存在");
		}

		return AjaxResult.successInstance(menberUserSettingCreditData);

	}

	/*
	 * 申请
	 */
	@RequestMapping("/applyOne.do")
	public @ResponseBody AjaxResult applyOne(Long menberUserSettingId, Integer quota, Integer accountPeriod,HttpServletRequest req)
	{
		if (menberUserSettingId == null)
		{
			return AjaxResult.errorInstance("用户记录不能为空");
		}
		if (quota == null || quota <= 0)
		{
			return AjaxResult.errorInstance("授信金额必须为整数");
		}
		if (accountPeriod == null || accountPeriod <= 0)
		{
			return AjaxResult.errorInstance("授信账期有误");
		}
		CreditApproval param = new CreditApproval();
		param.setMenberUserSettingId(menberUserSettingId);
		param.setReviewStatus("0");
		if (creditApprovalService.isExisted(param))
		{
			return AjaxResult.errorInstance("这个会员已经有一个待审批的授信额度，您不能重复申请");
		}

		// 查询出可用授信额度、原始授信额度、新授信额度>=原始授信额度-可用授信额度
		Map<String, Integer> result = menberUserSettingService.selectQuota(menberUserSettingId);
		if (CommonUtils.isEmpty(result))
		{
			return AjaxResult.errorInstance("会员记录不存在");
		}
		int oldQuota = 0;
		if (result.get("quota") != null)
		{
			oldQuota = Integer.valueOf(String.valueOf(result.get("quota")));
		}
		int availableQuota = 0;
		if (result.get("availableQuota") != null)
		{
			availableQuota = Integer.valueOf(String.valueOf(result.get("availableQuota")));
		}
		if (oldQuota != 0 && availableQuota != 0 && (quota * 1000 < (oldQuota - availableQuota)))
		{
			return AjaxResult.errorInstance("这个会员账户的最低授信额度值为" + (oldQuota - availableQuota) * 0.001 + "元");
		}
		
		

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");

		CreditApproval creditApproval = new CreditApproval();
		creditApproval.setId(new IDUtils(0, 0).nextId());
		creditApproval.setAccountPeriod(accountPeriod);
		creditApproval.setApplyAdminUserId(adminUser.getId());
		creditApproval.setApplyQianShangUserId(0l);
		creditApproval.setApplyTime(System.currentTimeMillis());
		creditApproval.setFailReasonId(0l);
		creditApproval.setMenberUserSettingId(menberUserSettingId);
		creditApproval.setPlateform(true);
		creditApproval.setQuota(quota * 1000);
		creditApproval.setOldQuota(oldQuota);
		creditApproval.setReviewAdminUserId(0l);
		creditApproval.setReviewQianShangUserId(0l);
		creditApproval.setReviewStatus("0");
		creditApproval.setReviewTime(System.currentTimeMillis());
		
		creditApprovalService.insert(creditApproval);
		
		return AjaxResult.successInstance("申请成功，请等待审核");

	}
}

