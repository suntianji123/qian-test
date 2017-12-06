package com.qiandi.web.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qiandi.pojo.AdminUser;
import com.qiandi.service.AdminUserService;
import com.qiandi.util.AjaxResult;

@Controller
public class OtherController
{

	@Autowired
	private AdminUserService adminUserService;


	@RequestMapping("/")
	public @ResponseBody AjaxResult index(HttpServletRequest req)
	{
		AdminUser adminUser = adminUserService.selectOne(373404015553150976l);
		HttpSession session = req.getSession();
		session.setAttribute("adminUser", adminUser);
		session.setMaxInactiveInterval(60 * 60 * 24 * 30);


		return AjaxResult.successInstance("xxxx");

	}

}
