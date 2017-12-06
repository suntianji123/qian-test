package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.qiandi.pojo.DraftBox;
import com.qiandi.pojo.MenberUser;

public interface DraftBoxMapper extends IMapper<DraftBox >{
	/**
	 * 显示所有草稿箱信息
	 * @param menber
	 * @return list
	 */
	List<Map<String, Object>>  menber(@Param("us")MenberUser menber,@Param("id") Integer id,@Param("start") String start,@Param("laydate")String laydate,
			Integer pageNum,Integer pageSize);
	/**
	 * 批量删除
	 * @param draftBoxs
	 * @return void
	 */
	void updateinfo(List<DraftBox> draftBoxs);
	
	/**
	 * 查看详细
	 * @param draftBoxs
	 * @return list
	 * */
	List<MenberUser>  batchSelectUsers(@Param("us") MenberUser menber,@Param("id")Integer id); 
	
	/**
	 *添加
	 * @param draftBoxs
	 * @return list
	 * */
	void inserDraftBox(DraftBox box);
	
	List<MenberUser>all(MenberUser menber);
	
	List<Map<String, Object>> getDraftBox();
}

