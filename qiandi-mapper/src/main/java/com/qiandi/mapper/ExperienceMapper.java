package com.qiandi.mapper;

import com.qiandi.pojo.Experience;

public interface ExperienceMapper extends IMapper<Experience>
{

	void deleteByItem(Experience experience);

}
