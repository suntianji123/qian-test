package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.SMSSendRecord;
import com.qiandi.pojo.SMSSendRecordData;
import com.qiandi.pojo.SMSSendTemplateData;
import com.qiandi.table.SMSSendRecordTable;
import com.qiandi.table.SMSSendTemplateTable;

public interface SMSSendRecordMapper extends IMapper<SMSSendRecord>
{

	void insertByList(List<SMSSendRecord> smsSendRecords);

	List<Map<String, Object>> selectData(Map<String, Object> params);

	SMSSendRecord selectOneById(Long id);

	List<Map<String, Object>> selectDataByTask(Map<String, Object> params);

	Map<String, Object> selectOneByTask(Long id);

	void deleteAllByTask(List<Long> idList);

	void deleteOneByTask(Long id);

	void deleteAllByNumber(List<Long> idList);

	void deleteOneByNumber(Long id);

	void insertSendData(List<SMSSendRecord> smsSendRecordList);

	void updateReportData(List<Map<String, Object>> paramList);

	Integer isExistMsgId(Long menberUserId);

	List<Map<String, Integer>> selectRecivedStatusCount(Long smsSendTemplateId);


	Map<String, Integer> getTotalSMSSendRecordItem(Long menberUserId);


	@SelectProvider(type = SMSSendRecordTable.class, method = "updateAuditResultByArray")
	void updateAuditResultByArray(Long[] ids, String auditResult);

	@SelectProvider(type = SMSSendRecordTable.class, method = "selectItemForRedis")
	List<String> selectItemForRedis(Boolean sendMode);

	void updateSendData(List<SMSSendRecord> smsSendRecordList);

	@SelectProvider(type = SMSSendRecordTable.class, method = "updateRedis")
	void updateIsOnRedis(Long[] ids, String cc);

	@SelectProvider(type = SMSSendRecordTable.class, method = "updateIsOnRedisByList")
	void updateIsOnRedisByList(List<Long> ids, String cc);

	@SelectProvider(type = SMSSendTemplateTable.class, method = "selectTaskData")
	List<SMSSendTemplateData> selectTaskData(Map<String, Object> param);

	@SelectProvider(type = SMSSendRecordTable.class, method = "selectNumberData")
	List<SMSSendRecordData> selectNumberData(Map<String, Object> param);

	@SelectProvider(type = SMSSendRecordTable.class, method = "deleteNumberByArray")
	void deleteNumberByArray(Long[] ids, String string);

	@SelectProvider(type = SMSSendRecordTable.class, method = "selectNumberByArray")
	List<SMSSendRecordData> selectNumberByArray(Long[] ids, String string);

}
