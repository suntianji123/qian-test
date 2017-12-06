package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.AisleRechargeRecord;
import com.qiandi.pojo.AisleRechargeRecordData;

public interface AisleRechargeRecordMapper extends IMapper<AisleRechargeRecord>
{

	List<AisleRechargeRecordData> selectData(Map<String, Object> param);


	void updateAuditResultByArray(Map<String, Object> param);

	void deleteByArray(Long[] ids);

	List<AisleRechargeRecordData> selectByArray(Long[] ids);

}
