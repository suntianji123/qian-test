package com.qiandi.mapper;




import java.util.List;
import java.util.Map;

import com.qiandi.pojo.Evaluation;

public interface EvaluationMapper extends IMapper<Evaluation>
{
	List<Map<String,Object>> selectEvas(Map<String, Object> queryMap);
	int goSelectTotal(Map<String, Object> queryMap);
	List<Map<String,Object>> statisticalEva();
	List<Map<String,Object>> yesAndNoEva();
}
