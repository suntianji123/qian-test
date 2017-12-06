package com.qiandi.web.controller;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.qiandi.dao.PhoneInformationDAO;
import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.IdName;
import com.qiandi.pojo.PhoneAttribution;
import com.qiandi.pojo.PhoneInformation;
import com.qiandi.pojo.PhoneInformationData;
import com.qiandi.pojo.PhoneInformationRedis;
import com.qiandi.service.IdNameService;
import com.qiandi.service.PhoneAttributionService;
import com.qiandi.service.PhoneInformationService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;
import com.qiandi.util.Jedis2Utils;
import com.qiandi.util.Jedis2Utils.SortSet;
import com.qiandi.util.JsonUtils;
import com.qiandi.util.UploadUtils;
import com.qiandi.web.util.AdminUtils;
import com.qiandi.web.util.ImportUtils;

@Controller
@RequestMapping("/phoneInformation")
public class PhoneInformationController
{

	@Autowired
	private PhoneInformationService phoneInformationService;

	@Autowired
	private PhoneAttributionService phoneAttributionService;

	@Autowired
	private IdNameService idNameService;

	private PhoneInformationDAO phoneInformationDAO = new PhoneInformationDAO();


	// 将上传的手机号信息批量存入数据库
	@RequestMapping("/addByList.do")
	public @ResponseBody AjaxResult addByListSubmit(Long statusId, HttpServletRequest req)
	{
		if (statusId == null)
		{
			return AjaxResult.errorInstance("没有对应的手机号状态");
		}

		IdName idName = idNameService.selectOne(statusId);
		if (idName == null || !("手机号状态").equals(idName.getName()))
		{
			return AjaxResult.errorInstance("选择的手机号状态id不正确");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		Long adminUserId = adminUser.getId();
		Connection conn = null;
		try
		{
			Jedis2Utils redis = Jedis2Utils.getInstance();
			SortSet sortSet = redis.SORTSET;
			String key = "phoneInformation_adminUser_" + adminUserId;
			long length = sortSet.zlength(key);
			Set<String> set = sortSet.zrangeByScore(key, 0, length);
			conn = AdminUtils.getConnection();
			phoneInformationDAO.insertByList(conn, set, statusId, adminUserId);
			clearCache(req);
			return AjaxResult.successInstance("新增成功");
		} catch (SQLException e)
		{
			throw new RuntimeException("获取数据库连接出错了", e);
		} finally
		{
			if (conn != null)
			{
				try
				{
					conn.close();
				} catch (SQLException e)
				{

				}
			}
		}


	}

	// 删除手机号、单个删除或者批量删除
	@RequestMapping("/deletePhoneNum.do")
	public @ResponseBody AjaxResult delete(Integer[] scores, HttpServletRequest req)
	{
		if (scores == null || scores.length <= 0)
		{
			return AjaxResult.errorInstance("该条记录索引不合法");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		String key = "phoneInformation_adminUser_" + adminUser.getId();
		Jedis2Utils redis = Jedis2Utils.getInstance();
		SortSet sortSet = redis.SORTSET;

		int count = 0;
		for (int i = 0; i < scores.length; i++)
		{
			int score = scores[i];
			long count1 = sortSet.zremrangeByRank(key, score, score);
			if (count1 != 0)
			{
				count++;
			}
		}
		if (count == 0)
		{
			return AjaxResult.errorInstance("删除的号码不存在");
		}

		return AjaxResult.successInstance("删除成功");

	}

	// 清理缓存
	@RequestMapping("/clearCache.do")
	public @ResponseBody AjaxResult clearCache(HttpServletRequest req)
	{
		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		String key = "phoneInformation_adminUser_" + adminUser.getId();
		Jedis2Utils redis = Jedis2Utils.getInstance();
		SortSet sortSet = redis.SORTSET;
		sortSet.zrem(key);
		return AjaxResult.successInstance("删除所有号码处理记录成功");
	}

	@RequestMapping("/listPhoneNum.do")
	public @ResponseBody AjaxResult listPhoneNum(Integer pageNum, Integer pageSize, HttpServletRequest req)
	{
		if (pageNum == null)
		{
			pageNum = 1;
		}
		if (pageSize == null)
		{
			pageSize = 5;
		}

		Jedis2Utils redis = Jedis2Utils.getInstance();
		SortSet sortSet = redis.SORTSET;

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		String key = "phoneInformation_adminUser_" + adminUser.getId();
		List<PhoneInformationRedis> phoneInformationRedisList = new ArrayList<PhoneInformationRedis>();
		Long length = sortSet.zlength(key);
		System.out.println(length);
		Set<String> set = sortSet.zrevrange(key, pageSize * (pageNum - 1), pageSize * pageNum - 1);
		Iterator<String> iterator = set.iterator();
		while (iterator.hasNext())
		{
			PhoneInformationRedis phoneInformationRedis = JsonUtils.toBean(iterator.next(),
					PhoneInformationRedis.class);
			phoneInformationRedisList.add(phoneInformationRedis);
		}

		int total = length.intValue();
		PageHelper.startPage(pageNum, pageSize);
		PageInfo<PhoneInformationRedis> pageInfo = new PageInfo<PhoneInformationRedis>(phoneInformationRedisList);
		pageInfo.setPageNum(pageNum);
		pageInfo.setPageSize(pageSize);
		pageInfo.setTotal(length.intValue());
		pageInfo.setPages(total % pageSize > 0 ? total / pageSize + 1 : total / pageSize);
		pageInfo.setPrePage(pageNum - 1);
		pageInfo.setNextPage(pageNum + 1);
		pageInfo.setNavigatePages(4);
		pageInfo.setNavigatepageNums(calcNavigatepageNums(pageInfo.getPages(), pageInfo.getNavigatePages(), pageNum));
		pageInfo.setIsFirstPage(pageNum == 1);
		pageInfo.setIsLastPage(pageNum == pageInfo.getPages());
		pageInfo.setHasPreviousPage(pageNum > 1);
		pageInfo.setHasNextPage(pageNum < pageInfo.getPages());

		// pager.setCurrentPage(pageNum);
		// pager.setPageSize(pageSize);
		// pager.setRecordTotal(length.intValue());
		// pager.setContent(phoneInformationRedisList);

		return AjaxResult.successInstance(pageInfo);
	}

	/**
	 * 计算导航页
	 */
	private int[] calcNavigatepageNums(int pages, int navigatePages, int pageNum)
	{
		// 当总页数小于或等于导航页码数时
		if (pages <= navigatePages)
		{
			int[] navigatepageNums = new int[pages];
			for (int i = 0; i < pages; i++)
			{
				navigatepageNums[i] = i + 1;
			}
			return navigatepageNums;
		} else
		{// 当总页数大于导航页码数时
			int[] navigatepageNums = new int[navigatePages];
			int startNum = pageNum - navigatePages / 2;
			int endNum = pageNum + navigatePages / 2;

			if (startNum < 1)
			{
				startNum = 1;
				// (最前navigatePages页
				for (int i = 0; i < navigatePages; i++)
				{
					navigatepageNums[i] = startNum++;
				}
			} else if (endNum > pages)
			{
				endNum = pages;
				// 最后navigatePages页
				for (int i = navigatePages - 1; i >= 0; i--)
				{
					navigatepageNums[i] = endNum--;
				}
			} else
			{
				// 所有中间页
				for (int i = 0; i < navigatePages; i++)
				{
					navigatepageNums[i] = startNum++;
				}
			}
			return navigatepageNums;
		}
	}

	@RequestMapping("/uploadByExcel.do")
	public @ResponseBody AjaxResult uploadByExcel(HttpServletRequest req, Long statusId)
	{

		if (statusId == null)
		{
			return AjaxResult.errorInstance("没有对应的手机号状态");
		}

		IdName idName = idNameService.selectOne(statusId);
		if (idName == null || !("手机号状态").equals(idName.getName()))
		{
			return AjaxResult.errorInstance("选择的手机号状态id不正确");
		}

		MultipartHttpServletRequest mulReq = (MultipartHttpServletRequest) req;
		MultipartFile file = mulReq.getFile("excel");
		if (file == null || file.getOriginalFilename().equals(""))
		{
			return AjaxResult.errorInstance("没有上传Excel类型的文件");
		}
		String extension = UploadUtils.getExtension(file.getOriginalFilename());
		if (!".xls".equalsIgnoreCase(extension) && !".xlsx".equalsIgnoreCase(extension))
		{
			return AjaxResult.errorInstance("请上传.xls或者.xlsx类型的文件");
		}

		// 读取文件
		InputStream inStream = null;
		try
		{
			inStream = file.getInputStream();

			// 查询出黑白空名单中所有的手机号
			List<String> phoneNumList = phoneInformationService.selectPhoneNumList();

			Map<String, Object> map = ImportUtils.getPhoneInformationRedisByExcel(inStream, phoneNumList);


			@SuppressWarnings("unchecked")
			List<PhoneInformationRedis> list = (List<PhoneInformationRedis>) map.get("phoneInformationRedisList");
			if(!CommonUtils.isEmpty(list))
			{

				Jedis2Utils redis = Jedis2Utils.getInstance();
				SortSet sortSet = redis.SORTSET;
				// 查询手机号归属地信息
				@SuppressWarnings("unchecked")
				List<Integer> idList = (List<Integer>) map.get("idList");
				// 查询归属地信息
				List<PhoneAttribution> phoneAttributionList = phoneAttributionService.selectListByIds(idList);

				if (!CommonUtils.isEmpty(idList))
				{
					AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
					Long adminUserId = adminUser.getId();
					for (int i = 0; i < list.size(); i++)
					{
						PhoneInformationRedis phoneInformationRedis = list.get(i);
						Iterator<PhoneAttribution> iterator = phoneAttributionList.iterator();
						while (iterator.hasNext())
						{
							PhoneAttribution phoneAttribution = iterator.next();
							if ((phoneAttribution.getId() + "")
									.equals(phoneInformationRedis.getPhoneNum().substring(0, 7)))
							{
								phoneInformationRedis.setOperator(phoneAttribution.getOperator());
								phoneInformationRedis.setProvince(phoneAttribution.getProvince());
								phoneInformationRedis.setCity(phoneAttribution.getCity());
								phoneInformationRedis.setScore(i);
								list.set(i, phoneInformationRedis);
								break;
							}
						}
						// 加入到redis缓存
						sortSet.zadd("phoneInformation_adminUser_" + adminUserId, i,
								JsonUtils.toJson(phoneInformationRedis));
					}
				}


			} else
			{
				return AjaxResult.errorInstance("没有导入手机号或者手机号都已经存在于手机号数据库");
			}
			
			map.remove("phoneInformationRedisList");
			map.remove("idList");

			return AjaxResult.successInstance(map);
		} catch (IOException e)
		{
			e.printStackTrace();
		} finally
		{
			UploadUtils.closeQuietly(inStream);
		}
		return AjaxResult.errorInstance("没有导入手机号或者手机号都已经存在于手机号数据库");
	}

	@RequestMapping("/exportToExcel.do")
	public void exportToExcel(Long beginTime, Long endTime, Long id, String phoneNum, Long statusId,
			String operator, String province, String city, Long[] ids, HttpServletRequest req, HttpServletResponse resp)
	{
		List<PhoneInformationData> phoneInformationList = new ArrayList<PhoneInformationData>();
		if (ids != null && ids.length > 0)
		{
			phoneInformationList = phoneInformationService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
			param.put("statusId", statusId);
			param.put("operator", CommonUtils.isEmpty(operator) ? null : operator);
			param.put("province", CommonUtils.isEmpty(province) ? null : province);
			param.put("city", CommonUtils.isEmpty(city) ? null : city);
			param.put("id", id);
			phoneInformationList = phoneInformationService.selectData(param);
		}

		ExportToExcelUtil<PhoneInformationData> excelUtil = new ExportToExcelUtil<PhoneInformationData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "黑白空号统计表");
			String[] headers = { "记录ID", "手机号", "所属运营商", "地区", "号码状态", "添加日期", "操作人" };
			String[] columns = { "id", "phoneNum", "operator", "address", "statusStr", "createTime",
					"addAdminUserName" };
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
			return AjaxResult.errorInstance("没有选择需要删除项，请选择");
		}

		phoneInformationService.deleteByArray(ids);
		return AjaxResult.successInstance("删除成功");
	}

	@RequestMapping("/updateSubmit.do")
	public @ResponseBody AjaxResult updateSubmit(Long id, Long statusId, HttpServletRequest req)
	{
		PhoneInformation phoneInformation = phoneInformationService.selectOne(id);
		if (phoneInformation == null)
		{
			return AjaxResult.errorInstance("手机号信息不存在");
		}


		IdName idName = idNameService.selectOne(statusId);
		if (idName == null)
		{
			return AjaxResult.errorInstance("手机号状态信息不存在");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");

		phoneInformation.setStatusId(statusId);
		phoneInformation.setCreateTime(System.currentTimeMillis());
		phoneInformation.setAddAdminUserId(adminUser.getId());
		phoneInformationService.update(phoneInformation);
		return AjaxResult.successInstance("修改成功");

	}

	@RequestMapping("/searchPhoneAttribution.do")
	public @ResponseBody AjaxResult searchPhoneAttribution(String phoneNum)
	{
		if (!CommonUtils.isPhone(phoneNum))
		{
			return AjaxResult.errorInstance("手机号格式错误");
		}

		Long id = Long.parseLong(phoneNum.substring(0, 7));
		PhoneAttribution phoneAttribution = phoneAttributionService.selectOne(id);
		if (phoneAttribution == null)
		{
			return AjaxResult.errorInstance("手机号归属地信息不存在");
		}

		return AjaxResult.successInstance(phoneAttribution);
	}

	// 从数据字典中加载出手机号状态信息
	@RequestMapping("/loadPhoneStatus.do")
	public @ResponseBody AjaxResult loadPhoneStatus()
	{
		IdName idName = new IdName();
		idName.setName("手机号状态");
		List<IdName> idNameList = idNameService.selectList(idName);
		return AjaxResult.successInstance(idNameList);
	}

	@RequestMapping("/addNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(String phoneNum, Long statusId, HttpServletRequest req)
	{

		// 查询出归属地信息
		if (!CommonUtils.isPhone(phoneNum))
		{
			return AjaxResult.errorInstance("手机号格式不正确");
		}

		IdName idName = idNameService.selectOne(statusId);
		if (idName == null || !("手机号状态").equals(idName.getName()))
		{
			return AjaxResult.errorInstance("手机号状态信息不存在");
		}

		Long phoneAttributionId = Long.parseLong(phoneNum.substring(0, 7));
		
		PhoneAttribution phoneAttribution = phoneAttributionService.selectOne(phoneAttributionId);
		if (phoneAttribution == null)
		{
			return AjaxResult.errorInstance("手机号归属地信息");
		}
		
		String operator = phoneAttribution.getOperator();
		String province = phoneAttribution.getProvince();
		String city = phoneAttribution.getCity();

		// 唯一性检查
		PhoneInformation phoneInformation = new PhoneInformation();
		phoneInformation.setPhoneNum(phoneNum);
		if (phoneInformationService.isExisted(phoneInformation))
		{
			return AjaxResult.errorInstance("手机号已经存在，不能重复添加");
		}


		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");

		phoneInformation = new PhoneInformation();
		phoneInformation.setId(new IDUtils(0, 0).nextId());
		phoneInformation.setPhoneNum(phoneNum);
		phoneInformation.setStatusId(statusId);
		phoneInformation.setOperator(operator);
		phoneInformation.setProvince(province);
		phoneInformation.setCity(city);
		phoneInformation.setAddAdminUserId(adminUser.getId());
		phoneInformation.setCreateTime(System.currentTimeMillis());

		phoneInformationService.insert(phoneInformation);
		return AjaxResult.successInstance("添加成功！");
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, Long id, String phoneNum, Long statusId,
			String operator, String province, String city, Integer pageNum, Integer pageSize)
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
		param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
		param.put("statusId", statusId);
		param.put("operator", CommonUtils.isEmpty(operator) ? null : operator);
		param.put("province", CommonUtils.isEmpty(province) ? null : province);
		param.put("city", CommonUtils.isEmpty(city) ? null : city);
		param.put("id", id);

		PageInfo<PhoneInformationData> pageInfo = phoneInformationService.pageData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);
	}
}
