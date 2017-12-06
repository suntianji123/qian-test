package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.Address;
import com.qiandi.pojo.PhoneInformation;
import com.qiandi.pojo.PhoneInformationData;
import com.qiandi.table.PhoneInformationTable;

public interface PhoneInformationMapper extends IMapper<PhoneInformation>
{

	void updateIsDefault(Address param);

	List<PhoneInformation> selectListByBlack(List<String> phoneNumList);

	List<PhoneInformation> selectListBySpace(List<String> phoneNumList);


	List<PhoneInformationData> selectData(Map<String, Object> param);

	void deleteByArray(Long[] ids);

	List<PhoneInformationData> selectByArray(Long[] ids);

	List<PhoneInformation> selectBlackAndBlankList(Map<String, Long> param);

	@SelectProvider(type = PhoneInformationTable.class, method = "selectPhoneNumList")
	List<String> selectPhoneNumList();

}
