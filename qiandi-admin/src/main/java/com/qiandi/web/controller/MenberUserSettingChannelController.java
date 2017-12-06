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
import com.qiandi.pojo.Channel;
import com.qiandi.pojo.MenberUserSetting;
import com.qiandi.pojo.MenberUserSettingChannelHistory;
import com.qiandi.pojo.MenberUserSettingChannelHistoryData;
import com.qiandi.service.ChannelService;
import com.qiandi.service.MenberUserSettingChannelHistoryService;
import com.qiandi.service.MenberUserSettingService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/menberUserSettingChannelHistory")
public class MenberUserSettingChannelController
{
	@Autowired
	private MenberUserSettingChannelHistoryService menberUserSettingChannelHistoryService;
	
	@Autowired
	private MenberUserSettingService menberUserSettingService;

	@Autowired
	private ChannelService channelService;

	@RequestMapping("/exportToExcel.do")
	public void exportToExcel(Long beginTime, Long endTime, String menberUserAccountName, Long channelId,
			String addAdminUserName, Long id, Long ids[], HttpServletRequest req, HttpServletResponse resp)
	{
		List<MenberUserSettingChannelHistoryData> list = new ArrayList<MenberUserSettingChannelHistoryData>();
		if (ids != null && ids.length > 0)
		{
			list = menberUserSettingChannelHistoryService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("id", id);
			param.put("menberUserAccountName",
					CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
			param.put("channelId", channelId);
			param.put("addAminUserName", CommonUtils.isEmpty(addAdminUserName) ? null : addAdminUserName);

			list = menberUserSettingChannelHistoryService.selectData(param);
		}

		ExportToExcelUtil<MenberUserSettingChannelHistoryData> excelUtil = new ExportToExcelUtil<MenberUserSettingChannelHistoryData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "频道绑定记录表");
			String[] headers = { "记录ID", "会员账户", "频道名称", "添加日期", "添加人" };
			String[] columns = { "id", "menberUserAccountName", "channelName", "createTimeStr", "addAdminUserName" };
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

	@RequestMapping("/delete.do")
	public @ResponseBody AjaxResult delete(Long[] ids)
	{
		if (ids == null)
		{
			return AjaxResult.errorInstance("没有选择需要删除的绑定频道记录");
		}

		menberUserSettingChannelHistoryService.deleteByArray(ids);
		return AjaxResult.successInstance("删除成功");
	}

	// 为会员绑定频道
	@RequestMapping("/updateChannelSubmit.do")
	public @ResponseBody AjaxResult updateChannelSubmit(Long menberUserSettingId, Long channelId,
			HttpServletRequest req)
	{
		if (menberUserSettingId == null)
		{
			return AjaxResult.errorInstance("绑定会员的配置id不能为空");
		}
		if (channelId == null)
		{
			return AjaxResult.errorInstance("频道id不能为空");
		}

		MenberUserSetting menberUserSetting = menberUserSettingService.selectOne(menberUserSettingId);
		if (menberUserSetting == null)
		{
			return AjaxResult.errorInstance("会员配置记录不存在");
		} else
		{
			menberUserSetting.setChannelId(channelId);
			menberUserSetting.setYiDongAisleId(0l);
			menberUserSetting.setLianTongAisleId(0l);
			menberUserSetting.setDianXingAisleId(0l);

			AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
			MenberUserSettingChannelHistory menberUserSettingChannelHistory = new MenberUserSettingChannelHistory();
			menberUserSettingChannelHistory.setId(new IDUtils(0, 0).nextId());
			menberUserSettingChannelHistory.setAddAdminUserId(adminUser.getId());
			menberUserSettingChannelHistory.setChannelId(channelId);
			menberUserSettingChannelHistory.setMenberUserSettingId(menberUserSettingId);
			menberUserSettingChannelHistory.setCreateTime(System.currentTimeMillis());

			menberUserSettingService.updateChannel(menberUserSetting, menberUserSettingChannelHistory);
		}

		return AjaxResult.successInstance("会员绑定频道成功");

	}

	// 加载所有可用的频道
	@RequestMapping("/loadChannel.do")
	public @ResponseBody AjaxResult loadChannel()
	{
		List<Channel> channelList = channelService.selectList();
		return AjaxResult.successInstance(channelList);
	}

	@RequestMapping("/searchMenberUserChannel.do")
	public @ResponseBody AjaxResult searchMenberUserChannel(String accountName)
	{
		Map<String, Object> map = menberUserSettingService.selectChannel(accountName);
		if (CommonUtils.isEmpty(map))
		{
			return AjaxResult.errorInstance("用户不存在");
		}
		if (map.get("channelName") == null)
		{
			return AjaxResult.errorInstance("会员没有绑定频道");
		}
		map.put("menberUserSettingId", map.get("menberUserSettingId") + "");
		return AjaxResult.successInstance(map);
	}

	@RequestMapping("/searchMenberUserAisle.do")
	public @ResponseBody AjaxResult searchMenberUser(String accountName)
	{
		Map<String, Object> map = menberUserSettingService.selectAisle(accountName);
		if (CommonUtils.isEmpty(map))
		{
			return AjaxResult.errorInstance("用户不存在");
		}
		map.put("menberUserSettingId", map.get("menberUserSettingId") + "");
		return AjaxResult.successInstance(map);
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, String menberUserAccountName,
			Long channelId, String addAdminUserName, Long id, Integer pageNum, Integer pageSize)
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
		param.put("id", id);
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("channelId", channelId);
		param.put("addAminUserName", CommonUtils.isEmpty(addAdminUserName) ? null : addAdminUserName);
		
		PageInfo<MenberUserSettingChannelHistoryData> pageInfo = menberUserSettingChannelHistoryService
				.pageData(pageNum, pageSize, param);
		
		return AjaxResult.successInstance(pageInfo);

	}
}
