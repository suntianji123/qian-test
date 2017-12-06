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
import com.qiandi.pojo.AisleData;
import com.qiandi.pojo.AisleDataForAisle;
import com.qiandi.pojo.ChannelDataForAisle;
import com.qiandi.pojo.MenberUserDataForAisle;
import com.qiandi.pojo.SMSType;
import com.qiandi.service.AisleSMSTypeService;
import com.qiandi.service.AisleService;
import com.qiandi.service.ChannelService;
import com.qiandi.service.MenberUserSettingService;
import com.qiandi.service.SMSTypeService;
import com.qiandi.util.AjaxResult;
import com.qiandi.util.CommonUtils;
import com.qiandi.util.ExportToExcelUtil;
import com.qiandi.util.IDUtils;

@Controller
@RequestMapping("/aisle")
public class AisleController
{
	@Autowired
	private AisleService aisleService;

	@Autowired
	private SMSTypeService smsTypeService;

	@Autowired
	private MenberUserSettingService menberUserSettingService;

	@Autowired
	private ChannelService channelService;

	// 开启或者关闭通道
	@RequestMapping("/updateIsOn.do")
	public @ResponseBody AjaxResult updateIsOn(Boolean isOn, Long[] ids)
	{
		if (ids == null || ids.length <= 0)
		{
			return AjaxResult.errorInstance("没有选择需要打开或者关闭通道的Id");
		}

		aisleService.updateIsOnByArray(isOn, ids);

		return AjaxResult.successInstance("开启或者关闭通道成功");
	}


	// 批量更改提交
	@RequestMapping("/updateMenberUserOrChannelAisleSubmit.do")
	public @ResponseBody AjaxResult updateMenberUserOrChannelAisleSubmit(Long[] splitAisleIds, Long yiDongAisleId,
			Long lianTongAisleId, Long dianXingAisleId, Long[] menberUserOrChannelIds, Boolean isMenberUser)
	{
		if (splitAisleIds == null || splitAisleIds.length <= 0)
		{
			return AjaxResult.errorInstance("没有选择需要删除的通道");
		}
		if (yiDongAisleId == null && lianTongAisleId == null && dianXingAisleId == null)
		{
			return AjaxResult.errorInstance("没有指定将要被绑定的通道id");
		}
		if (menberUserOrChannelIds == null || menberUserOrChannelIds.length <= 0)
		{
			return AjaxResult.errorInstance("没有选择需要绑定的会员或者频道");
		}
		if (isMenberUser == null)
		{
			return AjaxResult.errorInstance("没有选择需要绑定的账户类型");
		}
		if (CommonUtils.contains(splitAisleIds, yiDongAisleId) || CommonUtils.contains(splitAisleIds, lianTongAisleId)
				|| CommonUtils.contains(splitAisleIds, dianXingAisleId))
		{
			return AjaxResult.errorInstance("该通道将要被删除，你不能绑定这个通道");
		}
		Aisle aisle = new Aisle();
		if (yiDongAisleId != null)
		{
			aisle.setId(yiDongAisleId);
			aisle.setSupportOperator(4);
			aisle.setIsOn(false);
			if (!aisleService.isExisted(aisle))
			{
				return AjaxResult.errorInstance("该通道不支持移动或者已经被关闭");
			}
		}

		aisle = new Aisle();
		if (lianTongAisleId != null)
		{
			aisle.setId(lianTongAisleId);
			aisle.setSupportOperator(2);
			aisle.setIsOn(false);
			if (!aisleService.isExisted(aisle))
			{
				return AjaxResult.errorInstance("该通道不支持联通或者已经被关闭");
			}
		}

		aisle = new Aisle();
		if (dianXingAisleId != null)
		{
			aisle.setId(dianXingAisleId);
			aisle.setSupportOperator(1);
			aisle.setIsOn(false);
			if (!aisleService.isExisted(aisle))
			{
				return AjaxResult.errorInstance("该通道不支持电信或者已经被关闭");
			}
		}


		if (isMenberUser)
		{
			menberUserSettingService.updateMenberUserSettingAisleIdByArray(yiDongAisleId, lianTongAisleId,
					dianXingAisleId, menberUserOrChannelIds);
		} else
		{
			channelService.updateAisleIdByArray(yiDongAisleId, lianTongAisleId, dianXingAisleId,
					menberUserOrChannelIds);
		}

		return AjaxResult.successInstance("绑定通道成功");

	}



	// 加载所有支持移动、联通或电信的通道
	@RequestMapping("/loadAisleSingleType.do")
	public @ResponseBody AjaxResult loadAisleSingleType(Long[] aisleIds)
	{
		if (aisleIds == null || aisleIds.length <= 0)
		{
			return AjaxResult.errorInstance("没有选择通道删除的通道记录ID");
		}

		List<AisleDataForAisle> yiDongAisleDataForAisleList = aisleService.selectAisleDataForAisle(aisleIds, "0");
		List<AisleDataForAisle> lianTongAisleDataForAisleList = aisleService.selectAisleDataForAisle(aisleIds, "1");
		List<AisleDataForAisle> dianXingAisleDataForAisleList = aisleService.selectAisleDataForAisle(aisleIds, "2");

		Map<String, List<AisleDataForAisle>> map = new HashMap<String, List<AisleDataForAisle>>();
		map.put("yiDongAisles", yiDongAisleDataForAisleList);
		map.put("lianTongAisles", lianTongAisleDataForAisleList);
		map.put("dianXingAisles", dianXingAisleDataForAisleList);
		return AjaxResult.successInstance(map);
	}

	// 根据通道id加载出所有绑定了该通道的会员、频道列表
	@RequestMapping("/loadAisleById.do")
	public @ResponseBody AjaxResult loadMenberUser(Boolean isMenberUser, Long[] aisleIds, String aisleType,
			Integer pageSize, Integer pageNum)
	{
		if (aisleIds == null || aisleIds.length <= 0)
		{
			return AjaxResult.errorInstance("没有选择通道删除的通道记录ID");
		}

		if (CommonUtils.isEmpty(aisleType)
				|| (!"0".equals(aisleType) && !"1".equals(aisleType) && !"2".equals(aisleType)))
		{
			return AjaxResult.errorInstance("没有选择通道的类型：移动、联通或电信");
		}

		if (isMenberUser == null)
		{
			return AjaxResult.errorInstance("没有选择将要将要绑定的账户类型");
		}
		if (pageNum == null)
		{
			pageNum = 1;
		}
		if (pageSize == null)
		{
			pageSize = 2;
		}

		PageInfo<MenberUserDataForAisle> pageInfo1 = new PageInfo<MenberUserDataForAisle>();
		PageInfo<ChannelDataForAisle> pageInfo2 = new PageInfo<ChannelDataForAisle>();
		if(isMenberUser)
		{
			//查询所有的会员姓名，手机号
			pageInfo1 = menberUserSettingService
					.selectDataForAisle(pageNum, pageSize, aisleIds, aisleType);
			return AjaxResult.successInstance(pageInfo1);
		}else
		{
			pageInfo2 = channelService.selectDataForAisle(pageNum, pageSize, aisleIds, aisleType);
			return AjaxResult.successInstance(pageInfo2);
		}

	}

	@RequestMapping("/exportToExcel.do")
	public void exportToExcel(Long[] ids, Long beginTime, Long endTime, String name, String company, String contact,
			String phoneNum, String qqNum, String weiXinNum, String email, String province, String city, String spNum,
			String number, String addAdminUserName, Boolean billingMethod, Boolean paymentMethod,
			Integer supportOperator, Boolean isSupportReport, Boolean recivedFailRange, Long smsTypeId,
			HttpServletRequest req, HttpServletResponse resp)
	{
		List<AisleData> balanceWarningStatusList = new ArrayList<AisleData>();
		if (ids != null && ids.length > 0)
		{
			balanceWarningStatusList = aisleService.selectByArray(ids);
		} else
		{
			Map<String, Object> param = new HashMap<String, Object>();
			param.put("beginTime", beginTime);
			param.put("endTime", endTime);
			param.put("name", CommonUtils.isEmpty(name) ? null : name);
			param.put("company", CommonUtils.isEmpty(company) ? null : company);
			param.put("contact", CommonUtils.isEmpty(contact) ? null : contact);
			param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
			param.put("qqNum", CommonUtils.isEmpty(qqNum) ? null : qqNum);
			param.put("weiXinNum", CommonUtils.isEmpty(weiXinNum) ? null : weiXinNum);
			param.put("email", CommonUtils.isEmpty(email) ? null : email);
			param.put("province", CommonUtils.isEmpty(province) ? null : province);
			param.put("city", CommonUtils.isEmpty(city) ? null : city);
			param.put("spNum", CommonUtils.isEmpty(spNum) ? null : spNum);
			param.put("number", CommonUtils.isEmpty(number) ? null : number);
			param.put("addAdminUserName", CommonUtils.isEmpty(addAdminUserName) ? null : addAdminUserName);
			param.put("billingMethod", billingMethod);
			param.put("paymentMethod", paymentMethod);
			param.put("supportOperator", supportOperator);
			param.put("isSupportReport", isSupportReport);
			param.put("recivedFailRange", recivedFailRange);
			param.put("smsTypeId", smsTypeId);
			balanceWarningStatusList = aisleService.selectData(param);
		}

		ExportToExcelUtil<AisleData> excelUtil = new ExportToExcelUtil<AisleData>();
		OutputStream out = null;
		try
		{
			out = resp.getOutputStream();
			excelUtil.setResponseHeader(resp, "通道管理统计表");
			String[] headers = { "记录ID", "通道名称", "管理平台地址", "登录账户", "登录密码", "单位名称", "营业执照", "联系人", "手机号码",
					"身份", "QQ号码", "微信号码", "邮箱", "省", "市", "注册日期", "注册资金", "sp号", "码号", "计费单价", "支持运营商",
					"计费单价", "付费方式", "状态报告", "失败返回范围", "失败返回时间", "信息类型", "添加日期", "添加人" };
			String[] columns = { "id", "name", "manageAddress", "accountName",
					"password", "company", "businessLicense", "contact", "phoneNum", "identity", "qqNum", "weiXinNum",
					"email", "province", "city", "registerTimeStr", "registerMoney", "spNum", "number", "price",
					"supportOperatorStr", "billingMethodStr", "paymentMethodStr",
					"isSupportReportStr", "recivedFailRangeStr", "recivedFailReturnTime", "smsTypeStr", "createTimeStr",
					"addAdminUserName" };
			try
			{
				excelUtil.exportExcel(headers, columns, balanceWarningStatusList, out, req, "");
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

	@Autowired
	private AisleSMSTypeService aisleSMSTypeService;

	@RequestMapping("/delete.do")
	public @ResponseBody AjaxResult delete(Long[] ids)
	{
		if (ids == null)
		{
			return AjaxResult.errorInstance("您没有选择需要删除的记录");
		}
		aisleService.deleteTwo(ids);
		return AjaxResult.successInstance("删除成功");
	}

	@RequestMapping("/loadSelectSMSType.do")
	public @ResponseBody AjaxResult loadSelectSMSType(Long aisleId)
	{
		List<SMSType> smsTypeList = aisleSMSTypeService.selectSecondListByFirstId(aisleId);
		return AjaxResult.successInstance(smsTypeList);
	}

	@RequestMapping("/updateSubmit.do")
	public @ResponseBody AjaxResult updateSubmit(Long id,String name, String manageAddress, String accountName, String password,
			String company, String businessLicense, String contact, String identity, String phoneNum, String qqNum,
			String weiXinNum, String email, String province, String city, Long registerTime, Integer registerMoney,
			String spNum, String number, Integer supportOperator, Integer price, Boolean billingMethod,
			Boolean paymentMethod, Boolean isSupportReport,
			Boolean recivedFailRange, String recivedFailReturnTime, Long[] smsTypeIds, HttpServletRequest req)
	{
		if (CommonUtils.isEmpty(name))
		{
			return AjaxResult.errorInstance("通道名称不能为空");
		}
		if (CommonUtils.isEmpty(manageAddress))
		{
			return AjaxResult.errorInstance("管理平台地址不能为空");
		}
		if (CommonUtils.isEmpty(accountName))
		{
			return AjaxResult.errorInstance("登录账户不能为空");
		}
		if (CommonUtils.isEmpty(password))
		{
			return AjaxResult.errorInstance("登录密码不能为空");
		}
		if (CommonUtils.isEmpty(company))
		{
			return AjaxResult.errorInstance("单位名称不能为空");
		}

		if (CommonUtils.isEmpty(businessLicense))
		{
			return AjaxResult.errorInstance("营业执照不能为空");
		}
		if (CommonUtils.isEmpty(contact))
		{
			return AjaxResult.errorInstance("联系人不能为空");
		}
		if (CommonUtils.isEmpty(identity))
		{
			return AjaxResult.errorInstance("身份不能为空");
		}

		if (!CommonUtils.isPhone(phoneNum))
		{
			return AjaxResult.errorInstance("手机号格式不正确");
		}

		if (CommonUtils.isEmpty(qqNum))
		{
			return AjaxResult.errorInstance("qq号不能为空");
		}
		if (CommonUtils.isEmpty(weiXinNum))
		{
			return AjaxResult.errorInstance("微信号不能为空");
		}

		if (CommonUtils.isEmpty(email))
		{
			return AjaxResult.errorInstance("邮箱不能为空");
		}
		if (CommonUtils.isEmpty(province))
		{
			return AjaxResult.errorInstance("省不能为空");
		}
		if (CommonUtils.isEmpty(city))
		{
			return AjaxResult.errorInstance("市不能为空");
		}
		if (registerTime == null)
		{
			return AjaxResult.errorInstance("注册日期不能为空");
		}
		if (registerMoney == null)
		{
			return AjaxResult.errorInstance("注册资本不能为空");
		}
		if (CommonUtils.isEmpty(spNum))
		{
			return AjaxResult.errorInstance("sp号不能为空");
		}
		if (CommonUtils.isEmpty(number))
		{
			return AjaxResult.errorInstance("码号不能为空");
		}
		if (supportOperator == null || supportOperator > 7 || supportOperator < 0)
		{
			return AjaxResult.errorInstance("支持运营商不正确");
		}
		if (price == null)
		{
			return AjaxResult.errorInstance("计费单价不能为空");
		}
		if (billingMethod == null)
		{
			return AjaxResult.errorInstance("计费方式不能为空");
		}
		if (paymentMethod == null)
		{
			return AjaxResult.errorInstance("付费方式不能为空");
		}
		if (isSupportReport == null)
		{
			return AjaxResult.errorInstance("是否支持状态报告选项不能为空");
		}
		if (recivedFailRange == null)
		{
			return AjaxResult.errorInstance("失败返回范围不能为空");
		}
		if (CommonUtils.isEmpty(recivedFailReturnTime))
		{
			return AjaxResult.errorInstance("失败返回时间不能为空");
		}
		if (smsTypeIds == null)
		{
			return AjaxResult.errorInstance("信息类型不能为空");
		}

		// 唯一性检查
		Aisle aisle = aisleService.selectOne(id);
		if (aisle == null)
		{
			return AjaxResult.errorInstance("通道记录不存在");
		}

		Aisle aisle2 = new Aisle();
		aisle2.setName(name);
		aisle2 = aisleService.selectOne(aisle2);
		if (aisle2 != null && !aisle2.getName().equals(aisle.getName()))
		{
			return AjaxResult.errorInstance("通道名称已经存在，您不能重复添加");
		}
		
		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		aisle.setAddAdminUserId(adminUser.getId());
		aisle.setCreateTime(System.currentTimeMillis());
		aisle.setName(name);
		aisle.setManageAddress(manageAddress);
		aisle.setAccountName(accountName);
		aisle.setPassword(password);
		aisle.setCompany(company);
		aisle.setBusinessLicense(businessLicense);
		aisle.setContact(contact);
		aisle.setIdentity(identity);
		aisle.setPhoneNum(phoneNum);
		aisle.setQqNum(qqNum);
		aisle.setWeiXinNum(weiXinNum);
		aisle.setEmail(email);
		aisle.setProvince(province);
		aisle.setCity(city);
		aisle.setPrice(price);
		aisle.setRegisterTime(registerTime);
		aisle.setRegisterMoney(registerMoney);
		aisle.setSpNum(spNum);
		aisle.setNumber(number);
		aisle.setSupportOperator(supportOperator);
		aisle.setBillingMethod(billingMethod);
		aisle.setPaymentMethod(paymentMethod);
		aisle.setIsSupportReport(isSupportReport);
		aisle.setRecivedFailRange(recivedFailRange);
		aisle.setRecivedFailReturnTime(recivedFailReturnTime);
		
		aisleService.updateTwo(smsTypeIds, aisle);

		return AjaxResult.successInstance("修改成功");

	}

	@RequestMapping("/addNewSubmit.do")
	public @ResponseBody AjaxResult addNewSubmit(String name, String manageAddress, String accountName, String password,
			String company, String businessLicense, String contact, String identity, String phoneNum, String qqNum,
			String weiXinNum, String email, String province, String city, Long registerTime, Integer registerMoney,
			String spNum, String number, Integer supportOperator, Integer price, Boolean billingMethod,
			Boolean paymentMethod, Boolean isSupportReport,
			Boolean recivedFailRange, String recivedFailReturnTime, Long[] smsTypeIds, HttpServletRequest req)
	{
		if (CommonUtils.isEmpty(name))
		{
			return AjaxResult.errorInstance("通道名称不能为空");
		}
		if (CommonUtils.isEmpty(manageAddress))
		{
			return AjaxResult.errorInstance("管理平台地址不能为空");
		}
		if (CommonUtils.isEmpty(accountName))
		{
			return AjaxResult.errorInstance("登录账户不能为空");
		}
		if (CommonUtils.isEmpty(password))
		{
			return AjaxResult.errorInstance("登录密码不能为空");
		}
		if (CommonUtils.isEmpty(company))
		{
			return AjaxResult.errorInstance("单位名称不能为空");
		}

		if (CommonUtils.isEmpty(businessLicense))
		{
			return AjaxResult.errorInstance("营业执照不能为空");
		}
		if (CommonUtils.isEmpty(contact))
		{
			return AjaxResult.errorInstance("联系人不能为空");
		}
		if (CommonUtils.isEmpty(identity))
		{
			return AjaxResult.errorInstance("身份不能为空");
		}

		if (!CommonUtils.isPhone(phoneNum))
		{
			return AjaxResult.errorInstance("手机号格式不正确");
		}

		if (CommonUtils.isEmpty(qqNum))
		{
			return AjaxResult.errorInstance("qq号不能为空");
		}
		if (CommonUtils.isEmpty(weiXinNum))
		{
			return AjaxResult.errorInstance("微信号不能为空");
		}

		if (CommonUtils.isEmpty(email))
		{
			return AjaxResult.errorInstance("邮箱不能为空");
		}
		if (CommonUtils.isEmpty(province))
		{
			return AjaxResult.errorInstance("省不能为空");
		}
		if (CommonUtils.isEmpty(city))
		{
			return AjaxResult.errorInstance("市不能为空");
		}
		if (registerTime == null)
		{
			return AjaxResult.errorInstance("注册日期不能为空");
		}
		if (registerMoney == null)
		{
			return AjaxResult.errorInstance("注册资本不能为空");
		}
		if (CommonUtils.isEmpty(spNum))
		{
			return AjaxResult.errorInstance("sp号不能为空");
		}
		if (CommonUtils.isEmpty(number))
		{
			return AjaxResult.errorInstance("码号不能为空");
		}
		if (supportOperator == null || supportOperator > 7 || supportOperator < 0)
		{
			return AjaxResult.errorInstance("支持运营商不正确");
		}
		if (price == null)
		{
			return AjaxResult.errorInstance("移动单价不能为空");
		}
		if (billingMethod == null)
		{
			return AjaxResult.errorInstance("计费方式不能为空");
		}
		if (paymentMethod == null)
		{
			return AjaxResult.errorInstance("付费方式不能为空");
		}
		if (isSupportReport == null)
		{
			return AjaxResult.errorInstance("是否支持状态报告选项不能为空");
		}
		if (recivedFailRange == null)
		{
			return AjaxResult.errorInstance("失败返回范围不能为空");
		}
		if (CommonUtils.isEmpty(recivedFailReturnTime))
		{
			return AjaxResult.errorInstance("失败返回时间不能为空");
		}
		if (smsTypeIds == null)
		{
			return AjaxResult.errorInstance("信息类型不能为空");
		}

		// 唯一性检查
		Aisle aisle = new Aisle();
		aisle.setName(name);
		if (aisleService.isExisted(aisle))
		{
			return AjaxResult.errorInstance("通道名称已经存在，不能重复添加");
		}

		AdminUser adminUser = (AdminUser) req.getSession().getAttribute("adminUser");
		aisle = new Aisle();
		aisle.setId(new IDUtils(0, 0).nextId());
		aisle.setAddAdminUserId(adminUser.getId());
		aisle.setCreateTime(System.currentTimeMillis());
		aisle.setName(name);
		aisle.setManageAddress(manageAddress);
		aisle.setAccountName(accountName);
		aisle.setPassword(password);
		aisle.setCompany(company);
		aisle.setBusinessLicense(businessLicense);
		aisle.setContact(contact);
		aisle.setIdentity(identity);
		aisle.setPhoneNum(phoneNum);
		aisle.setQqNum(qqNum);
		aisle.setWeiXinNum(weiXinNum);
		aisle.setEmail(email);
		aisle.setProvince(province);
		aisle.setCity(city);
		aisle.setRegisterTime(registerTime);
		aisle.setRegisterMoney(registerMoney);
		aisle.setSpNum(spNum);
		aisle.setNumber(number);
		aisle.setSupportOperator(supportOperator);
		aisle.setPrice(price);
		aisle.setIsOn(false);
		aisle.setBillingMethod(billingMethod);
		aisle.setPaymentMethod(paymentMethod);
		aisle.setIsSupportReport(isSupportReport);
		aisle.setRecivedFailRange(recivedFailRange);
		aisle.setRecivedFailReturnTime(recivedFailReturnTime);

		aisleService.insertTwo(smsTypeIds, aisle);

		return AjaxResult.successInstance("添加成功");
	}

	@RequestMapping("/loadSMSType.do")
	public @ResponseBody AjaxResult loadSMSType()
	{
		List<SMSType> smsTypeList =  smsTypeService.selectList();
		
		return AjaxResult.successInstance(smsTypeList);
	}

	@RequestMapping("/search.do")
	public @ResponseBody AjaxResult search(Long beginTime, Long endTime, String name, String company, String contact,
			String phoneNum, String qqNum, String weiXinNum, String email, String province, String city, String spNum,
			String number, String addAdminUserName, Boolean billingMethod, Boolean paymentMethod,
			Integer supportOperator, Boolean isSupportReport, Boolean recivedFailRange, Long smsTypeId, Boolean isOn,
			Integer pageNum, Integer pageSize)
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
		param.put("name", CommonUtils.isEmpty(name) ? null : name);
		param.put("company", CommonUtils.isEmpty(company) ? null : company);
		param.put("contact", CommonUtils.isEmpty(contact) ? null : contact);
		param.put("phoneNum", CommonUtils.isEmpty(phoneNum) ? null : phoneNum);
		param.put("qqNum", CommonUtils.isEmpty(qqNum) ? null : qqNum);
		param.put("weiXinNum", CommonUtils.isEmpty(weiXinNum) ? null : weiXinNum);
		param.put("email", CommonUtils.isEmpty(email) ? null : email);
		param.put("province", CommonUtils.isEmpty(province) ? null : province);
		param.put("city", CommonUtils.isEmpty(city) ? null : city);
		param.put("spNum", CommonUtils.isEmpty(spNum) ? null : spNum);
		param.put("number", CommonUtils.isEmpty(number) ? null : number);
		param.put("addAdminUserName", CommonUtils.isEmpty(addAdminUserName) ? null : addAdminUserName);
		param.put("billingMethod", billingMethod);
		param.put("paymentMethod", paymentMethod);
		param.put("supportOperator", supportOperator);
		param.put("isSupportReport", isSupportReport);
		param.put("recivedFailRange", recivedFailRange);
		param.put("isOn", isOn);
		param.put("smsTypeId", smsTypeId);
		
		PageInfo<AisleData> pageInfo = aisleService.pageData(pageNum, pageSize, param);
		return AjaxResult.successInstance(pageInfo);
		
	}
}
