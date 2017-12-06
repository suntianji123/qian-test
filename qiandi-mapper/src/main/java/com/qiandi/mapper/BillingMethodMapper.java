package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.BillingMethod;
import com.qiandi.pojo.BillingMethodData;

public interface BillingMethodMapper extends IMapper<BillingMethod>
{

	List<BillingMethodData> selectData(Map<String, Object> param);

	void deleteByArray(Long[] ids);

	List<BillingMethodData> selectByArray(Long[] ids);

}
