package com.qiandi.mapper;

import java.util.List;

public interface IMapper<T>
{

	int insert(T pojo);// 根据id增加

	int update(T pojo); // 根据id修改

	int delete(Long id); // 根据id删除

	List<T> select(T pojo); // 以非空字段值作为查询条件进行查询
}
