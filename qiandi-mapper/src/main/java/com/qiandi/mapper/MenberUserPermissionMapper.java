package com.qiandi.mapper;

import java.util.Map;

import com.qiandi.pojo.MenberUser;
import com.qiandi.pojo.MenberUserPermission;
import com.qiandi.pojo.Permission;

public interface MenberUserPermissionMapper extends IManyToManyMapper<MenberUserPermission, MenberUser, Permission>
{

	int checkPermission(Map<String, Object> params);

}
