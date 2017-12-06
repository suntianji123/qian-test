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
import com.qiandi.pojo.OrderItemData;
import com.qiandi.service.OrderItemService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;

@Controller
@RequestMapping("/orderItem")
public class OrderItemController
{
	@Autowired
	private OrderItemService orderItemService;

	@RequestMapping(value = "/exportToExcel.do")
	public void exportToExcel(Long beginTime, Long endTime, Long[] ids, HttpServletRequest req,
			HttpServletResponse resp, String menberUserAccountName, String menberUserName,
			String menberUserPhoneNum, String qianShangUserName)
	{
		List<OrderItemData> orderItemList = new ArrayList<OrderItemData>();
		if (ids != null && ids.length > 0)
		{
			orderItemList = orderItemService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("menberUserAccountName",
					CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
			param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
			param.put("menberUserPhoneNum", CommonUtils.isEmpty(menberUserPhoneNum) ? null : menberUserPhoneNum);
			param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);
			orderItemList = orderItemService.selectData(param);
		}

		ExportToExcelUtil<OrderItemData> excelUtil = new ExportToExcelUtil<OrderItemData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "购物车统计表");
			String[] headers = { "记录ID", "账户名", "真实姓名", "手机号", "会员账号级别", "所属乾商", "商品信息", "充值类型", "充值条数", "计费单价", "应付金额",
					"创建日期" };
			String[] columns = { "id", "menberUserAccountName", "menberUserName", "menberUserPhoneNum",
					"menberUserLevel",
					"qianShangUserName", "productTypeStr", "rechargeTypeStr", "subTotal", "priceStr",
					"amountPayAbleStr", "createTimeStr" };
			try
			{
				excelUtil.exportExcel(headers, columns, orderItemList, out, req, "");
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
	public @ResponseBody AjaxResult delete(Long[] ids, Long id)
	{


			if (ids != null && ids.length > 0)
			{
			orderItemService.deleteByArray(ids);
			} else
			{
				if (id != null)
				{
				orderItemService.delete(id);
				}
			}

			return AjaxResult.successInstance("删除成功！");
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, String menberUserAccountName,
			String menberUserName, String menberUserPhoneNum, String qianShangUserName, Integer pageSize,
			Integer pageNum)
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
		param.put("menberUserAccountName", CommonUtils.isEmpty(menberUserAccountName) ? null : menberUserAccountName);
		param.put("menberUserName", CommonUtils.isEmpty(menberUserName) ? null : menberUserName);
		param.put("menberUserPhoneNum", CommonUtils.isEmpty(menberUserPhoneNum) ? null : menberUserPhoneNum);
		param.put("qianShangUserName", CommonUtils.isEmpty(qianShangUserName) ? null : qianShangUserName);

		PageInfo<OrderItemData> pageInfo = orderItemService.pageData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);
	}
}
