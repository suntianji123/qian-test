package com.qiandi.mapper;

import com.qiandi.pojo.Address;
import com.qiandi.pojo.SystemCharacter;

public interface SystemCharacterMapper extends IMapper<SystemCharacter>
{

	void updateIsDefault(Address param);

}
