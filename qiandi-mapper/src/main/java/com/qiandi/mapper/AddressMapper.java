package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.Address;
import com.qiandi.pojo.AddressData;

public interface AddressMapper extends IMapper<Address>
{

	void updateIsDefault(Address param);

	List<AddressData> selectData(Map<String, Object> param);

	void deleteByArray(Long[] ids);

	List<AddressData> selectByArray(Long[] ids);

}
