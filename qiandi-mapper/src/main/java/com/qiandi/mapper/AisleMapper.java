package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.Aisle;
import com.qiandi.pojo.AisleData;
import com.qiandi.pojo.AisleDataForAisle;
import com.qiandi.table.AisleTable;

public interface AisleMapper extends IMapper<Aisle>
{

	List<AisleData> selectData(Map<String, Object> param);

	void deleteByArray(Long[] ids);

	List<AisleData> selectByArray(Long[] ids);

	List<Aisle> selectIdAndName(Integer type);

	Map<String, Object> selectItemForRecharge(String name);

	@SelectProvider(type = AisleTable.class, method = "selectAisleDataForAisle")
	List<AisleDataForAisle> selectAisleDataForAisle(Long[] aisleIds, String aisleType);

	@SelectProvider(type = AisleTable.class, method = "updateIsOnByArray")
	void updateIsOnByArray(Boolean isOn, Long[] ids);

}
