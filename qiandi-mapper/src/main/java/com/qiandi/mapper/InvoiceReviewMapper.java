package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.InvoiceReview;

public interface InvoiceReviewMapper extends IMapper<InvoiceReview>
{
	List<Map<String, Object>> customSelect(Map<String, Object> map);
}
