package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.TransferRecord;

public interface TransferRecordMapper extends IMapper<TransferRecord>
{
	List<Map<String, Object>> customSelect(Map<String, Object> map);
	List<Map<String, Object>> selectStatistics();
}
