package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.SensitiveWord;
import com.qiandi.pojo.SensitiveWordData;
import com.qiandi.table.SensitiveWordTable;

public interface SensitiveWordMapper extends IMapper<SensitiveWord>
{

	@SelectProvider(type = SensitiveWordTable.class, method = "selectData")
	List<SensitiveWordData> selectData(Map<String, Object> param);

	@SelectProvider(type = SensitiveWordTable.class, method = "deleteByArray")
	void deleteByArray(Long[] ids, String string);

	@SelectProvider(type = SensitiveWordTable.class, method = "selectByArray")
	List<SensitiveWordData> selectByArray(Long[] ids, String string);

}
