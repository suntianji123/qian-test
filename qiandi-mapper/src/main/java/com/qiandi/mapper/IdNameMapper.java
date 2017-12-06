package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.IdName;
import com.qiandi.pojo.IdNameData;
import com.qiandi.table.IdNameTable;

public interface IdNameMapper extends IMapper<IdName>
{

	@SelectProvider(type = IdNameTable.class, method = "selectData")
	List<IdNameData> selectData(Map<String, Object> param);

	@SelectProvider(type = IdNameTable.class, method = "deleteByArray")
	void deleteByArray(Long[] ids, String name);

	@SelectProvider(type = IdNameTable.class, method = "selectByArray")
	List<IdNameData> selectByArray(Long[] ids, String name);

}
