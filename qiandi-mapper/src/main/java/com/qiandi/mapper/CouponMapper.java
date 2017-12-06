package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.Coupon;
import com.qiandi.pojo.MenberUserCouponData;

public interface CouponMapper extends IMapper<Coupon>
{

	List<MenberUserCouponData> selectData(Map<String, Object> param);

}
