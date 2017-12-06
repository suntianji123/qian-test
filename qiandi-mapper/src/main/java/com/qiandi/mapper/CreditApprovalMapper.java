package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.CreditApproval;
import com.qiandi.pojo.CreditApprovalData;
import com.qiandi.pojo.MenberUserSettingCreditDataForUpdate;
import com.qiandi.table.CreditApprovalTable;

public interface CreditApprovalMapper extends IMapper<CreditApproval>
{

	@SelectProvider(type = CreditApprovalTable.class, method = "selectData")
	List<CreditApprovalData> selectData(Map<String, Object> param);

	@SelectProvider(type = CreditApprovalTable.class, method = "selectListByIds")
	List<CreditApproval> selectListByIds(Long[] ids, String string);

	@SelectProvider(type = CreditApprovalTable.class, method = "updateReviewStatusByArray")
	void updateReviewStatusByArray(Long[] ids, String reviewStatus, long adminUserId, long failReasonId);

	@SelectProvider(type = CreditApprovalTable.class, method = "selectMenberUserSettingCreditDataForUpdate")
	List<MenberUserSettingCreditDataForUpdate> selectMenberUserSettingCreditDataForUpdate(Long[] ids, String string);

}
