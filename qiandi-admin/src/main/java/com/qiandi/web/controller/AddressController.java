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
import com.qiandi.pojo.Address;
import com.qiandi.pojo.AddressData;
import com.qiandi.pojo.Area;
import com.qiandi.service.AddressService;
import com.qiandi.service.AreaService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;

@Controller
@RequestMapping("/address")
public class AddressController
{
	@Autowired
	private AddressService addressService;
	
	@Autowired
	private AreaService areaService;

	@RequestMapping(value = "/exportToExcel.do")
	public void exportToExcel(Long beginTime, Long endTime, Long[] ids, HttpServletRequest req,
			HttpServletResponse resp)
	{

		List<AddressData> adddressList = new ArrayList<AddressData>();
		if (ids != null && ids.length > 0)
		{
			adddressList = addressService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			adddressList = addressService.selectData(param);
		}

		ExportToExcelUtil<AddressData> excelUtil = new ExportToExcelUtil<AddressData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "收件地址统计表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "手机号", "会员账号级别", "所属乾商", "所在地区", "收件人", "添加人", "添加日期" };
			String[] columns = { "id", "menberUserAccountName", "menberUserName", "phoneNum", "level",
					"qianShangUserName", "viewAddress", "recipient", "addMenberUserName", "createTimeStr" };
			try
			{
				excelUtil.exportExcel(headers, columns, adddressList, out, req, "");
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
	public @ResponseBody AjaxResult deleteById(Long[] ids, Long id, HttpServletRequest req)
	{

		if (ids != null && ids.length > 0)
		{
			addressService.deleteByArray(ids);
		} else
		{
			if (id != null)
			{
				addressService.delete(id);
			}
		}

		return AjaxResult.successInstance("删除成功！");
	}

	@RequestMapping("/updateIsDefault.do")
	public @ResponseBody AjaxResult isDefault(Long id)
	{
		addressService.updateDefault(id);
		return AjaxResult.successInstance("设置默认地址成功");
	}

	@RequestMapping("/updateSubmit.do")
	public @ResponseBody AjaxResult updateSubmit(Long id, String detailAddress, String recipient, String phoneNum,
			Long provinceId, Long cityId, Long regionId, HttpServletRequest req, Boolean isDefault)
	{
		// 数据有效性校验
		if (CommonUtils.isEmpty(detailAddress))
		{
			return AjaxResult.errorInstance("详细地址不能为空");
		}
		if (CommonUtils.isEmpty(recipient))
		{
			return AjaxResult.errorInstance("收件人不能为空");
		}
		if (!CommonUtils.isPhone(phoneNum))
		{
			return AjaxResult.errorInstance("手机号格式不正确");
		}

		Address address = addressService.selectOne(id);
		if (address != null)
		{
			address.setDetailAddress(detailAddress);
			address.setRecipient(recipient);
			address.setPhoneNum(phoneNum);
			address.setCreateTime(System.currentTimeMillis());
			StringBuffer sb = new StringBuffer();

			// 查询出省的名字
			Area provinceArea = areaService.selectOne(provinceId);
			sb.append(provinceArea.getName());
			// 查询出市
			Area cityArea = areaService.selectOne(cityId);
			if (!provinceArea.getName().equals(cityArea.getName()))
			{
				sb.append(cityArea.getName());
			}

			if (regionId != null)
			{
				address.setAreaId(regionId);
				Area regionArea = areaService.selectOne(regionId);
				sb.append(regionArea.getName());
			} else
			{
				address.setAreaId(cityId);
			}
			sb.append(detailAddress);
			address.setViewAddress(sb.toString());
			address.setAddAdminUserId(0L);
			address.setIsDefault(isDefault);
			if (isDefault)
			{
				// 更新所有的其他地址为不是默认收货地址
				Address param = new Address();
				param.setIsDefault(false);
				addressService.updateIsDefault(param);
			}
			addressService.update(address);
		} else
		{
			return AjaxResult.errorInstance("地址不存在");
		}


		return AjaxResult.successInstance("修改成功！");

	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, String menberUserAccountName,
			String menberUserName,
			String phoneNum, String qianShangUserName, Long id, String addMenberUserName,Integer pageNum,Integer pageSize)
	{
		if(pageNum==null)
		{
			pageNum =1;
		}
		if(pageSize==null)
		{
			pageSize =2;
		}
		
		Map<String,Object> param = new HashMap<String,Object>();
		param.put("beginTime", beginTime);
		param.put("endTime", endTime);
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
		param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
		param.put("id", id);
		param.put("addMenberUserName", CommonUtils.isEmpty(addMenberUserName) ? null : qianShangUserName);
		
		
		PageInfo<AddressData> pageInfo = addressService.pageData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);
	}

}
