package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.QianShangUserManagerData;
import com.qiandi.pojo.QianShangUserSetting;
import com.qiandi.table.QianShangUserSettingTable;

public interface QianShangUserSettingMapper extends IMapper<QianShangUserSetting>
{
	public double selectPriceById(Long qianShangUserId);

	public List<QianShangUserManagerData> selectManagerData(Map<String, Object> param);

	public List<QianShangUserManagerData> selectByArray(Long[] ids);

	@SelectProvider(type = QianShangUserSettingTable.class, method = "deleteByArray")
	public void deleteByArray(Long[] ids, String string);
}
