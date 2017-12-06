package com.qiandi.mapper;

import java.util.List;

import com.qiandi.pojo.PhoneAttribution;

public interface PhoneAttributionMapper extends IMapper<PhoneAttribution>
{
	public List<PhoneAttribution> selectListByIds(List<Integer> ids);
}
