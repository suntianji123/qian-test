package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.Address;
import com.qiandi.pojo.Channel;
import com.qiandi.pojo.MenberUser;
import com.qiandi.pojo.MenberUserDataForAisle;
import com.qiandi.pojo.MenberUserDataForChannel;
import com.qiandi.pojo.MenberUserSetting;
import com.qiandi.pojo.MenberUserSettingCreditData;
import com.qiandi.pojo.MenberUserSettingCreditDataForUpdate;
import com.qiandi.table.MenberUserSettingTable;

public interface MenberUserSettingMapper extends IMapper<MenberUserSetting>
{

	void updateIsDefault(Address param);

	Long selectCharacterId(Long menberUserId);

	Map<String, Object> selectAisle(String accountName);

	void updateupdateByChannel(Channel channel);

	List<MenberUser> selectAccountNameAndName(Long[] channelIds);

	@SelectProvider(type = MenberUserSettingTable.class, method = "selectChannel")
	Map<String, Object> selectChannel(String accountName);

	@SelectProvider(type = MenberUserSettingTable.class, method = "selectDataForAisle")
	List<MenberUserDataForAisle> selectDataForAisle(Long[] aisleIds, String aisleType);

	@SelectProvider(type = MenberUserSettingTable.class, method = "updateMenberUserSettingAisleIdByArray")
	void updateMenberUserSettingAisleIdByArray(Long yiDongAisleId, Long lianTongAisleId, Long dianXingAisleId,
			Long[] menberUserSettingIds);

	@SelectProvider(type = MenberUserSettingTable.class, method = "selectMenberUserDataForChannnel")
	List<MenberUserDataForChannel> selectMenberUserDataForChannel(Long[] channelIds, String cc);

	@SelectProvider(type = MenberUserSettingTable.class, method = "updateCreditItem")
	void updateCreditItem(List<MenberUserSettingCreditDataForUpdate> list, String string);

	@SelectProvider(type = MenberUserSettingTable.class, method = "selectCreditItem")
	MenberUserSettingCreditData selectCreditItem(@Param("menberUserAccountName") String menberUserAccountName);

	@SelectProvider(type = MenberUserSettingTable.class, method = "selectQuota")
	Map<String, Integer> selectQuota(@Param("menberUserSettingId") Long menberUserSettingId);

}
