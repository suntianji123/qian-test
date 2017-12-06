package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.BrandSignature;

public interface BrandSignatureMapper extends IMapper<BrandSignature>
{

	String selectLastNewSignature(BrandSignature brandSignature);
	
	//--------------------------LLin---------------------------
	List<Map<String, Object>> customSelect(Map<String, Object> map);
	BrandSignature logoSelect();

}
