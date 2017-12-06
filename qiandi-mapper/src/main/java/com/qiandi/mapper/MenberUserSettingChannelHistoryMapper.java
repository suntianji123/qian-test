package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.MenberUserSettingChannelHistory;
import com.qiandi.pojo.MenberUserSettingChannelHistoryData;

public interface MenberUserSettingChannelHistoryMapper extends IMapper<MenberUserSettingChannelHistory>
{

	List<MenberUserSettingChannelHistoryData> selectData(Map<String, Object> param);

	void deleteByArray(Long[] ids);

	List<MenberUserSettingChannelHistoryData> selectByArray(Long[] ids);

}
