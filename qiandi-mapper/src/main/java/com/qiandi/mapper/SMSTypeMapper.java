package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.SMSType;
import com.qiandi.pojo.SMSTypeData;

public interface SMSTypeMapper extends IMapper<SMSType>
{

	List<SMSTypeData> selectData(Map<String, Object> param);

	void deleteByArray(Long[] ids);

	List<SMSTypeData> selectByArray(Long[] ids);

}
