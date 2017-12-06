package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.RechargeRecordLateFee;
import com.qiandi.pojo.RechargeRecordLateFeeData;
import com.qiandi.table.RechargeRecordLateFeeTable;

public interface RechargeRecordLateFeeMapper extends IMapper<RechargeRecordLateFee>
{

	@SelectProvider(type = RechargeRecordLateFeeTable.class, method = "selectData")
	List<RechargeRecordLateFeeData> selectData(Map<String, Object> param);

}
