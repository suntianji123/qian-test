package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.BalanceWarningStatus;
import com.qiandi.pojo.BalanceWarningStatusData;


public interface BalanceWarningStatusMapper extends IMapper<BalanceWarningStatus>
{

	List<Map<String, Object>> selectWarningList(BalanceWarningStatus balanceWarningStatus);

	BalanceWarningStatus isWarning(Integer menberUserId);

	void updateByList(List<Integer> balanceWarningStatusIdList);

	List<BalanceWarningStatusData> selectData(Map<String, Object> param);

	BalanceWarningStatusData selectOneData(String menberUserAccountName);

	List<BalanceWarningStatusData> selectByArray(Long[] ids);

	void deleteByArray(Long[] ids);

}
