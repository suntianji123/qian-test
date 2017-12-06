package com.qiandi.web.controller;

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

import com.github.pagehelper.PageInfo;
import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.QianShangUser;
import com.qiandi.pojo.QianShangUserData;
import com.qiandi.pojo.QianShangUserSetting;
import com.qiandi.service.QianShangUserService;
import com.qiandi.service.QianShangUserSettingService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/qianShangUser")
public class QianShangUserController
{
	@Autowired
	private QianShangUserService qianShangUserService;

	@Autowired
	private QianShangUserSettingService qianShangUserSettingService;

	@RequestMapping("/exportToExcel.do")
	public void exportToExcel(Long beginTime, Long endTime, String accountName, String name,
			String phoneNum, String leaderName, String addQianShangUserName, Integer levelOneUserCountBegin,
			Integer levelOneUserCountEnd, String accountStatus, String province, String city, Long[] ids,
			HttpServletRequest req, HttpServletResponse resp)
	{
		List<QianShangUserData> qianShangUserList = new ArrayList<QianShangUserData>();
		if (ids != null && ids.length > 0)
		{
			qianShangUserList = qianShangUserService.selectUserByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("accountName", (CommonUtils.isEmpty(accountName)) ? null : accountName);
			param.put("name", (CommonUtils.isEmpty(name)) ? null : name);
			param.put("phoneNum", (CommonUtils.isEmpty(phoneNum)) ? null : phoneNum);
			param.put("leaderName", (CommonUtils.isEmpty(leaderName)) ? null : leaderName);
			param.put("addQianShangUserName",
					(CommonUtils.isEmpty(addQianShangUserName)) ? null : addQianShangUserName);
			param.put("levelOneUserCountBegin", levelOneUserCountBegin);
			param.put("levelOneUserCountEnd", levelOneUserCountEnd);
			param.put("accountStatus", (CommonUtils.isEmpty(accountStatus)) ? null : accountStatus);
			param.put("province", (CommonUtils.isEmpty(province)) ? null : province);
			param.put("city", (CommonUtils.isEmpty(city)) ? null : city);
			qianShangUserList = qianShangUserService.selectData(param);

		}

		ExportToExcelUtil<QianShangUserData> excelUtil = new ExportToExcelUtil<QianShangUserData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "乾商会员统计表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "手机号", "上一级乾商", "乾商账户级别", "注册日期", "操作人", "账户状态", "会员数量",
					"一级会员数量", "结算单价", "所在地区" };
			String[] columns = { "id", "accountName", "name", "phoneNum", "leaderName", "level", "createTimeStr",
					"addUserName", "accountStatusStr", "userCount", "levelOneUserCount", "price", "address" };
			try
			{
				excelUtil.exportExcel(headers, columns, qianShangUserList, out, req, "");
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
	@RequestMapping("/delete.do")
	public @ResponseBody AjaxResult delete(Long[] ids)
	{
		if (ids == null || ids.length <= 0)
		{
			return AjaxResult.errorInstance("需要被删除的记录id不能为空");
		}

		qianShangUserService.deleteTwo(ids);

		return AjaxResult.successInstance("删除成功");
	}

	@RequestMapping("/updateSubmit.do")
	public @ResponseBody AjaxResult updateSubmit(Long id, String phoneNum, String name, String password,
			String repassword, Integer price, HttpServletRequest req)
	{
		if (!CommonUtils.isPhone(phoneNum))
		{
			return AjaxResult.errorInstance("手机号格式不正确");
		}
		if (!CommonUtils.isLengthEnough(password, 6))
		{
			return AjaxResult.errorInstance("密码长度不够6");
		}
		if (!password.equals(repassword))
		{
			return AjaxResult.errorInstance("两次输入的密码不一致");
		}
		if (price == null || price <= 0)
		{
			return AjaxResult.errorInstance("结算单价必须大于0");
		}
		if (CommonUtils.isEmpty(name))
		{
			return AjaxResult.errorInstance("真实姓名不能为空");
		}

		QianShangUser qianShangUser1 = qianShangUserService.selectOne(id);
		if (qianShangUser1 == null)
		{
			return AjaxResult.errorInstance("乾商用户不存在");
		}

		QianShangUser qianShangUser2 = new QianShangUser();
		qianShangUser2.setPhoneNum(phoneNum);
		qianShangUser2 = qianShangUserService.selectOne(qianShangUser2);
		if (qianShangUser2 != null && !qianShangUser2.getPhoneNum().equals(qianShangUser1.getPhoneNum()))
		{
			return AjaxResult.errorInstance("乾商用户已经存在，您不能将手机号更改为这个号码");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		qianShangUser1.setPhoneNum(phoneNum);
		qianShangUser1.setAccountName(phoneNum);
		String passwordSalt = UUID.randomUUID().toString();
		qianShangUser1.setPasswordSalt(passwordSalt);
		qianShangUser1.setPassword(CommonUtils.calculateMD5(passwordSalt + password));
		qianShangUser1.setCreateTime(System.currentTimeMillis());
		qianShangUser1.setAddQianShangUserId(0l);
		qianShangUser1.setAddAdminUserId(adminUser.getId());
		qianShangUser1.setPrice(price);
		qianShangUser1.setName(name);

		qianShangUserService.update(qianShangUser1);

		return AjaxResult.successInstance("修改成功");

	}

	// ToDo:找出一级会员的数量和我的会员的总数
	@RequestMapping("/addNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(String phoneNum, String name, String password, String repassword,
			Integer price, String leaderPhoneNum, String province, String city, HttpServletRequest req)
	{
		if (!CommonUtils.isPhone(phoneNum))
		{
			return AjaxResult.errorInstance("手机号格式不正确");
		}
		if (CommonUtils.isEmpty(name))
		{
			return AjaxResult.errorInstance("真实姓名不能为空");
		}
		if (!CommonUtils.isLengthEnough(password, 6))
		{
			return AjaxResult.errorInstance("密码长度不够");
		}
		if (!password.equals(repassword))
		{
			return AjaxResult.errorInstance("两次输入的密码长度不一致");
		}
		if (price == null || price < 0)
		{
			return AjaxResult.errorInstance("结算单价不能为空或者数值小于0");
		}
		if (!CommonUtils.isEmpty(leaderPhoneNum) && !CommonUtils.isPhone(leaderPhoneNum))
		{
			return AjaxResult.errorInstance("上一级乾商的手机号不正确");
		}
		if (CommonUtils.isEmpty(province))
		{
			return AjaxResult.errorInstance("省不能为空");
		}
		if (CommonUtils.isEmpty(city))
		{
			return AjaxResult.errorInstance("市不能为空");
		}
		
		// 唯一性检查
		QianShangUser qianShangUser = new QianShangUser();
		qianShangUser.setPhoneNum(phoneNum);
		if (qianShangUserService.isExisted(qianShangUser))
		{
			return AjaxResult.errorInstance("乾商用户已经存在,不能重复添加");
		}

		Long leaderId = 0l;
		int level = 0;
		if (!CommonUtils.isEmpty(leaderPhoneNum) && CommonUtils.isPhone(leaderPhoneNum))
		{
			qianShangUser = new QianShangUser();
			qianShangUser.setPhoneNum(leaderPhoneNum);
			qianShangUser.setManagerId(0l);
			qianShangUser = qianShangUserService.selectOne(qianShangUser);
			if (qianShangUser == null)
			{
				return AjaxResult.errorInstance("上一级乾商不存在");
			} else
			{
				leaderId = qianShangUser.getId();
				level = qianShangUser.getLevel() + 1;
			}
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		IDUtils idUtils = new IDUtils(0,0);
		long qianShangUserId = idUtils.nextId();
		
		qianShangUser = new QianShangUser();
		qianShangUser.setId(qianShangUserId);
		qianShangUser.setAccountName(phoneNum);
		qianShangUser.setLeaderId(leaderId);
		qianShangUser.setManagerId(0l);
		qianShangUser.setAddQianShangUserId(0l);
		qianShangUser.setAddAdminUserId(adminUser.getId());
		qianShangUser.setLevel(level);
		qianShangUser.setPhoneNum(phoneNum);
		qianShangUser.setName(name);
		String passwordSalt = UUID.randomUUID().toString();
		qianShangUser.setPasswordSalt(passwordSalt);
		qianShangUser.setPassword(CommonUtils.calculateMD5(passwordSalt + password));
		qianShangUser.setDepartment("");
		qianShangUser.setPosition("");
		qianShangUser.setQqNum("");
		qianShangUser.setEmail("");
		qianShangUser.setWeiXinNum("");
		qianShangUser.setCreateTime(System.currentTimeMillis());
		qianShangUser.setPrice(price);
		
		long qianShangUserSettingId = idUtils.nextId();
		QianShangUserSetting qianShangUserSetting = new QianShangUserSetting();
		qianShangUserSetting.setId(qianShangUserSettingId);
		qianShangUserSetting.setQianShangUserId(qianShangUserId);
		qianShangUserSetting.setAccountStatus("0");
		qianShangUserSetting.setLevelOneUserCount(0);
		qianShangUserSetting.setUserCount(0);
		qianShangUserSetting.setThisMonthLevelOneUserCount(0);
		qianShangUserSetting.setThisMonthUserCount(0);
		qianShangUserSetting.setAddress(province + city);

		// 更新上一级乾商的统计数据
		QianShangUserSetting leaderQianShangUserSetting = new QianShangUserSetting();
		leaderQianShangUserSetting.setQianShangUserId(leaderId);
		leaderQianShangUserSetting = qianShangUserSettingService.selectOne(leaderQianShangUserSetting);
		if (leaderQianShangUserSetting != null)
		{
			leaderQianShangUserSetting.setUserCount(leaderQianShangUserSetting.getUserCount() + 1);
			if(level==1)
			{
				leaderQianShangUserSetting.setLevelOneUserCount(leaderQianShangUserSetting.getLevelOneUserCount() + 1);
			}
		}

		qianShangUserService.insertAndUpdate(qianShangUser, qianShangUserSetting, leaderQianShangUserSetting);


		return AjaxResult.successInstance("添加成功");
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, String accountName, String name,
			String phoneNum, String leaderName, String addQianShangUserName, Integer levelOneUserCountBegin,
			Integer levelOneUserCountEnd, String accountStatus, String province, String city, Integer pageNum,
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
		param.put("accountName", (CommonUtils.isEmpty(accountName)) ? null : accountName);
		param.put("name", (CommonUtils.isEmpty(name)) ? null : name);
		param.put("phoneNum", (CommonUtils.isEmpty(phoneNum)) ? null : phoneNum);
		param.put("leaderName", (CommonUtils.isEmpty(leaderName)) ? null : leaderName);
		param.put("addQianShangUserName", (CommonUtils.isEmpty(addQianShangUserName)) ? null : addQianShangUserName);
		param.put("levelOneUserCountBegin", levelOneUserCountBegin);
		param.put("levelOneUserCountEnd", levelOneUserCountEnd);
		param.put("accountStatus", (CommonUtils.isEmpty(accountStatus)) ? null : accountStatus);
		param.put("province", (CommonUtils.isEmpty(province)) ? null : province);
		param.put("city", (CommonUtils.isEmpty(city)) ? null : city);
		
		PageInfo<QianShangUserData> pageInfo = qianShangUserService.pageData(pageNum, pageSize, param);

		return AjaxResult.errorInstance(pageInfo);

	}
}
