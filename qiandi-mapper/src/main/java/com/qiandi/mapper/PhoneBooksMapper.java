package com.qiandi.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.qiandi.pojo.Contact;
import com.qiandi.pojo.Group;
import com.qiandi.pojo.MenberUser;

/**
 * 通讯录模块
 * 
 * @author JiangQianShu
 */
public interface PhoneBooksMapper {

	/**
	 * 通讯录分组
	 * 
	 * @param phoneNum
	 * @return list
	 */
	List<Group> findGroups(@Param("phone") MenberUser menberUser);

	/**
	 * 获取分组记录数
	 * 
	 * @param phoneNum
	 * @return int
	 */
	List<Map<Integer, String>> getGroupNum(@Param("phone") MenberUser menberUser);

	/**
	 * 通讯录分组删除
	 * 
	 * @param id
	 */
	void deleteGroupById(@Param("id") Integer id, @Param("phoneNum") String phoneNum);

	/**
	 * 显示全部数量
	 * 
	 * @param menberUser
	 * @return int
	 */
	int findAllInfo(@Param("phone") MenberUser menberUser);

	/**
	 * 通讯录分组新增
	 * 
	 * @param group
	 */
	void addGroup(Group group);
	void add(Integer contactid);
	/**
	 * 根据id移至分组
	 * 
	 * @param groups
	 */
	void moveGroup(List<Contact> groups);

	/**
	 * 根据ID修改分组信息
	 * 
	 * @param id
	 */
	void updateGroup(Group groups);

	/**
	 * 通讯录列表
	 * 
	 * @param group
	 * @return list
	 */
	List<Map<String, Object>> findAllContact(@Param("phone") String phone, @Param("ctx") Contact contact,
			Integer pageNum,Integer pageSize);

	/**
	 * 根据条件查询
	 * 
	 * @param contact
	 * @return group
	 */
	//List<Group> findContactByType(@Param("ctx") Contact contact, @Param("phone") MenberUser menberUser);

	/**
	 * 根据id查询
	 * 
	 * @param id
	 * @return id
	 */
	Group findContactById(Integer id);

	/**
	 * 通讯录新增
	 * 
	 * @param group
	 */
	void addContact(Contact contacts);

	/**
	 * 通讯录删除
	 * 
	 * @param id
	 */
	void deleteConact(List<Group> groups);

	/**
	 * 通讯录修改
	 * 
	 * @param group
	 */
	void updateContact(@Param("grp") Group group, @Param("ctx") Contact contact);

	/**
	 * 获取用户id
	 * 
	 * @param menber
	 */
	int findByPhoneNum(String phoneNum);

	/**
	 * addContactgroups
	 * @param contactid
	 * @param groupid
	 */
	void addContactgroups(Integer contactid);

	/**
	 * 分组查询对应的信息
	 * @param phoneNum
	 * @param id
	 */
	List<Group> grouping(@Param("phoneNum") String phoneNum, @Param("id") Integer id);
   
	//导入
	void batchInsert(Contact list);

	//導出
	List<Map<String, Object>> getPhoneBook();
}