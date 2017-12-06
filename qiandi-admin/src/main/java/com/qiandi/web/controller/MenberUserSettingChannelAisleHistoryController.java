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
import com.qiandi.pojo.Aisle;
import com.qiandi.pojo.Channel;
import com.qiandi.pojo.MenberUserSetting;
import com.qiandi.pojo.MenberUserSettingChannelAisleHistory;
import com.qiandi.pojo.MenberUserSettingChannelAisleHistoryData;
import com.qiandi.service.AisleService;
import com.qiandi.service.ChannelService;
import com.qiandi.service.MenberUserSettingChannelAisleHistoryService;
import com.qiandi.service.MenberUserSettingService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/menberUserSettingChannelAisleHistory")
public class MenberUserSettingChannelAisleHistoryController
{
	@Autowired
	private MenberUserSettingChannelAisleHistoryService menberUserSettingChannelAisleHistoryService;

	@Autowired
	private MenberUserSettingService menberUserSettingService;

	@Autowired
	private AisleService aisleService;


	@Autowired
	private ChannelService channelService;



	@RequestMapping("/updateAisleSubmit.do")
	public @ResponseBody AjaxResult updateAisle(Long channelId, Long menberUserSettingId, Long aisleId, Integer type,
			HttpServletRequest req)
	{
		if (type == null)
		{
			return AjaxResult.errorInstance("绑定的通道类型不能为空");
		}
		if (type != 4 && type != 2 && type != 1)
		{
			return AjaxResult.errorInstance("您选择的通道类型不对");
		}

		if (channelId != null && menberUserSettingId != null)
		{
			return AjaxResult.errorInstance("一次只能绑定一个通道");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		if (channelId != null)
		{
			Channel channel = channelService.selectOne(channelId);
			if (channel != null)
			{
				if (type == 4)
				{
					channel.setYiDongAisleId(aisleId);
				} else if (type == 2)
				{
					channel.setLianTongAisleId(aisleId);
				} else if (type == 1)
				{
					channel.setDianXingAisleId(aisleId);
				}
			}
			MenberUserSettingChannelAisleHistory menberUserSettingChannelAisleHistory = new MenberUserSettingChannelAisleHistory();
			menberUserSettingChannelAisleHistory.setId(new IDUtils(0, 0).nextId());
			menberUserSettingChannelAisleHistory.setAddAdminUserId(adminUser.getId());
			menberUserSettingChannelAisleHistory.setChannelId(channel.getId());
			menberUserSettingChannelAisleHistory.setCreateTime(System.currentTimeMillis());
			menberUserSettingChannelAisleHistory.setDianXingAisleId(channel.getDianXingAisleId());
			menberUserSettingChannelAisleHistory.setLianTongAisleId(channel.getLianTongAisleId());
			menberUserSettingChannelAisleHistory.setYiDongAisleId(channel.getYiDongAisleId());
			menberUserSettingChannelAisleHistory.setMenberUserSettingId(0l);
			menberUserSettingChannelAisleHistory.setType(true);

			channelService.updateChannelAndInsertToRecord(channel, menberUserSettingChannelAisleHistory);

		} else if (menberUserSettingId != null)
		{
			MenberUserSetting menberUserSetting = menberUserSettingService.selectOne(menberUserSettingId);
			if (menberUserSetting != null)
			{
				if (type == 4)
				{
					menberUserSetting.setYiDongAisleId(aisleId);
				} else if (type == 2)
				{
					menberUserSetting.setLianTongAisleId(aisleId);
				} else if (type == 1)
				{
					menberUserSetting.setDianXingAisleId(aisleId);
				}
				menberUserSetting.setChannelId(0l);

				MenberUserSettingChannelAisleHistory menberUserSettingChannelAisleHistory = new MenberUserSettingChannelAisleHistory();
				menberUserSettingChannelAisleHistory.setId(new IDUtils(0, 0).nextId());
				menberUserSettingChannelAisleHistory.setAddAdminUserId(adminUser.getId());
				menberUserSettingChannelAisleHistory.setChannelId(0l);
				menberUserSettingChannelAisleHistory.setCreateTime(System.currentTimeMillis());
				menberUserSettingChannelAisleHistory.setDianXingAisleId(menberUserSetting.getDianXingAisleId());
				menberUserSettingChannelAisleHistory.setLianTongAisleId(menberUserSetting.getLianTongAisleId());
				menberUserSettingChannelAisleHistory.setYiDongAisleId(menberUserSetting.getYiDongAisleId());
				menberUserSettingChannelAisleHistory.setMenberUserSettingId(menberUserSetting.getId());
				menberUserSettingChannelAisleHistory.setType(false);

				menberUserSettingService.updateChannelAndInsertToRecord(menberUserSetting,
						menberUserSettingChannelAisleHistory);
			}
		}

		return AjaxResult.successInstance("绑定通道成功");
	}

	// 加载三个通道
	@RequestMapping("/loadThreeAisle.do")
	public @ResponseBody AjaxResult loadThreeAisle()
	{
		List<Aisle> yiDongAisleList = aisleService.selectIdAndName(4);
		List<Aisle> lianTongAisleList = aisleService.selectIdAndName(2);
		List<Aisle> dianXingAisleList = aisleService.selectIdAndName(1);

		Map<String, Object> map = new HashMap<String, Object>();
		map.put("yiDongAisleList", yiDongAisleList);
		map.put("lianTongAisleList", lianTongAisleList);
		map.put("dianXingAisleList", dianXingAisleList);
		return AjaxResult.successInstance(map);
	}

	// 按移动、联通、电信加载通道:4.移动，2.联通，1.电信
	@RequestMapping("/loadAisle.do")
	public @ResponseBody AjaxResult loadAisle(Integer type)
	{ 
		if(type!=4&&type!=2&&type!=1)
		{
			return AjaxResult.errorInstance("您选择的通道类型不对");
		}
		List<Aisle> aisleList = aisleService.selectIdAndName(type);
		if (CommonUtils.isEmpty(aisleList))
		{
			return AjaxResult.errorInstance("通道不存在");
		}
		return AjaxResult.successInstance(aisleList);
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

	@RequestMapping("/searchChannelAisle.do")
	public @ResponseBody AjaxResult searchChannel(String name)
	{
		Map<String, Object> map = channelService.selectAisle(name);
		if (CommonUtils.isEmpty(map))
		{
			return AjaxResult.errorInstance("频道记录不存在");
		}

		map.put("channelId", map.get("channelId") + "");
		return AjaxResult.successInstance(map);
	}


	@RequestMapping("/exportToExcel.do")
	public void exportToExcel(Long beginTime, Long endTime, Long id, Long yiDongAisleId, Long lianTongAisleId,
			Long dianXingAisleId, Boolean type, String name, Long[] ids, HttpServletRequest req,
			HttpServletResponse resp)
	{
		List<MenberUserSettingChannelAisleHistoryData> list = new ArrayList<MenberUserSettingChannelAisleHistoryData>();
		if (ids != null && ids.length > 0)
		{
			list = menberUserSettingChannelAisleHistoryService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			list = menberUserSettingChannelAisleHistoryService.selectData(param);
		}

		ExportToExcelUtil<MenberUserSettingChannelAisleHistoryData> excelUtil = new ExportToExcelUtil<MenberUserSettingChannelAisleHistoryData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "收件地址统计表");
			String[] headers = { "记录ID", "绑定对象", "账户类型", "移动通道", "联通通道", "电信通道", "绑定日期", "操作人" };
			String[] columns = { "id", "name", "typeStr", "yiDongAisleName", "lianTongAisleName", "dianXingAisleName",
					"createTimeStr", "addAdminUserName" };
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
			return AjaxResult.errorInstance("没有选择需要删除的项");
		}

		menberUserSettingChannelAisleHistoryService.deleteByArray(ids);

		return AjaxResult.successInstance("删除成功");
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, Long id, Long yiDongAisleId,
			Long lianTongAisleId, Long dianXingAisleId, Boolean type, String name, Integer pageSize, Integer pageNum)
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
		param.put("yiDongAisleId", yiDongAisleId);
		param.put("lianTongAisleId", lianTongAisleId);
		param.put("dianXingAisleId", dianXingAisleId);
		param.put("name", CommonUtils.isEmpty(name) ? null : name);
		param.put("type", type);

		PageInfo<MenberUserSettingChannelAisleHistoryData> pageInfo = menberUserSettingChannelAisleHistoryService
				.pageData(pageNum, pageSize, param);

		return AjaxResult.successInstance(pageInfo);
	}
}
