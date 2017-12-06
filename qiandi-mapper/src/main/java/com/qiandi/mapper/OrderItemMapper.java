package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.OrderItem;
import com.qiandi.pojo.OrderItemData;

public interface OrderItemMapper extends IMapper<OrderItem>
{
	public int insertToCart(OrderItem orderItem);

	// 可根据注册时间、姓名、邮箱、手机等进行模糊查询
	public List<OrderItem> search(Map<String, Object> params);

	public List<OrderItem> settlementList(List<Long> idList);

	// public void deleteList(List<Integer> ids);

	public Integer selectTotalAmountPayAble(List<Long> idList);

	public int selectTotalSubTotal(List<Long> idList);

	public int updateOrderNum(Map<String, Object> map);

	public void deleteAll(List<Long> idList);

	public void updateByList(List<Map<String, Object>> list);

	public void updateByWeiXinPay(Long orderId);

	// 获得微信支付付款时间，orderItem有用信息插入数据库
	public List<Map<String, Object>> selectByList(Integer orderId);


	public void updateByAccount(OrderItem orderItem);

	public List<Map<String, Object>> selectGroupByPrice(Long orderId);

	public Integer selectOrderItemTotalCount(Long menberUserId);

	public List<OrderItem> selectOrderItemList(Long menberUserId);

	public List<OrderItemData> selectData(Map<String, Object> param);

	public void deleteByArray(Long[] ids);

	public List<OrderItemData> selectByArray(Long[] ids);

}
