package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.Aisle;
import com.qiandi.pojo.AisleSMSType;
import com.qiandi.pojo.SMSType;

public interface AisleSMSTypeMapper extends IManyToManyMapper<AisleSMSType, Aisle, SMSType>
{

	List<Map<String, Object>> selectSendAll();


	void deleteByFirstIdArray(Long[] ids);

}
