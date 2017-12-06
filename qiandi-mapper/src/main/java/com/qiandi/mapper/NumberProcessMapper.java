package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import com.qiandi.pojo.NumberProcess;

public interface NumberProcessMapper extends IMapper<NumberProcess>
{

	void insertByList(List<NumberProcess> numberProcessList);

	List<NumberProcess> selectData(NumberProcess numberProcess);

	void deleteById(Long numberProcessId);

	void updateByMenberUserId(Map<String, Object> param);

	Map<String, Object> countNumberProcessItemByMenberUserId(Long menberUserId);

	void updateByList(List<NumberProcess> numberProcessList1);

	List<NumberProcess> selectOrderByOperator(Map<String, Object> param);

	List<NumberProcess> selectOrderByProvince(Map<String, Object> param);

	List<NumberProcess> selectOrderByCity(Map<String, Object> param);

}
