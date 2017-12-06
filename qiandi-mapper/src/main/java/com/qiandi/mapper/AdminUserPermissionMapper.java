package com.qiandi.mapper;

import java.util.Map;

import com.qiandi.pojo.AdminUser;
import com.qiandi.pojo.AdminUserPermission;
import com.qiandi.pojo.Permission;

public interface AdminUserPermissionMapper extends IManyToManyMapper<AdminUserPermission, AdminUser, Permission>
{

	int checkPermission(Map<String, Object> params);

}
