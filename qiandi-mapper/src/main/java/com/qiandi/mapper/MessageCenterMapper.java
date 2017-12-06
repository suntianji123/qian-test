package com.qiandi.mapper;

import java.util.List;

import com.qiandi.pojo.MessageCenter;

public interface MessageCenterMapper extends IMapper<MessageCenter>
{

	void insertByList(List<MessageCenter> messageCenterList);

}
