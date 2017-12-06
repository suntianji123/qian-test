package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.Coupon;
import com.qiandi.pojo.MenberUser;
import com.qiandi.pojo.MenberUserCoupon;
import com.qiandi.pojo.MenberUserCouponData;

public interface MenberUserCouponMapper extends IManyToManyMapper<MenberUserCoupon, MenberUser, Coupon>
{

	List<Map<String, Object>> selectCouponList(Map<String, Object> param);

	Integer calcuTotalMoney(List<Long> menberUserCouponIdList);

	void updateOrderId(List<MenberUserCoupon> menberUserCouponList);

	void updateByOrderId(Long orderId);


	List<MenberUserCouponData> selectSendData(Map<String, Object> param);

	void deleteByArray(Long[] ids);

	List<MenberUserCouponData> selectSendByArray(Long[] ids);

	List<MenberUserCouponData> selectUseData(Map<String, Object> param);

	List<MenberUserCouponData> selectUseByArray(Long[] ids);

}
