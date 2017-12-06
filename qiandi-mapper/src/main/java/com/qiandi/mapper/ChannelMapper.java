package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.Channel;
import com.qiandi.pojo.ChannelData;
import com.qiandi.pojo.ChannelDataForAisle;
import com.qiandi.table.ChannelTable;

public interface ChannelMapper extends IMapper<Channel>
{

	List<ChannelData> selectData(Map<String, Object> param);

	void deleteByArray(Long[] ids);

	List<ChannelData> selectByArray(Long[] ids);

	Map<String, Object> selectAisle(String name);

	@SelectProvider(type = ChannelTable.class, method = "selectDataForAisle")
	List<ChannelDataForAisle> selectDataForAisle(Long[] aisleIds, String aisleType);

	@SelectProvider(type = ChannelTable.class, method = "updateAisleIdByArray")
	void updateAisleIdByArray(Long yiDongAisleId, Long lianTongAisleId, Long dianXingAisleId,
			Long[] menberUserOrChannelIds);

}
