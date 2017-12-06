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
import com.qiandi.pojo.AdminUserManagerData;
import com.qiandi.service.AdminUserService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/adminUser")
public class AdminUserController
{
	@Autowired
	private AdminUserService adminUserService;

	@RequestMapping("/managerUpdateSubmit.do")
	public @ResponseBody AjaxResult updateSubmit(Long id, String accountName, String password, String name,
			String phoneNum, String department, String position, String email, String qqNum, String weiXinNum,
			HttpServletRequest req)
	{
		if (id == null)
		{
			return AjaxResult.errorInstance("记录ID不能为空");
		}
		if (CommonUtils.isEmpty(accountName))
		{
			return AjaxResult.errorInstance("账户名不能为空");
		}
		if (!CommonUtils.isLengthEnough(password, 6))
		{
			return AjaxResult.errorInstance("密码长度不够");
		}
		if (CommonUtils.isEmpty(name))
		{
			return AjaxResult.errorInstance("真实姓名不能为空");
		}
		if (!CommonUtils.isPhone(phoneNum))
		{
			return AjaxResult.errorInstance("手机号格式不正确");
		}
		if (CommonUtils.isEmpty(department))
		{
			return AjaxResult.errorInstance("不能不能为空");
		}
		if (CommonUtils.isEmpty(position))
		{
			return AjaxResult.errorInstance("职位不能为空");
		}
		if (CommonUtils.isEmpty(qqNum))
		{
			return AjaxResult.errorInstance("qq号不能为空");
		}
		if (CommonUtils.isEmpty(weiXinNum))
		{
			return AjaxResult.errorInstance("微信号不能为空");
		}
		if (CommonUtils.isEmpty(email))
		{
			return AjaxResult.errorInstance("邮箱不能为空");
		}

		AdminUser adminUser = new AdminUser();
		adminUser = adminUserService.selectOne(id);
		if (adminUser == null)
		{
			return AjaxResult.errorInstance("操作员不存在");
		}
		
		AdminUser adminUser1 = new AdminUser();
		adminUser1.setPhoneNum(phoneNum);
		adminUser1 = adminUserService.selectOne(adminUser1);
		if (adminUser1 != null && !adminUser1.getPhoneNum().equals(adminUser.getPhoneNum()))
		{
			return AjaxResult.errorInstance("手机号已经存在，您不能更改为这个手机号！");
		}
		// 唯一性检查

		AdminUser au = (AdminUser) req.getSession().getAttribute("adminUser");

		adminUser.setAccountName(accountName);
		adminUser.setAccountName(phoneNum);
		adminUser.setName(name);
		adminUser.setManagerId(au.getId());
		String passwordSalt = UUID.randomUUID().toString();
		adminUser.setPasswordSalt(passwordSalt);
		adminUser.setPassword(CommonUtils.calculateMD5(passwordSalt + password));
		adminUser.setDepartment(department);
		adminUser.setPosition(position);
		adminUser.setEmail(email);
		adminUser.setQqNum(qqNum);
		adminUser.setWeiXinNum(weiXinNum);
		adminUser.setCreateTime(System.currentTimeMillis());

		adminUserService.update(adminUser);

		return AjaxResult.successInstance("修改成功！");
	}

	@RequestMapping("/managerExportToExcel.do")
	public void managerExportToExcel(HttpServletRequest req, HttpServletResponse resp, Long[] ids,
			Long beginTime, Long endTime, Long id, String accountName, String name, String phoneNum, String managerName)
	{
		List<AdminUserManagerData> adminUserList = new ArrayList<AdminUserManagerData>();
		if (ids != null && ids.length > 0)
		{
			adminUserList = adminUserService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("accountName", CommonUtils.isEmpty(accountName) ? null : accountName);
			param.put("name", CommonUtils.isEmpty(name) ? null : name);
			param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
			param.put("id", id);
			param.put("managerName", CommonUtils.isEmpty(managerName) ? null : managerName);
			adminUserList = adminUserService.selectManagerData(param);
		}

		ExportToExcelUtil<AdminUserManagerData> excelUtil = new ExportToExcelUtil<AdminUserManagerData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "收件地址统计表");
			String[] headers = { "记录ID", "登录名", "真实姓名", "手机号", "所属部门", "职位", "邮箱", "QQ", "微信", "创建日期", "所属平台",
					"所属平台账号" };
			String[] columns = { "id", "accountName", "name", "phoneNum", "department", "position", "email", "qqNum",
					"weiXinNum", "createTimeStr", "plateForm", "managerName" };
			try
			{
				excelUtil.exportExcel(headers, columns, adminUserList, out, req, "");
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

	@RequestMapping("/managerAddNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(String accountName, String password, String name, String phoneNum,
			String department, String position, String email, String qqNum, String weiXinNum, HttpServletRequest req)
	{
		if (CommonUtils.isEmpty(accountName))
		{
			return AjaxResult.errorInstance("账户名不能为空");
		}
		if (!CommonUtils.isLengthEnough(password, 6))
		{
			return AjaxResult.errorInstance("密码长度不够");
		}
		if (CommonUtils.isEmpty(name))
		{
			return AjaxResult.errorInstance("真实姓名不能为空");
		}
		if (!CommonUtils.isPhone(phoneNum))
		{
			return AjaxResult.errorInstance("手机号格式不正确");
		}
		if (CommonUtils.isEmpty(department))
		{
			return AjaxResult.errorInstance("不能不能为空");
		}
		if (CommonUtils.isEmpty(position))
		{
			return AjaxResult.errorInstance("职位不能为空");
		}
		if (CommonUtils.isEmpty(qqNum))
		{
			return AjaxResult.errorInstance("qq号不能为空");
		}
		if (CommonUtils.isEmpty(weiXinNum))
		{
			return AjaxResult.errorInstance("微信号不能为空");
		}
		if (CommonUtils.isEmpty(email))
		{
			return AjaxResult.errorInstance("邮箱不能为空");
		}

		AdminUser adminUser = new AdminUser();
		adminUser.setPhoneNum(phoneNum);
		if (adminUserService.isExisted(adminUser))
		{
			return AjaxResult.errorInstance("手机账号已经存在，不能重复添加");
		}

		AdminUser au = (AdminUser) req.getSession().getAttribute("adminUser");

		adminUser.setId(new IDUtils(0, 0).nextId());
		adminUser.setAccountName(phoneNum);
		adminUser.setName(name);
		adminUser.setManagerId(au.getId());
		String passwordSalt = UUID.randomUUID().toString();
		adminUser.setPasswordSalt(passwordSalt);
		adminUser.setPassword(CommonUtils.calculateMD5(passwordSalt + password));
		adminUser.setDepartment(department);
		adminUser.setPosition(position);
		adminUser.setEmail(email);
		adminUser.setQqNum(qqNum);
		adminUser.setWeiXinNum(weiXinNum);
		adminUser.setCreateTime(System.currentTimeMillis());

		adminUserService.insert(adminUser);
		return AjaxResult.successInstance("添加成功");
	}

	@RequestMapping("/delete.do")
	public @ResponseBody AjaxResult delete(Long[] ids)
	{
		if (ids == null || ids.length <= 0)
		{
			return AjaxResult.errorInstance("没有选择需要删除的记录ID，请重新选择");
		}

		adminUserService.deleteByArray(ids);

		return AjaxResult.successInstance("删除成功");
	}

	@RequestMapping("/managerSearch.do")
	public @ResponseBody AjaxResult managerSearch(Long beginTime, Long endTime, Long id, String accountName,
			String name, String phoneNum, String managerName, Integer pageNum, Integer pageSize)
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
		param.put("accountName", CommonUtils.isEmpty(accountName) ? null : accountName);
		param.put("name", CommonUtils.isEmpty(name) ? null : name);
		param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
		param.put("id", id);
		param.put("managerName", CommonUtils.isEmpty(managerName) ? null : managerName);
		PageInfo<AdminUserManagerData> pageInfo = adminUserService.pageManagerData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);

	}
}
