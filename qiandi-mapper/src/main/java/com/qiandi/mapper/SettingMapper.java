package com.qiandi.mapper;

import com.qiandi.pojo.Setting;

public interface SettingMapper extends IMapper<Setting>
{

	void updateByName(Setting setting);

}
