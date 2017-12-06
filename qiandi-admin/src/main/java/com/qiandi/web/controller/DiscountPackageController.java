package com.qiandi.web.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.github.pagehelper.PageInfo;
import com.qiandi.pojo.DiscountPackage;
import com.qiandi.service.DiscountPackageService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/discountPackage")
public class DiscountPackageController
{
	@Autowired
	private DiscountPackageService discountPackageService;

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Boolean isShelved,Integer pageNum,Integer pageSize)
	{
		if(pageNum==null)
		{
			pageNum =1;
		}
		if(pageSize==null)
		{
			pageSize =2;
		}
		
		DiscountPackage discountPackage = new DiscountPackage();
		discountPackage.setIsShelved(isShelved);
		PageInfo<DiscountPackage> pageInfo = discountPackageService.pageData(pageNum, pageSize, discountPackage);
		return AjaxResult.successInstance(pageInfo);
	}

	@RequestMapping("/updateShelved.do")
	public @ResponseBody AjaxResult updateShelved(Long[] ids, Long id, Boolean isShelved)
	{

		if (ids != null && ids.length > 0)
		{
			if (isShelved)
			{
				discountPackageService.updateShelvedTrueByArray(ids);
			} else
			{
				discountPackageService.updateShelvedFalseByArray(ids);
			}

		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			if (id != null)
			{
				param.put("id", id);
				param.put("isShelved", isShelved);
				discountPackageService.updateShelved(param);
			}
		}

		return AjaxResult.successInstance("上架成功");
	}

	@RequestMapping("/delete.do")
	public @ResponseBody AjaxResult delete(Long[] ids, Long id)
	{
		if (ids != null && ids.length > 0)
		{
			discountPackageService.deleteByArray(ids);
		} else
		{
			if (id != null)
			{
				discountPackageService.delete(id);
			}
		}

		return AjaxResult.successInstance("删除成功");
	}

	@RequestMapping("/updateSubmit.do")
	public @ResponseBody AjaxResult updateSubmit(Long id, Integer price, Integer totalCount)
	{
		if (id == null)
		{
			return AjaxResult.errorInstance("id不能为空");
		}
		if (price == null)
		{
			return AjaxResult.errorInstance("价格不能为空");
		}
		if (totalCount == null)
		{
			return AjaxResult.errorInstance("总条数不能为空");
		}

		DiscountPackage discountPackage = discountPackageService.selectOne(id);
		if (discountPackage == null)
		{
			return AjaxResult.errorInstance("优惠券不存在");
		} else
		{
			discountPackage.setCreateTime(System.currentTimeMillis());
			discountPackage.setPrice(price);
			discountPackage.setTotalCount(totalCount);
			discountPackageService.update(discountPackage);
		}

		return AjaxResult.successInstance("修改成功");

	}

	@RequestMapping("/addNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(Integer totalCount, Integer price, Boolean isShelved)
	{
		if (totalCount == null)
		{
			return AjaxResult.errorInstance("短信条数不能为空");
		}
		if (price == null)
		{
			return AjaxResult.errorInstance("单价不能为空");
		}
		
		DiscountPackage discountPackage = new DiscountPackage();
		discountPackage.setId(new IDUtils(0,0).nextId());
		discountPackage.setCreateTime(System.currentTimeMillis());
		discountPackage.setIsShelved(isShelved);
		discountPackage.setPrice(price);
		discountPackage.setTotalCount(totalCount);
		
		discountPackageService.insert(discountPackage);
		return AjaxResult.successInstance("添加成功");
	}
}
