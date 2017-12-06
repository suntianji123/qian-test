package com.qiandi.mapper;

import java.util.List;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.RepaymentRecord;
import com.qiandi.pojo.RepaymentRecordDataForAudit;
import com.qiandi.table.RepaymentRecordTable;

public interface RepaymentRecordMapper extends IMapper<RepaymentRecord>
{
	@SelectProvider(type = RepaymentRecordTable.class, method = "updateRepaymentStatus")
	void updateRepaymentStatus(Long[] repaymentRecordIds, String repaymentStatus, Long failReasonId, long adminUserId);

	@SelectProvider(type = RepaymentRecordTable.class, method = "selectListById")
	List<RepaymentRecordDataForAudit> selectListById(Long[] repaymentRecordIds, String cc);

}
