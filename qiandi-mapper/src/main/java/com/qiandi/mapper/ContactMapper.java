package com.qiandi.mapper;

import java.util.List;

import com.qiandi.pojo.Contact;

public interface ContactMapper extends IMapper<Contact>
{

	List<Contact> selectItem(Long menberUserId);

}
