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
import com.qiandi.dao.MenberUserSettingChannelHistoryDAO;
import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.Channel;
import com.qiandi.pojo.ChannelData;
import com.qiandi.pojo.MenberUserDataForChannel;
import com.qiandi.pojo.MenberUserSetting;
import com.qiandi.pojo.MenberUserSettingChannelHistory;
import com.qiandi.service.ChannelService;
import com.qiandi.service.MenberUserSettingService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;
import com.qiandi.web.util.AdminUtils;

@Controller
@RequestMapping("/channel")
public class ChannelController
{
	@Autowired
	private ChannelService channelService;

	private MenberUserSettingChannelHistoryDAO menberUserSettingChannelHistoryDAO = new MenberUserSettingChannelHistoryDAO();

	@Autowired
	private MenberUserSettingService menberUserSettingService;


	// 会员批量绑定频道提交
	@RequestMapping("/updateMenberUserSettingChannelSubmit.do")
	public @ResponseBody AjaxResult updateMenberUserSettingChannelSubmit(Long[] menberUserSettingIds,
			Long[] splitChannelIds, Long channelId,
			HttpServletRequest req)
	{
		if (menberUserSettingIds == null || menberUserSettingIds.length <= 0)
		{
			return AjaxResult.errorInstance("没有选择需要更改的会员配置id");
		}
		if (channelId == null)
		{
			return AjaxResult.errorInstance("没有选择频道id");
		}
		if(splitChannelIds==null||splitChannelIds.length<=0)
		{
			return AjaxResult.errorInstance("将要被去除的频道不能为空");
		}
		if (CommonUtils.contains(splitChannelIds, channelId))
		{
			return AjaxResult.errorInstance("这个频道将要被删除，你不能绑定这个频道");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		// 单个修改
		if (menberUserSettingIds.length == 1)
		{
			Long menberUserSettingId = menberUserSettingIds[0];
			MenberUserSetting menberUserSetting = menberUserSettingService.selectOne(menberUserSettingId);
			if (menberUserSetting == null)
			{
				return AjaxResult.errorInstance("会员配置id不存在");
			} else
			{
				menberUserSetting.setChannelId(channelId);
				menberUserSetting.setYiDongAisleId(0l);
				menberUserSetting.setLianTongAisleId(0l);
				menberUserSetting.setDianXingAisleId(0l);

				MenberUserSettingChannelHistory menberUserSettingChannelHistory = new MenberUserSettingChannelHistory();
				menberUserSettingChannelHistory.setId(new IDUtils(0, 0).nextId());
				menberUserSettingChannelHistory.setAddAdminUserId(adminUser.getId());
				menberUserSettingChannelHistory.setChannelId(channelId);
				menberUserSettingChannelHistory.setMenberUserSettingId(menberUserSettingId);
				menberUserSettingChannelHistory.setCreateTime(System.currentTimeMillis());
				menberUserSettingService.updateChannel(menberUserSetting, menberUserSettingChannelHistory);
			}
		} else
		{
			channelService.insertByList();
			Connection conn = null;
			try
			{
				conn = AdminUtils.getConnection();
				menberUserSettingChannelHistoryDAO.insertByArray(conn, adminUser.getId(), channelId,
						menberUserSettingIds);
			} catch (SQLException e)
			{
				throw new RuntimeException("获取连接出错了", e);
			} finally
			{
				JDBCUtils.closeQuietly(conn);
			}

		}

		return AjaxResult.successInstance("修改成功");

	}

	// 加载账户名、姓名，并分页显示
	@RequestMapping("/loadMenberUserByChannelId.do")
	public @ResponseBody AjaxResult loadMenberUserByChannelId(Long[] channelIds, Integer pageSize, Integer pageNum)
	{
		if (channelIds == null || channelIds.length <= 0)
		{
			return AjaxResult.errorInstance("没有选择绑定的频道id");
		}
		if (pageNum == null)
		{
			pageNum = 1;
		}
		if (pageSize == null)
		{
			pageSize = 2;
		}
		

		PageInfo<MenberUserDataForChannel> pageInfo = menberUserSettingService.selectMenberUserDataForChannel(pageNum,
				pageSize, channelIds);

		return AjaxResult.successInstance(pageInfo);
	}

	@RequestMapping("/exportToExcel.do")
	public void exportToExcel(Long id, String addAdminUserName, Long beginTime, Long endTime, Long[] ids,
			HttpServletRequest req, HttpServletResponse resp)
	{
		List<ChannelData> phoneInformationList = new ArrayList<ChannelData>();
		if (ids != null && ids.length > 0)
		{
			phoneInformationList = channelService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("id", id);
			param.put("addAdminUserName", CommonUtils.isEmpty(addAdminUserName) ? null : addAdminUserName);
			phoneInformationList = channelService.selectData(param);
		}

		ExportToExcelUtil<ChannelData> excelUtil = new ExportToExcelUtil<ChannelData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "频道统计表");
			String[] headers = { "记录ID", "频道名称", "添加日期", "添加人" };
			String[] columns = { "id", "name", "createTimeStr", "addAdminUserName" };
			try
			{
				excelUtil.exportExcel(headers, columns, phoneInformationList, out, req, "");
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
		if (ids == null || ids.length <= 0)
		{
			return AjaxResult.errorInstance("没有选择需要删除的记录ID");
		}

		channelService.deleteByArray(ids);

		return AjaxResult.successInstance("删除成功");
	}

	@RequestMapping("/addNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(String name, HttpServletRequest req)
	{
		if (CommonUtils.isEmpty(name))
		{
			return AjaxResult.errorInstance("频道名称不能为空");
		}

		// 唯一性判断
		Channel channel = new Channel();
		channel.setName(name);

		if (channelService.isExisted(channel))
		{
			return AjaxResult.errorInstance("频道名称已经存在，不能重复添加");
		}
		
		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		channel = new Channel();
		channel.setAddAdminUserId(adminUser.getId());
		channel.setId(new IDUtils(0, 0).nextId());
		channel.setCreateTime(System.currentTimeMillis());
		channel.setName(name);
		channel.setYiDongAisleId(0l);
		channel.setLianTongAisleId(0l);
		channel.setDianXingAisleId(0l);
		
		channelService.insert(channel);
		return AjaxResult.successInstance("添加成功");
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long id, String addAdminUserName, Long beginTime, Long endTime,
			Integer pageSize, Integer pageNum)
	{
		if (pageSize == null)
		{
			pageSize = 2;
		}
		if (pageNum == null)
		{
			pageNum = 1;
		}

		Map<String, Object> param = new HashMap<String, Object>();
		param.put("beginTime", beginTime);
		param.put("endTime", endTime);
		param.put("id", id);
		param.put("addAdminUserName", CommonUtils.isEmpty(addAdminUserName) ? null : addAdminUserName);

		PageInfo<ChannelData> pageInfo = channelService.pageData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);

	}
}
