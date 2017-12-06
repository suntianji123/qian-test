package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.AdminUserManagerData;
import com.qiandi.table.AdminUserItem;
import com.qiandi.table.AdminUserTable;

public interface AdminUserMapper extends IMapper<AdminUser>
{

	List<AdminUserManagerData> selectManagerData(Map<String, Object> param);

	void deleteByArray(Long[] ids);

	List<AdminUserManagerData> selectByArray(Long[] ids);

	@SelectProvider(type = AdminUserTable.class, method = "selectMyAdminUserItem")
	List<AdminUserItem> selectMyAdminUserItem(Long managerId);

}
