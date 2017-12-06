package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.RecivedFailRecharge;
import com.qiandi.pojo.RecivedFailRechargeData;
import com.qiandi.table.RecivedFailRechargeTable;

public interface RecivedFailRechargeMapper extends IMapper<RecivedFailRecharge>
{

	@SelectProvider(type = RecivedFailRechargeTable.class, method = "selectData")
	List<RecivedFailRechargeData> selectData(Map<String, Object> param);

	@SelectProvider(type = RecivedFailRechargeTable.class, method = "deleteByArray")
	void deleteByArray(Long[] ids, String string);

	@SelectProvider(type = RecivedFailRechargeTable.class, method = "selectByArray")
	List<RecivedFailRechargeData> selectByArray(Long[] ids, String string);

}
