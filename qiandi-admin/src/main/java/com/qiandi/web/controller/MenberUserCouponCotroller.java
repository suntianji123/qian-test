package com.qiandi.web.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
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
import com.qiandi.dao.JDBCUtils;
import com.qiandi.dao.MenberUserCouponDAO;
import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.Coupon;
import com.qiandi.pojo.MenberUserCoupon;
import com.qiandi.pojo.MenberUserCouponData;
import com.qiandi.service.MenberUserCouponService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;
import com.qiandi.web.util.AdminUtils;

@Controller
@RequestMapping("/menberUserCoupon")
public class MenberUserCouponCotroller
{
	@Autowired
	private MenberUserCouponService menberUserCouponService;

	private MenberUserCouponDAO menberUserCouponDAO = new MenberUserCouponDAO();

	@RequestMapping("/useExportToExcel.do")
	public void useExportToExcel(Long useBeginTime, Long useEndTime, Long ids[], HttpServletResponse resp,
			HttpServletRequest req)
	{
		List<MenberUserCouponData> menberUserCouponList = new ArrayList<MenberUserCouponData>();
		if (ids != null && ids.length > 0)
		{
			menberUserCouponList = menberUserCouponService.selectUseByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("useBeginTime", useBeginTime);
			param.put("useEndTime", useEndTime);
			menberUserCouponList = menberUserCouponService.selectUseData(param);
		}

		ExportToExcelUtil<MenberUserCouponData> excelUtil = new ExportToExcelUtil<MenberUserCouponData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "收件地址统计表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "手机号", "会员账号级别", "所属乾商", "优惠券金额（元）", "使用限额（元）", "使用日期",
					"记录编码" };
			String[] columns = { "id", "menberUserAccountName", "menberUserName", "menberUserPhoneNum",
					"menberUserLevel", "qianShangUserName", "money", "fullUsed", "useTimeStr", "rechargeRecordId" };
			try
			{
				excelUtil.exportExcel(headers, columns, menberUserCouponList, out, req, "");
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

	@RequestMapping("/useSearch.do")
	public @ResponseBody AjaxResult useSearch(Long useBeginTime, Long useEndTime, Long id, Long rechargeRecordId,
			String menberUserAccountName, String menberUserName, String menberUserPhoneNum, String qianShangUserName,
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
		param.put("useBeginTime", useBeginTime);
		param.put("useEndTime", useEndTime);
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
		param.put("menberUserPhoneNum", CommonUtils.isEmpty(menberUserPhoneNum) ? null : menberUserPhoneNum);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
		param.put("id", id);
		param.put("rechargeRecordId", rechargeRecordId);

		PageInfo<MenberUserCouponData> pageInfo = menberUserCouponService.pageUseData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);

	}


	@RequestMapping("/addNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(Long menberUserIds[], String limitProduct, Integer money,
			Integer fullUsed,Long endTime,HttpServletRequest req)
	{
		if (CommonUtils.isEmpty(limitProduct))
		{
			return AjaxResult.errorInstance("限制商品类不能为空");
		}
		if (money == null || money <= 0)
		{
			return AjaxResult.errorInstance("优惠券金额不正确");
		}
		if (fullUsed == null || fullUsed <= 0)
		{
			return AjaxResult.errorInstance("使用限额不正确");
		}
		if (menberUserIds == null)
		{
			return AjaxResult.errorInstance("赠送对象不能为空");
		}
		
		menberUserCouponService.insertByList();
		MenberUserCoupon[] menberUserCoupons = new MenberUserCoupon[menberUserIds.length];
		IDUtils idUtils = new IDUtils(0, 0);
		Long couponId = idUtils.nextId();
		for (int i = 0; i < menberUserIds.length; i++)
		{
			MenberUserCoupon menberUserCoupon = new MenberUserCoupon();
			menberUserCoupon.setId(idUtils.nextId());
			menberUserCoupon.setMenberUserId(menberUserIds[i]);
			menberUserCoupon.setCouponId(couponId);
			menberUserCoupon.setStartTime(System.currentTimeMillis());
			menberUserCoupon.setEndTime(endTime);
			menberUserCoupon.setUserTime(System.currentTimeMillis());
			menberUserCoupon.setStatus("1");
			menberUserCoupon.setOrderId(0l);
			menberUserCoupon.setCreateTime(System.currentTimeMillis());
			menberUserCoupons[i] = menberUserCoupon;
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		
		Coupon coupon = new Coupon();
		coupon.setId(couponId);
		coupon.setLimitProduct(limitProduct);
		coupon.setMoney(money);
		coupon.setFullUsed(fullUsed);
		coupon.setNumber("");
		coupon.setAddAdminUserId(adminUser.getId());

		Connection conn = null;
		try
		{
			conn = AdminUtils.getConnection();
			menberUserCouponDAO.insertByArray(conn, menberUserCoupons, coupon);
		} catch (SQLException e)
		{
			throw new RuntimeException("获取数据库连接出错了", e);
		} finally
		{
			JDBCUtils.closeQuietly(conn);
		}
		return AjaxResult.successInstance("添加成功");
	}

	@RequestMapping("/sendExportToExcel.do")
	public void sendExportToExcel(Long beginTime, Long endTime, Long ids[],
			HttpServletResponse resp, HttpServletRequest req)
	{
		List<MenberUserCouponData> menberUserCouponList = new ArrayList<MenberUserCouponData>();
		if (ids != null && ids.length > 0)
		{
			menberUserCouponList = menberUserCouponService.selectSendByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			menberUserCouponList = menberUserCouponService.selectSendData(param);
		}

		ExportToExcelUtil<MenberUserCouponData> excelUtil = new ExportToExcelUtil<MenberUserCouponData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "收件地址统计表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "手机号", "会员账号级别", "所属乾商", "优惠券金额（元）", "使用限额（元）", "赠送日期",
					"有效截止日期", "使用状态", "创建人", "创建日期" };
			String[] columns = { "id", "menberUserAccountName", "menberUserName", "menberUserPhoneNum",
					"menberUserLevel", "qianShangUserName", "money", "fullUsed", "startTimeStr", "endTimeStr",
					"statusStr", "addAdminUserName", "createTimeStr" };
			try
			{
				excelUtil.exportExcel(headers, columns, menberUserCouponList, out, req, "");
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
	public @ResponseBody AjaxResult delete(Long[] ids, Long id)
	{
		if (ids != null && ids.length > 0)
		{
			menberUserCouponService.deleteByArray(ids);
		} else
		{
			if (id != null)
			{
				menberUserCouponService.delete(id);
			}
		}

		return AjaxResult.successInstance("删除成功！");
	}

	@RequestMapping("/sendSearch.do")
	public @ResponseBody AjaxResult sendSearch(Long beginTime, Long endTime, Long startBeginTime, Long startEndTime,
			Long endBeginTime, Long endEndTime, Long id, String menberUserAccountName,
			String menberUserName, String menberUserPhoneNum, String qianShangUserName, String status,
			String addAdminUserName, Integer pageNum,
			Integer pageSize)
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
		param.put("startBeginTime", startBeginTime);
		param.put("startEndTime", startEndTime);
		param.put("endBeginTime", endBeginTime);
		param.put("endEndTime", endEndTime);
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
		param.put("menberUserPhoneNum", CommonUtils.isEmpty(menberUserPhoneNum) ? null : menberUserPhoneNum);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
		param.put("addAdminUserName", CommonUtils.isEmpty(addAdminUserName) ? null : addAdminUserName);
		param.put("status", CommonUtils.isEmpty(status) ? null : status);
		param.put("id", id);
		
		PageInfo<MenberUserCouponData> pageInfo = menberUserCouponService.pageSendData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);
		
	}
}
