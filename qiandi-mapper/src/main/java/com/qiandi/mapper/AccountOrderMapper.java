package com.qiandi.mapper;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.AccountOrder;
import com.qiandi.pojo.AccountOrderData;
import com.qiandi.table.AccountOrderTable;

public interface AccountOrderMapper extends IMapper<AccountOrder>
{

	@SelectProvider(type = AccountOrderTable.class, method = "selectOneData")
	AccountOrderData selectOneData(@Param("id") Long id);

}
