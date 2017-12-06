package com.qiandi.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.IncomeSettlement;
import com.qiandi.table.IncomeSettlementDataForRechargeRecord;
import com.qiandi.table.IncomeSettlementTable;

public interface IncomeSettlementMapper extends IMapper<IncomeSettlement>
{

	@SelectProvider(type = IncomeSettlementTable.class, method = "selectItemByRechargeRecordId")
	List<IncomeSettlementDataForRechargeRecord> selectItemByRechargeRecordId(
			@Param("rechargeRecordId") Long rechargeRecordId);

}
