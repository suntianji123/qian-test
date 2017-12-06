package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.MenberUserSettingChannelAisleHistory;
import com.qiandi.pojo.MenberUserSettingChannelAisleHistoryData;

public interface MenberUserSettingChannelAisleHistoryMapper extends IMapper<MenberUserSettingChannelAisleHistory>
{

	List<MenberUserSettingChannelAisleHistoryData> selectData(Map<String, Object> param);

	void deleteByArray(Long[] ids);

	List<MenberUserSettingChannelAisleHistoryData> selectByArray(Long[] ids);

}
