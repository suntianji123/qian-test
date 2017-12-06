package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.InvoiceApply;

public interface InvoiceApplyMapper extends IMapper<InvoiceApply>
{
	List<Map<String, Object>> customSelect(Map<String, Object> map);
}
