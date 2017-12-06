package com.qiandi.web.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.qiandi.pojo.IdName;
import com.qiandi.pojo.LateFeeFormula;
import com.qiandi.service.IdNameService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.JsonUtils;

/*
 * 滞纳金计算公式控制器
 */
@RequestMapping("/lateFeeFormula")
@Controller
public class LateFeeFormulaController
{
	@Autowired
	private IdNameService idNameService;

	@RequestMapping("/update.do")
	public @ResponseBody AjaxResult update(Long id, Integer percent)
	{
		if (id == null)
		{
			return AjaxResult.errorInstance("将要修改的滞纳金计算公式的id不能为空");
		}

		if (percent == null || percent <= 0)
		{
			return AjaxResult.errorInstance("百分比必须为大于0的整数");
		}

		IdName idName = new IdName();
		idName.setId(id);
		idName.setName("滞纳金计算公式");
		idName = idNameService.selectOne(idName);
		if (idName == null)
		{
			return AjaxResult.errorInstance("滞纳金计算公式不存在");
		}

		LateFeeFormula lateFeeFormula = JsonUtils.toBean(idName.getValue(), LateFeeFormula.class);
		lateFeeFormula.setPercent(percent);
		Map<String,Object> map = new HashMap<String,Object>();
		map.put("percent", lateFeeFormula.getPercent());
		map.put("type", lateFeeFormula.getType());

		String value = JsonUtils.toJson(map);
		idName.setValue(value);

		idNameService.update(idName);

		return AjaxResult.successInstance("修改成功");

	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search()
	{
		IdName param = new IdName();
		param.setName("滞纳金计算公式");
		List<IdName> idNameList = idNameService.selectList(param);
		List<LateFeeFormula> lateFeeFormulaList = new ArrayList<>();
		for (IdName idName : idNameList)
		{
			String value = idName.getValue();
			LateFeeFormula lateFeeFormula = JsonUtils.toBean(value, LateFeeFormula.class);
			lateFeeFormula.setId(idName.getId());
			lateFeeFormulaList.add(lateFeeFormula);
		}
		return AjaxResult.successInstance(lateFeeFormulaList);
	}
}
