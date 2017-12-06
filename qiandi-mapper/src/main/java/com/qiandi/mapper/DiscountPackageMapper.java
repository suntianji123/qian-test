package com.qiandi.mapper;

import java.util.Map;

import com.qiandi.pojo.DiscountPackage;

public interface DiscountPackageMapper extends IMapper<DiscountPackage>
{

	void deleteByArray(Long[] ids);

	void updateShelvedByArray(Map<String, Object> param);

	void updateShelved(Map<String, Object> param);

	void updateShelvedTrueByArray(Long[] ids);

	void updateShelvedFalseByArray(Long[] ids);

}
