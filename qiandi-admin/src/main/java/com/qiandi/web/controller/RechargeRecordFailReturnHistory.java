package com.qiandi.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qiandi.service.SettingService;
import com.qiandi.util.AjaxResult;
import com.qiandi.web.job.SpringDynamicCronTask;
import com.qiandi.web.util.AdminUtils;

@Controller
@RequestMapping("/rechargeRecordFailReturnHistory")
public class RechargeRecordFailReturnHistory
{
	@Autowired
	private SettingService settingService;

	@RequestMapping("/loadRechargeReturnSetting.do")
	public @ResponseBody AjaxResult loadRechargeReturnSetting()
	{
		Map<String, Integer> map = new HashMap<String, Integer>();
		Integer day = Integer.parseInt(AdminUtils.getValue(settingService, "recived_fail_day"));
		Integer hour = Integer.parseInt(AdminUtils.getValue(settingService, "recived_fail_hour"));
		Integer minute = Integer.parseInt(AdminUtils.getValue(settingService, "recived_fail_minute"));
		map.put("day", day);
		map.put("hour", hour);
		map.put("minute", minute);

		return AjaxResult.successInstance(map);
	}

	@RequestMapping("/updateReturnTime.do")
	public @ResponseBody AjaxResult updateReturnTime(Integer day, Integer hour, Integer minute)
	{
		if (day == null || day <= 0)
		{
			return AjaxResult.errorInstance("天数必须大于0");
		}
		if (hour == null || hour < 0 || hour > 23)
		{
			return AjaxResult.errorInstance("小时必须在0和23之间");
		}
		if (minute == null || minute < 0 || minute > 59)
		{
			return AjaxResult.errorInstance("分钟必须在0和59之间");
		}

		settingService.updateThree(day, hour, minute);
		SpringDynamicCronTask.cron = "0 " + minute + " " + hour + " ? * *";
		return AjaxResult.successInstance("ok");
	}
}
