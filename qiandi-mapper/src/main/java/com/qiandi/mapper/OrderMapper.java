package com.qiandi.mapper;

import com.qiandi.pojo.Order;

public interface OrderMapper extends IMapper<Order>
{

	public Integer insertAndGetId(Order order);

}
