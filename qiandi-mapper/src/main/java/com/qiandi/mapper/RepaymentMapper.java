package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.Repayment;
import com.qiandi.pojo.RepaymentAuditData;
import com.qiandi.table.RepaymentTable;

public interface RepaymentMapper extends IMapper<Repayment>
{

	@SelectProvider(type = RepaymentTable.class, method = "updateLateFeeItem")
	void updateLateFeeItem(List<Repayment> repaymentList, String string);

	@SelectProvider(type = RepaymentTable.class, method = "selectAuditData")
	List<RepaymentAuditData> selectAuditData(Map<String, Object> param);

	@SelectProvider(type = RepaymentTable.class, method = "selectListByIds")
	List<Repayment> selectListByIds(Long[] repaymentIds, String cc);

	@SelectProvider(type = RepaymentTable.class, method = "selectListByRepaymentRecordId")
	List<Repayment> selectListByRepaymentRecordId(Long[] repaymentRecordIds, String string);

}
