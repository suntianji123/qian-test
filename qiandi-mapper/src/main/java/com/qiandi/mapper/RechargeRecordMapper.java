package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.RechargeRecord;
import com.qiandi.pojo.RechargeRecordData;
import com.qiandi.pojo.RechargeRecordDataForAdmin;
import com.qiandi.pojo.RechargeRecordForUpdate;
import com.qiandi.table.RechargeRecordTable;

public interface RechargeRecordMapper extends IMapper<RechargeRecord>
{

	public List<RechargeRecordData> selectData(Map<String, Object> map);

	public RechargeRecordData selectOneData(Integer id);

	public void insertByWeiXinPay(List<Map<String, Object>> orderItemList);

	public void insertByAccount(List<Map<String, Object>> paramList);

	public Map<String, Object> selectRechargeRecordTotalCount(Long menberUserId);

	public List<RechargeRecordData> selectRechargeRecordList(Long menberUserId);

	public List<RechargeRecordData> selectRechargeRecordListById(List<Long> idList);

	@SelectProvider(type = RechargeRecordTable.class, method = "selectAdminData")
	public List<RechargeRecordDataForAdmin> selectAdminData(Map<String, Object> param);

	@SelectProvider(type = RechargeRecordTable.class, method = "deleteByArray")
	public void deleteByArray(Long[] ids, String cc);

	@SelectProvider(type = RechargeRecordTable.class, method = "selectAdminByArray")
	public List<RechargeRecordDataForAdmin> selectAdminByArray(Long[] ids, String cc);

	@SelectProvider(type = RechargeRecordTable.class, method = "selectDataForUpdate")
	public List<RechargeRecordForUpdate> selectDataForUpdate(RechargeRecord rechargeRecord);

	@SelectProvider(type = RechargeRecordTable.class, method = "updateLateFeeItem")
	public void updateLateFeeItem(List<RechargeRecordForUpdate> rechargeRecordList, String cc);

	@SelectProvider(type = RechargeRecordTable.class, method = "selectListByRepaymentRecordId")
	public List<RechargeRecordForUpdate> selectListByRepaymentRecordId(Long[] repaymentRecordIds, String string);

}
