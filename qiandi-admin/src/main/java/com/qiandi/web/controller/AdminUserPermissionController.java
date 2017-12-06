package com.qiandi.web.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.Permission;
import com.qiandi.service.AdminUserPermissionService;
import com.qiandi.service.AdminUserService;
import com.qiandi.service.PermissionService;
import com.qiandi.table.AdminUserItem;
import com.qiandi.util.AjaxResult;

@Controller
@RequestMapping("/adminUserPermission")
public class AdminUserPermissionController
{
	@Autowired
	private AdminUserService adminUserService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private AdminUserPermissionService adminUserPermissionService;

	/*
	 * 修改权限提交
	 */
	@RequestMapping("/updateAdminUserPermission.do")
	public @ResponseBody AjaxResult updateAdminUserPermission(Long adminUserId, Long[] permissionIds,
			HttpServletRequest req)
	{
		if(adminUserId==null)
		{
			return AjaxResult.errorInstance("需要修改权限的操作员记录不能为空");
		}
		
		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		
		AdminUser param = new AdminUser();
		param.setId(adminUserId);
		param.setManagerId(adminUser.getId());
		if(!adminUserService.isExisted(param))
		{
			return AjaxResult.errorInstance("操作员不存在");
		}
		
		adminUserPermissionService.updateFirst(adminUserId, permissionIds);
		
		return AjaxResult.successInstance("修改成功");
		
	}

	/*
	 * 查询单个会员所绑定的权限
	 */
	@RequestMapping("/searchMyAdminUserPermission.do")
	public @ResponseBody AjaxResult searchMyAdminUserPermission(Long adminUserId, HttpServletRequest req)
	{
		if (adminUserId == null)
		{
			return AjaxResult.errorInstance("操作员的id不能为空");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");

		AdminUser param = new AdminUser();
		param.setId(adminUserId);
		param.setManagerId(adminUser.getId());
		if (!adminUserService.isExisted(param))
		{
			return AjaxResult.errorInstance("操作员不存在");
		}

		List<Permission> permissionList = adminUserPermissionService.selectSecondListByFirstId(adminUserId);

		return AjaxResult.successInstance(permissionList);

	}

	/*
	 * 加载所有系统平台权限
	 */
	@RequestMapping("/loadPermission")
	public @ResponseBody AjaxResult loadPermission()
	{
		Permission permission = new Permission();
		permission.setType("2");
		List<Permission> permissionList = permissionService.selectList(permission);
		return AjaxResult.successInstance(permissionList);
	}

	// 加载管理员所有的操作员
	@RequestMapping("/loadMyAdminUser.do")
	public @ResponseBody AjaxResult selectMyAdminUser(HttpServletRequest req)
	{
		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");

		List<AdminUserItem> adminUserItemList = adminUserService.selectMyAdminUserItem(adminUser.getId());

		return AjaxResult.successInstance(adminUserItemList);

	}

}
