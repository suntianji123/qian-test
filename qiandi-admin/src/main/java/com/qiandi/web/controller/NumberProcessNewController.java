package com.qiandi.web.controller;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.IdName;
import com.qiandi.pojo.NumberProcessNew;
import com.qiandi.pojo.PhoneAttribution;
import com.qiandi.service.IdNameService;
import com.qiandi.service.PhoneAttributionService;
import com.qiandi.service.PhoneInformationService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.IDUtils;
import com.qiandi.util.Jedis2Utils;
import com.qiandi.util.Jedis2Utils.SortSet;
import com.qiandi.util.JsonUtils;
import com.qiandi.util.Pager;
import com.qiandi.util.UploadUtils;
import com.qiandi.web.util.ImportUtils;

@Controller
@RequestMapping("/numberProcessNew")
public class NumberProcessNewController
{
	@Autowired
	private PhoneInformationService phoneInformationService;

	@Autowired
	private PhoneAttributionService phoneAttributionService;

	@Autowired
	private IdNameService idNameService;

	@RequestMapping("/deleteAll.do")
	public @ResponseBody AjaxResult deleteAll(HttpServletRequest req)
	{
		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		String key = "numberProcessNew_adminUser_" + adminUser.getId();
		Jedis2Utils redis = Jedis2Utils.getInstance();
		SortSet sortSet = redis.SORTSET;
		sortSet.zrem(key);
		return AjaxResult.successInstance("删除所有号码处理记录成功");
	}

	/**
	 * 计算移动、联通、电信手机号数量
	 * 
	 * @return
	 */
	@RequestMapping("calNumberCount.do")
	public @ResponseBody AjaxResult calNumberCount(HttpServletRequest req)
	{
		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		String key = "numberProcessNew_adminUser_" + adminUser.getId();
		Jedis2Utils redis = Jedis2Utils.getInstance();
		SortSet sortSet = redis.SORTSET;

		Long length = sortSet.zlength(key);
		Set<String> set = sortSet.zrevrange(key, 0, length.intValue());
		Iterator<String> iterator = set.iterator();
		int yiDongCount = 0;
		int lianTongCount = 0;
		int dianXingCount = 0;
		while (iterator.hasNext())
		{
			String str = iterator.next();
			if (str != null && str.contains("移动"))
			{
				yiDongCount++;
			} else if (str != null && str.contains("联通"))
			{
				lianTongCount++;
			} else if (str != null && str.contains("电信"))
			{
				dianXingCount++;
			}
		}

		Map<String, Integer> map = new HashMap<String, Integer>();
		map.put("yiDongCount", yiDongCount);
		map.put("lianTongCount", lianTongCount);
		map.put("dianXingCount", dianXingCount);
		map.put("totalCount", yiDongCount + lianTongCount + dianXingCount);

		return AjaxResult.successInstance(map);
	}

	@RequestMapping("/updateSubmit.do")
	public @ResponseBody AjaxResult delete(Integer score, String phoneNum, HttpServletRequest req)
	{
		if (!CommonUtils.isPhone(phoneNum))
		{
			return AjaxResult.errorInstance("手机号格式不正确");
		}
		if (score == null || score <= 0)
		{
			return AjaxResult.errorInstance("该条记录索引不合法");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		String key = "numberProcessNew_adminUser_" + adminUser.getId();
		Jedis2Utils redis = Jedis2Utils.getInstance();
		SortSet sortSet = redis.SORTSET;
		long count = sortSet.zremrangeByRank(key, score, score);
		if (count == 0)
		{
			return AjaxResult.errorInstance("号码记录不存在");
		}

		// 查询手机号归属地信息
		PhoneAttribution phoneAttribution = new PhoneAttribution();
		phoneAttribution.setId(Long.parseLong(phoneNum.substring(0, 7)));
		phoneAttribution = phoneAttributionService.selectOne(phoneAttribution);
		if (phoneAttribution == null)
		{
			return AjaxResult.errorInstance("手机号归属地信息不存在");
		}


		NumberProcessNew numberProcessNew = new NumberProcessNew();
		numberProcessNew.setId(new IDUtils(0, 0).nextId());
		numberProcessNew.setScore(score);
		numberProcessNew.setPhoneNum(phoneNum);
		numberProcessNew.setContent("");
		numberProcessNew.setLength(0);
		numberProcessNew.setSegment(0);
		numberProcessNew.setOperator(phoneAttribution.getOperator());
		numberProcessNew.setProvince(phoneAttribution.getProvince());
		numberProcessNew.setCity(phoneAttribution.getCity());
		sortSet.zadd(key, score, JsonUtils.toJson(numberProcessNew));
		return AjaxResult.successInstance("修改成功");
	}

	@RequestMapping("/delete.do")
	public @ResponseBody AjaxResult delete(Integer[] scores, HttpServletRequest req)
	{

		if (scores == null || scores.length <= 0)
		{
			return AjaxResult.errorInstance("该条记录索引不合法");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		String key = "numberProcessNew_adminUser_" + adminUser.getId();
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

	@RequestMapping("/addNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(String phoneNum, HttpServletRequest req)
	{
		if (!CommonUtils.isPhone(phoneNum))
		{
			return AjaxResult.errorInstance("手机号格式不正确");
		}

		// 查询手机号归属地信息
		PhoneAttribution phoneAttribution = new PhoneAttribution();
		phoneAttribution.setId(Long.parseLong(phoneNum.substring(0, 7)));
		phoneAttribution = phoneAttributionService.selectOne(phoneAttribution);
		if (phoneAttribution == null)
		{
			return AjaxResult.errorInstance("手机号归属地信息不存在");
		}
		
		//插入到redis缓存
		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		String key = "numberProcessNew_adminUser_" + adminUser.getId();
		Jedis2Utils redis = Jedis2Utils.getInstance();
		SortSet sortSet = redis.SORTSET;
		Long length = sortSet.zlength(key);
		Integer score = length.intValue();
		NumberProcessNew numberProcessNew = new NumberProcessNew();
		numberProcessNew.setId(new IDUtils(0, 0).nextId());
		numberProcessNew.setScore(score);
		numberProcessNew.setPhoneNum(phoneNum);
		numberProcessNew.setContent("");
		numberProcessNew.setLength(0);
		numberProcessNew.setSegment(0);
		numberProcessNew.setOperator(phoneAttribution.getOperator());
		numberProcessNew.setProvince(phoneAttribution.getProvince());
		numberProcessNew.setCity(phoneAttribution.getCity());
		sortSet.zadd(key, score, JsonUtils.toJson(numberProcessNew));
		return AjaxResult.successInstance("添加成功");
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Integer pageNum, Integer pageSize, HttpServletRequest req)
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
		String key = "numberProcessNew_adminUser_" + adminUser.getId();
		List<NumberProcessNew> numberProcessNewList = new ArrayList<NumberProcessNew>();
		Long length = sortSet.zlength(key);
		System.out.println(length);
		Set<String> set = sortSet.zrevrange(key, (pageNum - 1) * pageSize, pageSize - 1);
		Iterator<String> iterator = set.iterator();
		while (iterator.hasNext())
		{
			NumberProcessNew numberProcessNew = JsonUtils.toBean(iterator.next(), NumberProcessNew.class);
			numberProcessNewList.add(numberProcessNew);
		}

		Pager<NumberProcessNew> pager = new Pager<NumberProcessNew>();

		pager.setCurrentPage(pageNum);
		pager.setPageSize(pageSize);
		pager.setRecordTotal(length.intValue());
		pager.setContent(numberProcessNewList);

		return AjaxResult.successInstance(pager);
	}

	@RequestMapping("/uploadByExcel.do")
	public @ResponseBody AjaxResult uploadByExcel(HttpServletRequest req)
	{
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

					// 查询出黑名单的statusId
					IdName idName = new IdName();
					idName.setName("手机号状态");
					idName.setValue("黑号");
					idName = idNameService.selectOne(idName);
					Long blackId = idName.getId();

					idName = new IdName();
					idName.setName("手机号状态");
					idName.setValue("空号");
					idName = idNameService.selectOne(idName);
					Long blankId = idName.getId();

					inStream = file.getInputStream();
					Map<String, Object> map = ImportUtils.getPhoneNumsByExcel(inStream, phoneInformationService, blackId,
					blankId);

					@SuppressWarnings("unchecked")
					List<String> phoneNumList = (List<String>) map.get("phoneNumList");
					@SuppressWarnings("unchecked")
					List<Integer> idList = (List<Integer>) map.get("idList");

					if (CommonUtils.isEmpty(phoneNumList) || CommonUtils.isEmpty(idList))
					{
						return AjaxResult.errorInstance("手机号信息有误");
					}

					// 遍历每一个手机号，获取它的归属地以及运营商
					List<PhoneAttribution> phoneAttributionList = phoneAttributionService.selectListByIds(idList);

					if (CommonUtils.isEmpty(phoneAttributionList))
					{
						return AjaxResult.errorInstance("手机号信息有误");
					}

					Long startTime = System.currentTimeMillis();
					AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
					String key = "numberProcessNew_adminUser_" + adminUser.getId();
					Jedis2Utils redis = Jedis2Utils.getInstance();
					SortSet sortSet = redis.SORTSET;
					int score = 0;
					IDUtils idUtils = new IDUtils(0, 0);

					for (int i = 0; i < phoneAttributionList.size(); i++)
					{
						PhoneAttribution phoneAttribution = phoneAttributionList.get(i);
						Iterator<String> iterator = phoneNumList.iterator();
						while (iterator.hasNext())
						{
							String phoneNum = iterator.next();
							if ((phoneAttribution.getId() + "").equals(phoneNum.substring(0, 7)))
							{
								NumberProcessNew numberProcessNew = new NumberProcessNew();
								numberProcessNew.setId(idUtils.nextId());
								numberProcessNew.setScore(score);
								numberProcessNew.setPhoneNum(phoneNum);
								numberProcessNew.setOperator(phoneAttribution.getOperator());
								numberProcessNew.setProvince(phoneAttribution.getProvince());
								numberProcessNew.setCity(phoneAttribution.getCity());
								numberProcessNew.setContent("");
								numberProcessNew.setLength(0);
								numberProcessNew.setSegment(0);
								String numberProcessNewStr = JsonUtils.toJson(numberProcessNew);
								sortSet.zadd(key, score, numberProcessNewStr);
								score++;
								iterator.remove();
							}
						}
					}

					System.out.println("耗时：" + (System.currentTimeMillis() - startTime));

					return AjaxResult.successInstance("文件上传成功");
				} catch (InvalidFormatException | IOException e)
				{
					throw new RuntimeException("Excel文件解析出错了！", e);
				} finally
				{
					UploadUtils.closeQuietly(inStream);
				}
	}
}
