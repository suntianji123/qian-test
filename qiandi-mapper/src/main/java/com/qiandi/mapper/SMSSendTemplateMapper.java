package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.ResultType;
import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.SMSSendTemplate;
import com.qiandi.pojo.SMSSendTemplateAuditData;
import com.qiandi.pojo.SMSSendTemplateData;
import com.qiandi.table.SMSSendTemplateTable;

public interface SMSSendTemplateMapper extends IMapper<SMSSendTemplate>
{


	Map<String, Object> loadData(Integer id);

	void insertSendData(SMSSendTemplate smsSendTemplate);

	void updateReportData(List<Map<String, Integer>> resultMap);

	void updateReportData2(List<Map<String, Object>> paramList);


	@SelectProvider(type = SMSSendTemplateTable.class, method = "selectAuditSQL")
	@ResultType(SMSSendTemplateAuditData.class)
	List<SMSSendTemplateAuditData> pageAuditData(Map<String, Object> param);

	@SelectProvider(type = SMSSendTemplateTable.class, method = "updateAuditResultByArray")
	void updateAuditResultByArray(Long[] ids, Long auditResultReasonId, String auditResult, Long adminUserId);

	void updateSendData(SMSSendTemplate smsSendTemplate);

	@SelectProvider(type = SMSSendTemplateTable.class, method = "updateIsDisabled")
	void updateIsDisabled(Long[] ids, Boolean isDisabled);

	@SelectProvider(type = SMSSendTemplateTable.class, method = "selectByArray")
	List<SMSSendTemplateAuditData> selectByArray(Long[] ids, String cc);

	@SelectProvider(type = SMSSendTemplateTable.class, method = "deleteByArray")
	void deleteByArray(Long[] ids, String cc);

	@SelectProvider(type = SMSSendTemplateTable.class, method = "selectTaskByArray")
	List<SMSSendTemplateData> selectTaskByArray(Long[] ids, String string);

	@SelectProvider(type = SMSSendTemplateTable.class, method = "selectTaskData")
	List<SMSSendTemplateData> selectTaskData(Map<String, Object> param);

}
