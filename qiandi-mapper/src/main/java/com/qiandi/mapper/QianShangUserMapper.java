package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.QianShangUser;
import com.qiandi.pojo.QianShangUserData;
import com.qiandi.pojo.QianShangUserManagerData;
import com.qiandi.table.QianShangUserTable;

public interface QianShangUserMapper extends IMapper<QianShangUser>
{
	public double selectPriceById(Long qianShangUserId);

	public List<QianShangUserManagerData> selectManagerData(Map<String, Object> param);

	public List<QianShangUserManagerData> selectByArray(Long[] ids);

	@SelectProvider(type = QianShangUserTable.class, method = "selectData")
	public List<QianShangUserData> selectData(Map<String, Object> param);

	@SelectProvider(type = QianShangUserTable.class, method = "deleteByArray")
	public void deleteByArray(Long[] ids, String string);

	@SelectProvider(type = QianShangUserTable.class, method = "selectUserByArray")
	List<QianShangUserData> selectUserByArray(Long[] ids, String string);
}
