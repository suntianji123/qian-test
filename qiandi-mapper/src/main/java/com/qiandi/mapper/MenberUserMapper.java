package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.SelectProvider;

import com.qiandi.pojo.MenberUser;
import com.qiandi.pojo.MenberUserDataForAisle;
import com.qiandi.pojo.MenberUserItemForRecharge;
import com.qiandi.pojo.MenberUserManagerData;
import com.qiandi.table.MenberUserTable;

public interface MenberUserMapper extends IMapper<MenberUser>
{
	int registerInsert(MenberUser menberUser);

	Map<String, Integer> selectBlanceAndCount(Integer menberUserId);

	void updateSMSBlanceAndCount(Map<String, Object> paramMap);

	List<Map<String, Object>> selectData(Map<String, Object> map);

	void insertMyMenberUser(MenberUser menberUser);

	Integer selectMaxLevel(Integer id);

	Map<String, Object> selectDetail(Integer id);

	List<Map<String, Object>> selectMyMeberUserList(Long menberUserId);

	List<Map<String, Object>> selectMyMeberUserListById(List<Long> idList);

	List<Map<String, String>> selectItem(Long managerId);

	Integer selectMyMenberUserTotalCount(Long leaderId);

	List<MenberUser> selectDataByAdminUser(Map<String, Object> param);

	Map<String, Object> selectOneByPhoneNumForBillingMethod(Map<String, Object> param);

	List<Map<String, Object>> selectCustom(Map<String, Object> map);

	List<MenberUserManagerData> selectManagerData(Map<String, Object> param);

	List<MenberUserManagerData> selectByArray(Long[] ids);

	List<MenberUser> selectAccountNameAndName(Map<String, Object> param);

	List<MenberUserDataForAisle> selectDataForAisle(Long[] aisleIds);

	@SelectProvider(type = MenberUserTable.class, method = "selectItemForRecharge")
	MenberUserItemForRecharge selectItemForRecharge(@Param("accountName") String accountName);

}
